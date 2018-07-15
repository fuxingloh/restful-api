package munch.restful.core.exception;

/**
 * Created By: Fuxing Loh
 * Date: 18/3/2017
 * Time: 3:42 PM
 * Project: munch-core
 */
public final class OfflineException extends StructuredException {

    static {
        ExceptionParser.registerRoot(OfflineException.class, OfflineException::new);
    }

    OfflineException(StructuredException e) {
        super(e);
    }


    public OfflineException(Throwable throwable) {
        super(500, OfflineException.class, "Request from client to server is not reachable.", throwable);
    }
}
