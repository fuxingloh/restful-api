package munch.restful.server.auth0;

import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.JwkProviderBuilder;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import munch.restful.server.jwt.TokenAuthenticator;

import javax.inject.Singleton;

/**
 * Created by: Fuxing
 * Date: 8/2/18
 * Time: 3:01 PM
 * Project: restful-api
 */
public final class Auth0AuthenticationModule extends AbstractModule {
    private final String audience;
    private final String issuer;

    public Auth0AuthenticationModule(String audience, String issuer) {
        this.audience = audience;
        this.issuer = issuer;
    }

    @Provides
    @Singleton
    JwkProvider provideJwkProvider() {
        return new JwkProviderBuilder(issuer).build();
    }

    @Provides
    @Singleton
    TokenAuthenticator provideAuthenticator(JwkProvider jwkProvider) {
        return new Auth0Authenticator(jwkProvider, audience, issuer);
    }
}
