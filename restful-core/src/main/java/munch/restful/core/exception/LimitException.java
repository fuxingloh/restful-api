package munch.restful.core.exception;

/**
 * Created by: Fuxing
 * Date: 29/6/18
 * Time: 2:52 PM
 * Project: restful-api
 */
public final class LimitException extends StructuredException {
    static {
        ExceptionParser.registerRoot(LimitException.class, LimitException::new);
    }

    private LimitException(StructuredException e) {
        super(e);
    }

    public LimitException(String message) {
        super(400, LimitException.class, message);
    }
}
