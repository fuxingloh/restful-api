package munch.restful.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by: Fuxing
 * Date: 27/7/18
 * Time: 11:34 AM
 * Project: restful-api
 */
class KeyUtilsTest {

    @Test
    void randomMillisUUID() {
        System.out.println(KeyUtils.randomMillisUUID());
        System.out.println(KeyUtils.randomMillisUUID());
        System.out.println(KeyUtils.randomMillisUUID());
    }

    @Test
    void createUUID() {
        System.out.println(KeyUtils.createUUID(1000, 0));
    }
}