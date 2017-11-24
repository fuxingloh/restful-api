package munch.restful.server;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.Payload;
import spark.Request;
import spark.Response;

import java.util.Optional;

/**
 * Created by: Fuxing
 * Date: 24/11/2017
 * Time: 8:49 PM
 * Project: restful-api
 */
public class SecuredCall extends JsonCall {
    private final DecodedJWT token;

    /**
     * @param call  json call to embed
     * @param token token
     */
    public SecuredCall(JsonCall call, DecodedJWT token) {
        this(call.request(), call.response(), token);
    }

    /**
     * @param request  spark request
     * @param response spark response
     * @param token    token
     */
    SecuredCall(Request request, Response response, DecodedJWT token) {
        super(request, response);
        this.token = token;
    }

    /**
     * @return decoded json web token
     * @throws SecuredException if token is not available
     */
    public DecodedJWT requireToken() {
        if (token == null) throw new SecuredException(403, "Authentication required");
        return token;
    }

    /**
     * @return optional token
     */
    public Optional<DecodedJWT> optionalToken() {
        return Optional.ofNullable(token);
    }

    /**
     * @return token subject
     * @throws SecuredException if token is not available
     * @see DecodedJWT#getToken()
     */
    public String requireTokenSubject() {
        if (token == null) throw new SecuredException(403, "Authentication required");
        return token.getSubject();
    }

    /**
     * @return optional token subject
     */
    public Optional<String> optionalTokenSubject() {
        return optionalToken()
                .map(Payload::getSubject);
    }
}
