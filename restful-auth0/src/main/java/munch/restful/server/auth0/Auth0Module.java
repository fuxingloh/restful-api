package munch.restful.server.auth0;

import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.JwkProviderBuilder;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import javax.inject.Singleton;

/**
 * Created by: Fuxing
 * Date: 5/1/2018
 * Time: 1:13 AM
 * Project: restful-api
 */
public final class Auth0Module extends AbstractModule {

    private final String audience;
    private final String issuer;

    public Auth0Module(String audience, String issuer) {
        this.audience = audience;
        this.issuer = issuer;
    }

    @Override
    protected void configure() {

    }

    @Provides
    @Singleton
    JwkProvider provideJwkProvider() {
        return new JwkProviderBuilder(issuer).build();
    }

    @Provides
    @Singleton
    JwtAuthenticator provideAuthenticator(JwkProvider jwkProvider) {
        return new JwtAuthenticator(jwkProvider, audience, issuer);
    }
}
