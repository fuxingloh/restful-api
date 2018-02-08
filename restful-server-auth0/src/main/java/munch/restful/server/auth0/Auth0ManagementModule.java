package munch.restful.server.auth0;

import com.auth0.client.auth.AuthAPI;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import javax.inject.Singleton;

/**
 * Created by: Fuxing
 * Date: 22/11/17
 * Time: 2:55 AM
 * Project: facebook-corpus
 */
public final class Auth0ManagementModule extends AbstractModule {

    private final String domain;
    private final String audience;

    private final String clientId;
    private final String clientSecret;

    public Auth0ManagementModule(String domain, String audience, String clientId, String clientSecret) {
        this.domain = domain;
        this.audience = audience;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    @Override
    protected void configure() {

    }

    @Provides
    @Singleton
    ManagementAPISupplier provideManagementAPI(final AuthAPI authAPI) {
        return new ManagementAPISupplier(authAPI, domain, audience);
    }

    @Singleton
    @Provides
    AuthAPI provideAuthAPI() {
        return new AuthAPI(domain, clientId, clientSecret);
    }
}