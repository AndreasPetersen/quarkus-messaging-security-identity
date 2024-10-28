package org.acme;

import static io.restassured.RestAssured.given;

import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.jwt.build.Jwt;
import java.net.HttpURLConnection;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@QuarkusTest
class MessageTest {
    public static final String MY_CLAIM_NAME = "myClaim";

    @Test
    void run() {
        String claimValue = "Hello Quarkus 1";
        String authHeader = createJwtWithClaim(claimValue);

        given()
                .header("Authorization", authHeader)
                .when().get("/hello")
                .then().statusCode(HttpURLConnection.HTTP_OK);
    }

    private static String createJwtWithClaim(String claimValue) {
        String jwt = Jwt.claim(MY_CLAIM_NAME, claimValue)
                .signWithSecret(UUID.randomUUID().toString());
        return "Bearer " + jwt;
    }
}