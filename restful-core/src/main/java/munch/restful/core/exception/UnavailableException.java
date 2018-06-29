package munch.restful.core.exception;

/**
 * Created by: Fuxing
 * Date: 3/12/2017
 * Time: 8:13 AM
 * Project: restful-api
 */
public class UnavailableException extends StructuredException {

    static {
        ExceptionParser.register(UnavailableException.class, UnavailableException::new);
    }

    UnavailableException(StructuredException e) {
        super(e);
    }


    public UnavailableException(Throwable throwable) {
        super(503, UnavailableException.class, "503 Service Temporarily Unavailable", throwable);
    }
}
