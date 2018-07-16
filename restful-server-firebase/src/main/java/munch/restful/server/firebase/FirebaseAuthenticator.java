package munch.restful.server.firebase;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import munch.restful.core.exception.AuthenticationException;
import munch.restful.server.jwt.TokenAuthenticator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;

/**
 * Created by: Fuxing
 * Date: 5/1/2018
 * Time: 1:32 AM
 * Project: restful-api
 */
public final class FirebaseAuthenticator extends TokenAuthenticator<FirebaseAuthenticatedToken> {
    private static final Logger logger = LoggerFactory.getLogger(FirebaseAuthenticator.class);

    private FirebaseAuth getAuth() {
        return FirebaseAuth.getInstance();
    }

    @Override
    public FirebaseAuthenticatedToken authenticate(DecodedJWT decodedJwt) throws AuthenticationException {
        try {
            FirebaseToken decodedToken = getAuth().verifyIdTokenAsync(decodedJwt.getToken()).get();
            return new FirebaseAuthenticatedToken(decodedJwt, decodedToken);
        } catch (InterruptedException | ExecutionException e) {
            throw new AuthenticationException("Authentication get interrupted.");
        } catch (Exception e) {
            logger.warn("Unknown Authentication Exception", e);
            throw new AuthenticationException("Unknown Authentication Exception");
        }
    }
}
