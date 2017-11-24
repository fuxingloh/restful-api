package munch.restful.server;

import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.JwkProviderBuilder;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.RSAKeyProvider;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.typesafe.config.Config;

import javax.inject.Singleton;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * Created by: Fuxing
 * Date: 24/11/2017
 * Time: 8:01 PM
 * Project: restful-api
 */
public class Auth0Module extends AbstractModule {

    public Auth0Module() {
    }

    @Provides
    @Singleton
    JwkProvider provideJwk(Config config) {
        String issuer = config.getString("services.auth0.issuer");
        return new JwkProviderBuilder(issuer).build();
    }

    @Provides
    @Singleton
    RSAKeyProvider provideKeyProvider(JwkProvider jwkProvider) {
        return new RSAKeyProvider() {
            @Override
            public RSAPublicKey getPublicKeyById(String kid) {
                //Received 'kid' value might be null if it wasn't defined in the Token's header
                try {
                    return (RSAPublicKey) jwkProvider.get(kid).getPublicKey();
                } catch (JwkException e) {
                    throw new SecuredException(500, "JwkProvider failed", e);
                }
            }

            @Override
            public RSAPrivateKey getPrivateKey() {
                return null;
            }

            @Override
            public String getPrivateKeyId() {
                return null;
            }
        };
    }

    @Provides
    @Singleton
    Algorithm provideAlgorithm(RSAKeyProvider keyProvider) {
        return Algorithm.RSA256(keyProvider);
    }

    @Provides
    @Singleton
    JWTVerifier provideVerifier(Config config, Algorithm algorithm) {
        String issuer = config.getString("services.auth0.issuer");
        return JWT.require(algorithm).withIssuer(issuer).build();
    }

    @Override
    protected void configure() {

    }
}
