package munch.restful.server.auth0;

import com.auth0.jwt.interfaces.DecodedJWT;

/**
 * Created by: Fuxing
 * Date: 5/1/2018
 * Time: 1:47 AM
 * Project: restful-api
 */
public final class AuthenticatedJWT {
    private final DecodedJWT decodedJWT;
    private final boolean authenticated;

    public AuthenticatedJWT(DecodedJWT decodedJWT) {
        this.decodedJWT = decodedJWT;
        this.authenticated = decodedJWT != null;
    }

    /**
     * @return raw decoded JWT
     */
    public DecodedJWT getDecodedJWT() {
        return decodedJWT;
    }

    /**
     * @return whether user is authenticated
     */
    public boolean isAuthenticated() {
        return authenticated;
    }

    /**
     * @return subject: user id
     */
    public String getSubject() {
        return decodedJWT.getSubject();
    }
}
