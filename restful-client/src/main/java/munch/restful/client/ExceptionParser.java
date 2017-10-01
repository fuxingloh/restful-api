package munch.restful.client;

import munch.restful.core.exception.OfflineException;
import munch.restful.core.exception.TimeoutException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.NoHttpResponseException;
import org.apache.http.conn.HttpHostConnectException;

import java.net.SocketTimeoutException;

/**
 * Created by: Fuxing
 * Date: 22/8/2017
 * Time: 11:39 AM
 * Project: munch-core
 */
public final class ExceptionParser {
    public static void parse(Exception e) {
        for (Throwable throwable : ExceptionUtils.getThrowables(e)) {
            parseEach(e, throwable);
        }
    }

    private static void parseEach(Exception e, Throwable throwable) {
        if (throwable instanceof HttpHostConnectException) {
            throw new OfflineException(e);
        }
        if (throwable instanceof NoHttpResponseException) {
            throw new OfflineException(e);
        }
        if (throwable instanceof SocketTimeoutException) {
            throw new TimeoutException(e);
        }
    }
}
