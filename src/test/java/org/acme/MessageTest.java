package org.acme;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathTemplate;
import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.fail;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.matching.UrlPathPattern;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.jwt.build.Jwt;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.net.HttpURLConnection;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.acme.db.MyEntity;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import wiremock.com.google.common.net.HttpHeaders;

@QuarkusTest
class MessageTest {
    public static final String MY_CLAIM_NAME = "myClaim";
    public static final int CONCURRENCY = 16;
    public static final Duration ASSERTION_TIMEOUT = Duration.ofSeconds(30);

    static WireMockServer wireMockServer = new WireMockServer(
            ConfigProvider.getConfig().getValue("port", Integer.class));

    @Inject
    EntityManager entityManager;

    @BeforeAll
    static void beforeAll() {
        wireMockServer.start();
    }

    @AfterAll
    static void afterAll() {
        wireMockServer.stop();
    }

    @ParameterizedTest
    @MethodSource
    @Execution(ExecutionMode.CONCURRENT)
    void test(int i) {
        String claimValue = String.valueOf(i);
        String jwt = Jwt.claim(MY_CLAIM_NAME, claimValue)
                .signWithSecret(UUID.randomUUID().toString());
        String authHeader = "Bearer " + jwt;
        JwtContainingClaim jwtContainingClaim = new JwtContainingClaim(Map.of(MY_CLAIM_NAME, claimValue));
        UrlPathPattern urlPathPattern = urlPathTemplate("/{channelName}/{payload}");
        setupWireMockStub(urlPathPattern, "a", claimValue);
        setupWireMockStub(urlPathPattern, "b", claimValue);

        given()
                .header("Authorization", authHeader)
                .when().get("/hello")
                .then().statusCode(HttpURLConnection.HTTP_OK);

        await().atMost(ASSERTION_TIMEOUT)
                .untilAsserted(() -> {
                    wireMockServer.verify(1, postRequestedFor(urlPathPattern)
                            .withHeader(HttpHeaders.AUTHORIZATION, jwtContainingClaim)
                            .withPathParam("channelName", equalTo("a"))
                            .withPathParam("payload", equalTo(claimValue)));

                    wireMockServer.verify(1, postRequestedFor(urlPathPattern)
                            .withHeader(HttpHeaders.AUTHORIZATION, jwtContainingClaim)
                            .withPathParam("channelName", equalTo("b"))
                            .withPathParam("payload", equalTo(claimValue)));

                    try {
                        hasEntityWithId(claimValue);
                    } catch (Exception e) {
                        fail("Failed to find saved entity", e);
                    }
                });
    }

    @Transactional
    public void hasEntityWithId(String id) {
        entityManager.createQuery("SELECT e FROM MyEntity e WHERE id = :id", MyEntity.class)
                .setParameter("id", id)
                .getSingleResult();
    }

    private static void setupWireMockStub(UrlPathPattern urlPathPattern, String b, String claimValue) {
        wireMockServer.stubFor(post(urlPathPattern)
                .withPathParam("channelName", equalTo(b))
                .withPathParam("payload", equalTo(claimValue))
                .willReturn(aResponse()
                        .withStatus(HttpURLConnection.HTTP_OK)
                        .withUniformRandomDelay(400, 800)));
    }

    static Stream<Arguments> test() {
        return IntStream.rangeClosed(1, CONCURRENCY).mapToObj(Arguments::of);
    }
}