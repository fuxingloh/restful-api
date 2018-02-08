package munch.restful.server.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import munch.restful.server.jwt.TokenAuthenticator;

import javax.inject.Singleton;

/**
 * Created by: Fuxing
 * Date: 8/2/18
 * Time: 3:16 PM
 * Project: restful-api
 */
public final class FirebaseAuthenticationModule extends AbstractModule {

    private final String projectId;
    private final GoogleCredentials credentials;

    public FirebaseAuthenticationModule(String projectId, GoogleCredentials credentials) {
        this.projectId = projectId;
        this.credentials = credentials;
    }

    @Override
    protected void configure() {
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(credentials)
                .setDatabaseUrl("https://" + projectId + ".firebaseio.com")
                .build();

        FirebaseApp.initializeApp(options);

        bind(TokenAuthenticator.class).to(FirebaseAuthenticator.class);
    }

    @Provides
    @Singleton
    FirebaseAuthenticator provideAuthenticator() {
        return new FirebaseAuthenticator();
    }
}
