package munch.restful.server.firebase;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.firebase.auth.FirebaseToken;
import munch.restful.server.jwt.AuthenticatedToken;

/**
 * Created by: Fuxing
 * Date: 8/2/18
 * Time: 4:14 PM
 * Project: restful-api
 */
public final class FirebaseAuthenticatedToken extends AuthenticatedToken {
    private final FirebaseToken firebaseToken;

    public FirebaseAuthenticatedToken(DecodedJWT decodedJWT, FirebaseToken firebaseToken) {
        super(decodedJWT);
        this.firebaseToken = firebaseToken;
    }

    public FirebaseToken getFirebaseToken() {
        return firebaseToken;
    }
}
