package munch.restful.core.exception;

/**
 * Created By: Fuxing Loh
 * Date: 16/6/2017
 * Time: 7:04 PM
 * Project: munch-core
 */
public final class CodeException extends StructuredException {

    static {
        ExceptionParser.register(CodeException.class, CodeException::new);
    }

    public CodeException(StructuredException e) {
        super(e);
    }

    public CodeException(int code) {
        super(code, CodeException.class, null);
    }

    public CodeException(int code, String message) {
        super(code, CodeException.class, message);
    }
}
