package org.acme;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.github.tomakehurst.wiremock.matching.MatchResult;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import java.util.Map;
import java.util.Optional;
import wiremock.com.fasterxml.jackson.annotation.JsonProperty;

public class JwtContainingClaim extends StringValuePattern {
    private final Map<String, String> expectedClaims;

    public JwtContainingClaim(@JsonProperty("expectedClaims") Map<String, String> expectedClaims) {
        super("");
        this.expectedClaims = expectedClaims;
    }

    @Override
    public MatchResult match(String value) {
        try {
            DecodedJWT jwt = JWT.decode(value.split(" ")[1]);
            return expectedClaims.entrySet().stream()
                    .map(entry -> {
                        Optional<String> claim = getClaimFromJwt(jwt, entry.getKey());
                        return claim.map(c -> c.equals(entry.getValue())).orElse(false);
                    })
                    .map(MatchResult::of)
                    .reduce(MatchResult.aggregate(), MatchResult::aggregate);
        } catch (JWTDecodeException e) {
            return MatchResult.of(false);
        }
    }

    private static Optional<String> getClaimFromJwt(DecodedJWT jwt, String claimName) {
        Claim claim = jwt.getClaim(claimName);

        if (!claim.isMissing() && !claim.isNull()) {
            String claimAsString = claim.as(Object.class).toString();
            return Optional.of(claimAsString);
        }

        return Optional.empty();
    }

    public Map<String, String> getExpectedClaims() {
        return expectedClaims;
    }
}
