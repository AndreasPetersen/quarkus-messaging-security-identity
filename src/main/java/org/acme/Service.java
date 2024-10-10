package org.acme;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class Service {
    @Inject
    JsonWebToken token;

    @RestClient
    ExternalServiceRestClient externalServiceRestClient;

    public Uni<Void> doSomething(String channelName, String payload) {
        return externalServiceRestClient.post(channelName, payload)
                .replaceWithVoid();
    }
}
