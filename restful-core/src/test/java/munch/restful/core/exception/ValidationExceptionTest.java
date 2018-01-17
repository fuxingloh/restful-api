package munch.restful.core.exception;

import org.junit.jupiter.api.Test;

/**
 * Created by: Fuxing
 * Date: 17/1/2018
 * Time: 9:54 PM
 * Project: restful-api
 */
class ValidationExceptionTest {

    @Test
    void name() throws Exception {
        ValidationException.validate(null);
    }
}