package munch.restful.server;

/**
 * Created by: Fuxing
 * Date: 2019-02-12
 * Time: 21:08
 * Project: restful-api
 */
@FunctionalInterface
public interface RequestRoute<T> {

    Object handle(JsonCall call, T request);
}
