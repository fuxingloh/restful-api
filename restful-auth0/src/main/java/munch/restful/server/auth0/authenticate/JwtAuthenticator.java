package munch.restful.server.auth0.authenticate;

import com.auth0.jwk.*;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import munch.restful.server.JsonCall;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.interfaces.RSAPublicKey;

/**
 * Created by: Fuxing
 * Date: 5/1/2018
 * Time: 1:32 AM
 * Project: restful-api
 */
public final class JwtAuthenticator {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticator.class);

    private final String audience;
    private final String issuer;

    private final JwkProvider jwkProvider;

    public JwtAuthenticator(JwkProvider jwkProvider, String audience, String issuer) {
        this.jwkProvider = jwkProvider;
        this.audience = audience;
        this.issuer = issuer;
    }

    private JWTVerifier providerForRS256(RSAPublicKey key) {
        return JWT.require(Algorithm.RSA256(key, null))
                .withIssuer(issuer)
                .withAudience(audience)
                .build();
    }

    /**
     * defaults: requires authentication
     *
     * @param call json call
     * @return AuthenticatedJWT util
     * @throws AuthenticationException authentication error
     */
    public AuthenticatedJWT authenticate(JsonCall call) throws AuthenticationException {
        return authenticate(call, true);
    }


    /**
     * @param call     json call
     * @param requires whether authentication is optional
     * @return AuthenticatedJWT util
     * @throws AuthenticationException authentication error
     */
    public AuthenticatedJWT authenticate(JsonCall call, boolean requires) throws AuthenticationException {
        DecodedJWT decoded = call.getJWT();
        if (decoded == null) {
            if (requires) throw new AuthenticationException(403, "Forbidden");
            return new AuthenticatedJWT(null);
        }
        return authenticate(decoded);
    }

    public AuthenticatedJWT authenticate(DecodedJWT decodedJwt) throws AuthenticationException {
        final String kid = decodedJwt.getKeyId();

        try {
            Jwk jwk = jwkProvider.get(kid);
            JWTVerifier jwtVerifier = providerForRS256((RSAPublicKey) jwk.getPublicKey());

            DecodedJWT verifiedJWT = jwtVerifier.verify(decodedJwt.getToken());
            return new AuthenticatedJWT(verifiedJWT);
        } catch (SigningKeyNotFoundException e) {
            logger.error("Could not retrieve jwks from issuer", e);
            throw new AuthenticationException("Could not retrieve jwks from issuer");
        } catch (InvalidPublicKeyException e) {
            logger.error("Could not retrieve public key from issuer", e);
            throw new AuthenticationException("Could not retrieve public key from issuer");
        } catch (JwkException e) {
            logger.error("Cannot authenticate with jwt", e);
            throw new AuthenticationException("Cannot authenticate with jwt");
        } catch (JWTVerificationException e) {
            logger.warn("Not a valid token", e);
            throw new AuthenticationException("Failed verification");
        }
    }
}
