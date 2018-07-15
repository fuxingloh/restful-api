package munch.restful.core.exception;

/**
 * Created By: Fuxing Loh
 * Date: 22/3/2017
 * Time: 5:19 PM
 * Project: munch-core
 */
public final class TimeoutException extends StructuredException {

    static {
        ExceptionParser.registerRoot(TimeoutException.class, TimeoutException::new);
    }

    TimeoutException(StructuredException e) {
        super(e);
    }


    public TimeoutException(Throwable throwable) {
        super(408, TimeoutException.class, "Request from client to server has timeout.", throwable);
    }

    public TimeoutException(int code, Throwable throwable) {
        super(code, TimeoutException.class, "Request from client to server has timeout.", throwable);
    }
}
