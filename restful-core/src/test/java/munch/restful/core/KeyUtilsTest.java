package munch.restful.core;

import org.junit.jupiter.api.Test;

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
        System.out.println(KeyUtils.createUUID(1000, Long.MIN_VALUE));
        System.out.println(KeyUtils.createUUID(1000, Long.MAX_VALUE));
    }

    @Test
    void createI4() {
        System.out.println(KeyUtils.createUUID(0, -100, 0, Integer.MIN_VALUE));
        System.out.println(KeyUtils.createUUID(0, -100, 0, Integer.MAX_VALUE));
        System.out.println(KeyUtils.createUUID(1000, 0, 2043, 0));

        System.out.println(KeyUtils.createUUID(0, 0, 0, Integer.MAX_VALUE));
        System.out.println(KeyUtils.createUUID(0, 0, 0, Integer.MIN_VALUE));
        System.out.println(Integer.MIN_VALUE + 2);
        System.out.println(KeyUtils.createUUID(0, 0, 0, Integer.MIN_VALUE + 2));
    }
}