package munch.restful.core.exception;

/**
 * Created by: Fuxing
 * Date: 10/12/2016
 * Time: 11:26 AM
 * Project: corpus-catalyst
 */
public final class JsonException extends StructuredException {

    /**
     * @param cause throwable for actual cause of json exception
     */
    public JsonException(Throwable cause, String url) {
        super(400, "JsonException", cause.getMessage() + "\n" + url, cause);
    }

    /**
     * @param cause throwable for actual cause of json exception
     */
    public JsonException(Throwable cause) {
        super(400, "JsonException", cause.getMessage(), cause);
    }

    /**
     * @param message readable message
     */
    public JsonException(String message) {
        super(400, "JsonException", message);
    }
}
