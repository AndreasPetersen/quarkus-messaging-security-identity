package org.acme;

import io.quarkus.security.identity.CurrentIdentityAssociation;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

@ApplicationScoped
public class MessageResource {
    @Inject
    Logger logger;

    @Inject
    CurrentIdentityAssociation currentIdentityAssociation;

    @Inject
    JsonWebToken jsonWebToken;

    @Incoming("a")
    Uni<Void> channelA(String payload, JsonWebToken jwt) {
        return taskA(payload, jwt)
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }

    @ActivateRequestContext
    public Uni<Void> taskA(String payload, JsonWebToken jwt) {
        setToken(jwt);
        return Uni.createFrom().voidItem();
    }

    private void setToken(JsonWebToken jwt) {
        // Comment out to get the getClaim on line 42 to return the claim
        logger.info("myClaim: " + jsonWebToken.getClaim("myClaim"));

        currentIdentityAssociation.setIdentity(new MessageSecurityIdentity(jwt));

        logger.info("myClaim: " + jsonWebToken.getClaim("myClaim"));
    }
}
