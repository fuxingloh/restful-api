package munch.restful.server;

import munch.restful.core.exception.StructuredException;

/**
 * Created by: Fuxing
 * Date: 24/11/2017
 * Time: 8:24 PM
 * Project: restful-api
 */
public class SecuredException extends StructuredException {

    public SecuredException(int code, String message) {
        super(code, "SecuredException", message);
    }

    public SecuredException(int code, String message, Throwable throwable) {
        super(code, "SecuredException", message, throwable);
    }
}
