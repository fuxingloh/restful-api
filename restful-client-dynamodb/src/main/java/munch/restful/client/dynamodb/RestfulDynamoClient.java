package munch.restful.client.dynamodb;

import munch.restful.client.RestfulClient;

/**
 * Created by: Fuxing
 * Date: 6/5/18
 * Time: 2:20 PM
 * Project: restful-api
 */
public abstract class RestfulDynamoClient<T> extends RestfulClient {

    protected final Class<T> clazz;
    protected final String hashName;

    /**
     * @param url      base url, must not end with /
     * @param clazz    class for parsing
     * @param hashName primary hash name of the table
     */
    public RestfulDynamoClient(String url, Class<T> clazz, String hashName) {
        super(url);
        this.clazz = clazz;
        this.hashName = hashName;
    }
}
