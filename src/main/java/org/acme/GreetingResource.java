package org.acme;

import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.auth.principal.ParseException;
import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.MutinyEmitter;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Message;

@Path("/hello")
public class GreetingResource {

    @Inject
    JsonWebToken jwt;

    @Inject
    JWTParser jwtParser;

    @Channel("a")
    MutinyEmitter<String> channelA;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<String> hello() throws ParseException {
        String greeting = "Hello Quarkus";

        JsonWebToken jsonWebToken = jwtParser.parse(jwt.getRawToken());
        Message<String> message = Message.of(jsonWebToken.getClaim("myClaim").toString()).addMetadata(jsonWebToken);

        return channelA.sendMessage(message)
                .onItem().transform(v -> greeting);
    }
}
