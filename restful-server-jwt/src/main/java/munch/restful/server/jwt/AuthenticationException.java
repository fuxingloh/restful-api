package munch.restful.server.jwt;

import munch.restful.core.exception.StructuredException;

/**
 * Created by: Fuxing
 * Date: 5/1/2018
 * Time: 1:48 AM
 * Project: restful-api
 */
public class AuthenticationException extends StructuredException {

    public AuthenticationException(String message) {
        super(401, "AuthenticationException", message);
    }

    public AuthenticationException(int code, String message) {
        super(code, "AuthenticationException", message);
    }
}
