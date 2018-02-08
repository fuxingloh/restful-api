package munch.restful.server.auth0;

import com.auth0.jwk.*;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import munch.restful.server.jwt.AuthenticatedToken;
import munch.restful.server.jwt.AuthenticationException;
import munch.restful.server.jwt.TokenAuthenticator;

import java.security.interfaces.RSAPublicKey;

/**
 * Created by: Fuxing
 * Date: 8/2/18
 * Time: 3:59 PM
 * Project: restful-api
 */
public final class Auth0Authenticator extends TokenAuthenticator {
    private final String audience;
    private final String issuer;

    private final JwkProvider jwkProvider;

    public Auth0Authenticator(JwkProvider jwkProvider, String audience, String issuer) {
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

    public AuthenticatedToken authenticate(DecodedJWT decodedJwt) throws AuthenticationException {
        final String kid = decodedJwt.getKeyId();

        try {
            Jwk jwk = jwkProvider.get(kid);
            JWTVerifier jwtVerifier = providerForRS256((RSAPublicKey) jwk.getPublicKey());

            DecodedJWT verifiedJWT = jwtVerifier.verify(decodedJwt.getToken());
            return new AuthenticatedToken(verifiedJWT);
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
