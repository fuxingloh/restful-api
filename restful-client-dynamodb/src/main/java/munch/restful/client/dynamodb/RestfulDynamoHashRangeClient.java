package munch.restful.client.dynamodb;

import munch.restful.client.RestfulRequest;

import javax.annotation.Nullable;
import java.util.Iterator;

/**
 * Created by: Fuxing
 * Date: 3/5/18
 * Time: 2:43 PM
 * Project: munch-partners
 */
public class RestfulDynamoHashRangeClient<T> extends RestfulDynamoClient<T> {

    protected final String rangeName;

    /**
     * @param url       base url, must not end with /
     * @param clazz     class for parsing
     * @param hashName  name of hash
     * @param rangeName name of range
     */
    public RestfulDynamoHashRangeClient(String url, Class<T> clazz, String hashName, String rangeName) {
        super(url, clazz, hashName);
        this.rangeName = rangeName;
    }

    /**
     * @param path      for querying list, e.g. /resources/{hash}
     * @param hash      value
     * @param nextRange value
     * @param size      per list
     * @return PagedList of Data or Empty
     */
    public NextNodeList<T> list(String path, Object hash, @Nullable Object nextRange, int size) {
        return list(path, hashName, hash, rangeName, nextRange, size);
    }

    /**
     * @param path for querying list, e.g. /resources/{hash}
     * @param hash value
     * @param size default size per list
     * @return Iterator of all Data with the same hash
     */
    public Iterator<T> list(String path, Object hash, int size) {
        return list(path, hashName, hash, rangeName, size);
    }

    /**
     * @param path  for get request, e.g. /resources/{hash}/resources/{range}
     * @param hash  value
     * @param range value
     * @return Object, Null = not found
     */
    @Nullable
    public T get(String path, Object hash, Object range) {
        RestfulRequest request = doGet(path);
        request.path(hashName, hash);
        request.path(rangeName, range);
        return request.asDataObject(clazz);
    }

    /**
     * @param path  for put request, e.g. /resources/{hash}/resources/{range}
     * @param hash  value
     * @param range value
     * @param data  body value to put
     * @return Object
     */
    public T put(String path, Object hash, Object range, T data) {
        RestfulRequest request = doPut(path);
        request.path(hashName, hash);
        request.path(rangeName, range);
        request.body(data);
        return request.asDataObject(clazz);
    }

    /**
     * @param path  for delete request, e.g. /resources/{hash}/resources/{range}
     * @param hash  value
     * @param range value
     * @return Object, Null = not found
     */
    @Nullable
    public T delete(String path, Object hash, Object range) {
        RestfulRequest request = doDelete(path);
        request.path(hashName, hash);
        request.path(rangeName, range);
        return request.asDataObject(clazz);
    }
}