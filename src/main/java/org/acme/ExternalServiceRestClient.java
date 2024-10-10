package org.acme;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "external-service")
@RegisterClientHeaders(HeaderFactory.class)
public interface ExternalServiceRestClient {
    @POST
    @Path("/{channelName}/{payload}")
    Uni<Response> post(@PathParam("channelName") String channelName, @PathParam("payload") String payload);
}
