package munch.restful.client;

import munch.restful.core.exception.StructuredException;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by: Fuxing
 * Date: 22/8/2017
 * Time: 11:39 AM
 * Project: munch-core
 *
 * @deprecated use munch.restful.core.exception.ExceptionParser instead
 */
@Deprecated
public final class ExceptionParser {

    /**
     * @param type     to resolve
     * @param consumer to apply Exception
     */
    public static void consume(String type, Consumer<StructuredException> consumer) {
        munch.restful.core.exception.ExceptionParser.consume(type, consumer);
    }

    /**
     * Register a new type to resolve
     *
     * @param type     to resolve
     * @param function to apply Exception
     * @param <T>      to resolve to
     */
    public static <T extends StructuredException> void register(String type, Function<StructuredException, T> function) {
        munch.restful.core.exception.ExceptionParser.register(type, function);
    }

    public static void parse(Exception e) {
        munch.restful.core.exception.ExceptionParser.parse(e);
    }
}
