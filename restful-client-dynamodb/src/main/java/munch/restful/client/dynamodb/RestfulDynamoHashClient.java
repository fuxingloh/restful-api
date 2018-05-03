package munch.restful.client.dynamodb;

import munch.restful.client.RestfulClient;
import munch.restful.client.RestfulRequest;

import javax.annotation.Nullable;

/**
 * Created by: Fuxing
 * Date: 3/5/18
 * Time: 2:43 PM
 * Project: munch-partners
 */
public class RestfulDynamoHashClient<T> extends RestfulClient {

    protected final Class<T> clazz;
    protected final String hashName;
    protected final String path;

    /**
     * @param url      base url
     * @param clazz    class for parsing
     * @param path     path of data service, e.g. /account/{hashName}
     * @param hashName name of hash
     */
    public RestfulDynamoHashClient(String url, Class<T> clazz,
                                   String path, String hashName) {
        super(url);
        this.clazz = clazz;
        this.path = path;
        this.hashName = hashName;
    }

    /**
     * @param hash value
     * @return Object, Null = not found
     */
    @Nullable
    public T get(Object hash) {
        RestfulRequest request = doGet(path);
        request.path(hashName, hash);
        return request.asDataObject(clazz);
    }

    /**
     * @param hash value
     * @param data body value to put
     * @return Object
     */
    public T put(Object hash, T data) {
        RestfulRequest request = doPut(path);
        request.path(hashName, hash);
        request.body(data);
        return request.asDataObject(clazz);
    }

    /**
     * @param hash value
     * @return Object, Null = not found
     */
    @Nullable
    public T delete(Object hash) {
        RestfulRequest request = doDelete(path);
        request.path(hashName, hash);
        return request.asDataObject(clazz);
    }
}
