package org.acme;

import io.smallrye.jwt.auth.principal.DefaultJWTCallerPrincipal;
import io.smallrye.jwt.auth.principal.JWTAuthContextInfo;
import io.smallrye.jwt.auth.principal.JWTCallerPrincipal;
import io.smallrye.jwt.auth.principal.JWTCallerPrincipalFactory;
import io.smallrye.jwt.auth.principal.ParseException;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.ws.rs.Priorities;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.InvalidJwtException;

/**
 * Simply parse the JWT with no validation
 */
@ApplicationScoped
@Alternative
@Priority(Priorities.AUTHENTICATION + 100)
public class JwtParser extends JWTCallerPrincipalFactory {
    @Override
    public JWTCallerPrincipal parse(String token, JWTAuthContextInfo authContextInfo) throws ParseException {
        try {
            String json = new String(Base64.getUrlDecoder().decode(token.split("\\.")[1]), StandardCharsets.UTF_8);
            return new DefaultJWTCallerPrincipal(token, "JWT", JwtClaims.parse(json));
        } catch (InvalidJwtException | IndexOutOfBoundsException e) {
            throw new ParseException(e.getMessage(), e);
        }
    }
}
