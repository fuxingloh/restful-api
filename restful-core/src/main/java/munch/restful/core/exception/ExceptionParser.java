package munch.restful.core.exception;

import org.apache.commons.lang3.exception.ExceptionUtils;

import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by: Fuxing
 * Date: 22/8/2017
 * Time: 11:39 AM
 * Project: munch-core
 */
public final class ExceptionParser {
    private static final Map<String, Consumer<StructuredException>> mapper = new HashMap<>();

    /**
     * @param type     to resolve
     * @param consumer to apply Exception
     */
    public static void consume(String type, Consumer<StructuredException> consumer) {
        mapper.put(type, consumer);
    }

    /**
     * Register a new exception type to resolve
     *
     * @param type     name type to register
     * @param function to cast Exception type
     * @param <T>      Exception Class Type
     */
    public static <T extends StructuredException> void register(String type, Function<StructuredException, T> function) {
        consume(type, e -> {
            throw function.apply(e);
        });
    }

    /**
     * Register a new class type of resolve
     * Exception type must be same as class name for this to work
     *
     * @param tClass   exception class to register
     * @param function to cast Exception type
     * @param <T>      Exception Class Type
     */
    public static <T extends StructuredException> void register(Class<T> tClass, Function<StructuredException, T> function) {
        register(tClass.getSimpleName(), function);
    }

    public static void parse(Exception e) {
        if (e instanceof StructuredException) {
            parseStructured((StructuredException) e);
        }

        for (Throwable throwable : ExceptionUtils.getThrowables(e)) {
            parseEach(e, throwable);
        }
    }

    /**
     * @param e structured exception to parse
     * @throws StructuredException thrown
     */
    private static void parseStructured(StructuredException e) throws StructuredException {
        mapper.get(e.getType()).accept(e);

        throw e;
    }

    private static void parseEach(Exception e, Throwable throwable) {
        String name = throwable.getClass().getSimpleName();
        if (name.equals("HttpHostConnectException")) throw new OfflineException(e);
        if (name.equals("NoHttpResponseException")) throw new OfflineException(e);

        if (throwable instanceof SocketTimeoutException) {
            throw new TimeoutException(e);
        }
    }
}
