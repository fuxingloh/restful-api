package munch.restful.core.exception;

/**
 * Created by: Fuxing
 * Date: 5/1/2018
 * Time: 1:48 AM
 * Project: restful-api
 */
public final class AuthenticationException extends StructuredException {

    static {
        ExceptionParser.registerRoot(AuthenticationException.class, AuthenticationException::new);
    }

    private AuthenticationException(StructuredException e) {
        super(e);
    }

    public AuthenticationException(String message) {
        super(401, AuthenticationException.class, message);
    }

    public AuthenticationException(int code, String message) {
        super(code, AuthenticationException.class, message);
    }
}
