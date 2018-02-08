package munch.restful.server.auth0;

import com.auth0.client.auth.AuthAPI;
import com.auth0.client.mgmt.ManagementAPI;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.auth.TokenHolder;
import com.google.common.base.Supplier;

import java.util.Objects;

/**
 * Created by: Fuxing
 * Date: 22/11/17
 * Time: 3:43 AM
 * Project: facebook-corpus
 */
public class ManagementAPISupplier implements Supplier<ManagementAPI> {
    private final AuthAPI authAPI;
    private final String domain;
    private final String audience;

    private TokenInstance tokenInstance;
    private ManagementAPI apiInstance;

    public ManagementAPISupplier(AuthAPI authAPI, String domain, String audience) {
        this.authAPI = authAPI;
        this.domain = domain;
        this.audience = audience;
        // Eager load
        get();
    }

    @Override
    public ManagementAPI get() {
        if (tokenInstance == null || tokenInstance.isExpiring()) {
            try {
                TokenHolder token = authAPI.requestToken(audience).execute();
                Objects.requireNonNull(token.getAccessToken());

                this.tokenInstance = new TokenInstance(token);
                this.apiInstance = new ManagementAPI(domain, token.getAccessToken());
            } catch (Auth0Exception e) {
                throw new RuntimeException(e);
            }
        }
        return apiInstance;
    }

    private class TokenInstance {
        private final long expiresAt;

        private TokenInstance(TokenHolder token) {
            // With 1 hour safety
            if (token.getExpiresIn() < 7200) throw new IllegalArgumentException("Token must expires > 7200");
            this.expiresAt = System.currentTimeMillis() / 1000 + token.getExpiresIn() - 3600;
        }

        private boolean isExpiring() {
            return System.currentTimeMillis() / 1000 > expiresAt;
        }
    }
}