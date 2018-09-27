package munch.restful.core.exception;

/**
 * Created by: Fuxing
 * Date: 27/9/18
 * Time: 3:15 PM
 * Project: restful-api
 */
public final class BadRequestException extends StructuredException {
    static {
        ExceptionParser.register(BadRequestException.class, BadRequestException::new);
    }

    private BadRequestException(StructuredException e) {
        super(e);
    }

    /**
     * @param message bad request message¬
     */
    public BadRequestException(String message) {
        super(400, BadRequestException.class, message);
    }
}
