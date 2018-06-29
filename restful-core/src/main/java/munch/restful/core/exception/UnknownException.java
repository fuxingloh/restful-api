package munch.restful.core.exception;

/**
 * Created by: Fuxing
 * Date: 16/6/2017
 * Time: 1:27 PM
 * Project: munch-core
 */
public final class UnknownException extends StructuredException {

    static {
        ExceptionParser.register(UnknownException.class, UnknownException::new);
    }

    UnknownException(StructuredException e) {
        super(e);
    }


    /**
     * Map any throwable to Unknown Exception
     *
     * @param throwable throwable unknown error
     */
    public UnknownException(Throwable throwable) {
        super(500, UnknownException.class, throwable.getMessage(), throwable);
    }
}
