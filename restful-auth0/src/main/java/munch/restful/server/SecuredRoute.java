package munch.restful.server;

/**
 * Created by: Fuxing
 * Date: 24/11/2017
 * Time: 7:36 PM
 * Project: restful-api
 */
@FunctionalInterface
public interface SecuredRoute {

    Object handle(SecuredCall call) throws Exception;
}
