package org.acme;

import io.quarkus.security.identity.CurrentIdentityAssociation;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.smallrye.reactive.messaging.annotations.Blocking;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.acme.db.Dao;
import org.acme.db.MyEntity;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

@ApplicationScoped
public class MessageResource {
    @Inject
    CurrentIdentityAssociation currentIdentityAssociation;

    @Inject
    Service service;

    @Inject
    Dao dao;

    @Incoming("a")
    @Outgoing("b")
    Uni<String> channelA(String payload, JsonWebToken jwt) {
        return taskA(payload, jwt)
                .replaceWith(payload)
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }

    @Incoming("b")
    Uni<Void> channelB(String payload, JsonWebToken jwt) {
        return taskB(payload, jwt)
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }

    @ActivateRequestContext
    public Uni<Void> taskA(String payload, JsonWebToken jwt) {
        setToken(jwt);
        return service.doSomething("a", payload);
    }

    @ActivateRequestContext
    public Uni<Void> taskB(String payload, JsonWebToken jwt) {
        setToken(jwt);
        MyEntity myEntity = new MyEntity(payload);
        return service.doSomething("b", payload)
                .onItem().transformToUni(v -> persist(myEntity));
    }

    public Uni<Void> persist(MyEntity myEntity) {
        return Uni.createFrom().item(myEntity)
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
                .onItem().invoke(() -> dao.persist(myEntity))
                .replaceWithVoid();
    }

    private void setToken(JsonWebToken jwt) {
        currentIdentityAssociation.setIdentity(new MessageSecurityIdentity(jwt));
    }
}
