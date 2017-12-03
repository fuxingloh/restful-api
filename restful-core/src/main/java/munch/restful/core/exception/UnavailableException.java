package munch.restful.core.exception;

/**
 * Created by: Fuxing
 * Date: 3/12/2017
 * Time: 8:13 AM
 * Project: restful-api
 */
public class UnavailableException extends StructuredException {

    public UnavailableException(Throwable throwable) {
        super(503, "UnavailableException", "503 Service Temporarily Unavailable", throwable);
    }
}
