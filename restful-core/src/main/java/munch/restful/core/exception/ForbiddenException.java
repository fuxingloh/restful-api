package munch.restful.core.exception;

/**
 * Created by: Fuxing
 * Date: 17/7/18
 * Time: 12:39 AM
 * Project: restful-api
 */
public final class ForbiddenException extends StructuredException {
    static {
        ExceptionParser.registerRoot(ForbiddenException.class, ForbiddenException::new);

    }

    ForbiddenException(StructuredException e) {
        super(e);
    }

    /**
     * @param message reasons
     */
    public ForbiddenException(String message) {
        super(403, ForbiddenException.class, message);
    }
}
