package munch.restful.core.exception;

/**
 * Created By: Fuxing Loh
 * Date: 22/3/2017
 * Time: 5:19 PM
 * Project: munch-core
 */
public final class TimeoutException extends StructuredException {

    public TimeoutException(Throwable throwable) {
        super(408, "TimeoutException", "Request from client to server has timeout.", throwable);
    }
}
