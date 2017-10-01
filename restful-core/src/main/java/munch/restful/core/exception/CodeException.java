package munch.restful.core.exception;

/**
 * Created By: Fuxing Loh
 * Date: 16/6/2017
 * Time: 7:04 PM
 * Project: munch-core
 */
public class CodeException extends RuntimeException {

    private final int code;

    /**
     * This is another special type of exception
     * Similar to StructuredException, it is caught to parse.
     * However this is not a error exception it is a code meta exception
     * Value of code will be set to meta.code and http status code
     * <pre>
     * {
     *     meta: {
     *         code: 200
     *     }
     * }
     * </pre>
     * Code exception log is set to trace only
     *
     * @param code code
     */
    public CodeException(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
