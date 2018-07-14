package munch.restful.core.exception;

import org.apache.commons.lang3.exception.ExceptionUtils;

import java.net.SocketTimeoutException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by: Fuxing
 * Date: 22/8/2017
 * Time: 11:39 AM
 * Project: munch-core
 */
public final class ExceptionParser {
    private static final Map<String, Consumer<StructuredException>> FOUND_CONSUMERS = new HashMap<>();
    private static final Set<String> NOT_FOUND_LIST = new HashSet<>();

    /**
     * @param name     to resolve
     * @param consumer to apply Exception
     */
    private static void consume(String name, Consumer<StructuredException> consumer) {
        FOUND_CONSUMERS.put(name, consumer);
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
        consume(tClass.getName(), e -> {
            throw function.apply(e);
        });
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
        Consumer<StructuredException> consumer = FOUND_CONSUMERS.get(e.getType());
        if (consumer == null && !NOT_FOUND_LIST.contains(e.getType())) {
            try {
                Class.forName(e.getType());
                consumer = FOUND_CONSUMERS.get(e.getType());
            } catch (ClassNotFoundException e1) {
                NOT_FOUND_LIST.add(e.getType());
            }
        }

        // Try again
        if (consumer != null) consumer.accept(e);
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
