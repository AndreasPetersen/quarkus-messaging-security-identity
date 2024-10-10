package org.acme;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.rest.client.ext.ClientHeadersFactory;

@Dependent
public class HeaderFactory implements ClientHeadersFactory {

    @Inject
    JsonWebToken jwt;

    @Override
    public MultivaluedMap<String, String> update(MultivaluedMap<String, String> multivaluedMap, MultivaluedMap<String, String> multivaluedMap1) {
        MultivaluedMap<String, String> result = new MultivaluedHashMap();
        result.add("Authorization", "Bearer " + this.jwt.getRawToken());
        return result;
    }
}
