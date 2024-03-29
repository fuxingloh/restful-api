package munch.restful.server.jwt;

import com.auth0.jwt.interfaces.DecodedJWT;
import munch.restful.core.exception.AuthenticationException;
import munch.restful.server.JsonCall;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Created by: Fuxing
 * Date: 5/1/2018
 * Time: 1:32 AM
 * Project: restful-api
 */
public abstract class TokenAuthenticator<T extends AuthenticatedToken> {
    protected static final Logger logger = LoggerFactory.getLogger(TokenAuthenticator.class);

    /**
     * defaults: requires authentication
     *
     * @param call json call
     * @return AuthenticatedToken
     * @throws AuthenticationException authentication error
     */
    public T authenticate(JsonCall call) throws AuthenticationException {
        return authenticate(call, true);
    }

    /**
     * @param call     json call
     * @param requires whether authentication is optional
     * @return AuthenticatedToken
     * @throws AuthenticationException authentication error
     */
    @Nullable
    public T authenticate(JsonCall call, boolean requires) throws AuthenticationException {
        DecodedJWT decoded = call.getJWT();
        if (decoded == null) {
            if (requires) throw new AuthenticationException(403, "Forbidden");
            return null;
        }
        return authenticate(decoded);
    }

    /**
     * Optionally authenticate
     *
     * @param call json call
     * @return AuthenticatedToken
     * @throws AuthenticationException authentication error
     */
    public Optional<T> optional(JsonCall call) throws AuthenticationException {
        DecodedJWT decoded = call.getJWT();
        if (decoded == null) return Optional.empty();
        return Optional.of(authenticate(decoded));
    }

    public abstract T authenticate(DecodedJWT decodedJwt) throws AuthenticationException;

    /**
     * Force authentication and return subject
     *
     * @param call json call
     * @return subject, AKA: userId
     * @throws AuthenticationException authentication error
     */
    public String getSubject(JsonCall call) {
        return authenticate(call, true).getSubject();
    }

    /**
     * Optional authentication and return optional subject
     *
     * @param call json call
     * @return optional subject, AKA: userId
     * @throws AuthenticationException authentication error
     */
    public Optional<String> optionalSubject(JsonCall call) {
        Optional<? extends AuthenticatedToken> optional = optional(call);
        String subject = optional.map(AuthenticatedToken::getSubject).orElse(null);
        return Optional.ofNullable(subject);
    }
}
