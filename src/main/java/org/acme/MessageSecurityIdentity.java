package org.acme;

import io.quarkus.security.credential.Credential;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;
import java.security.Permission;
import java.security.Principal;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.eclipse.microprofile.jwt.JsonWebToken;

class MessageSecurityIdentity implements SecurityIdentity {
    private final JsonWebToken jsonWebToken;

    public MessageSecurityIdentity(JsonWebToken jsonWebToken) {
        this.jsonWebToken = jsonWebToken;
    }

    @Override
    public Principal getPrincipal() {
        return jsonWebToken;
    }

    @Override
    public boolean isAnonymous() {
        return jsonWebToken == null;
    }

    @Override
    public Set<String> getRoles() {
        return Collections.emptySet();
    }

    @Override
    public boolean hasRole(String role) {
        return false;
    }

    @Override
    public <T extends Credential> T getCredential(Class<T> credentialType) {
        return null;
    }

    @Override
    public Set<Credential> getCredentials() {
        return Collections.emptySet();
    }

    @Override
    public <T> T getAttribute(String name) {
        return null;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return Collections.emptyMap();
    }

    @Override
    public Uni<Boolean> checkPermission(Permission permission) {
        return Uni.createFrom().item(false);
    }
}
