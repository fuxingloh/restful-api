package munch.restful.server;

import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.interfaces.DecodedJWT;

import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * Created by: Fuxing
 * Date: 24/11/2017
 * Time: 7:34 PM
 * Project: restful-api
 */
public abstract class SecuredService implements JsonService {

    private JWTVerifier jwtVerifier;

    @Inject
    void inject(JWTVerifier jwtVerifier) {
        this.jwtVerifier = jwtVerifier;
    }

    protected void GET(String path, SecuredRoute route) {
        GET(path, (JsonCall call) -> securedRoute(call, route));
    }

    protected void POST(String path, SecuredRoute route) {
        POST(path, (JsonCall call) -> securedRoute(call, route));
    }

    protected void POST(String path, String acceptType, SecuredRoute route) {
        POST(path, acceptType, (JsonCall call) -> securedRoute(call, route));
    }

    protected void PUT(String path, SecuredRoute route) {
        PUT(path, (JsonCall call) -> securedRoute(call, route));
    }

    protected void PUT(String path, String acceptType, SecuredRoute route) {
        PUT(path, acceptType, (JsonCall call) -> securedRoute(call, route));
    }

    protected void DELETE(String path, SecuredRoute route) {
        DELETE(path, (JsonCall call) -> securedRoute(call, route));
    }

    protected void HEAD(String path, SecuredRoute route) {
        HEAD(path, (JsonCall call) -> securedRoute(call, route));
    }

    protected void PATCH(String path, SecuredRoute route) {
        PATCH(path, (JsonCall call) -> securedRoute(call, route));
    }

    private Object securedRoute(JsonCall call, SecuredRoute route) throws Exception {
        String token = getToken(call);
        if (token == null) {
            return route.handle(new SecuredCall(call, null));
        } else {
            DecodedJWT jwt = jwtVerifier.verify(token);
            return route.handle(new SecuredCall(call, jwt));
        }
    }

    /**
     * @param call Authorization get from header
     * @return token or null if don't exist
     */
    @Nullable
    static String getToken(JsonCall call) {
        final String value = call.getHeader("Authorization");
        if (value == null || !value.toLowerCase().startsWith("bearer")) {
            return null;
        }

        String[] parts = value.split(" ");
        if (parts.length < 2) {
            return null;
        }

        return parts[1].trim();
    }
}
