package com.brainking.tools.services.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.Collection;
import java.util.Collections;

import org.springframework.stereotype.Service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeRequestInitializer;

@Service
@SuppressWarnings("PMD.LooseCoupling")
public class YouTubeCoreService {

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String APPLICATION_NAME = "Chess Videos";
    private static final Collection<String> SCOPES = Collections.singletonList("https://www.googleapis.com/auth/youtube.upload");

    public YouTube getClient(final String apiKey, final String clientSecretsFile) throws GeneralSecurityException, IOException {
        final YouTube youTube;
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        if (apiKey != null) {
            youTube = new YouTube.Builder(httpTransport, JSON_FACTORY, null)
                    .setApplicationName(APPLICATION_NAME)
                    .setYouTubeRequestInitializer(new YouTubeRequestInitializer(apiKey))
                    .build();
        } else {
            final Credential credential = authorize(httpTransport, clientSecretsFile);
            youTube = new YouTube.Builder(httpTransport, JSON_FACTORY, credential)
                    .setApplicationName(APPLICATION_NAME)
                    .build();
        }
        return youTube;
    }

    private Credential authorize(final NetHttpTransport httpTransport, final String clientSecretsFile) throws IOException {
        try (InputStream inputStream = Files.newInputStream(Path.of(clientSecretsFile))) {
            final GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(inputStream, "UTF-8"));
            final GoogleAuthorizationCodeFlow flow =
                    new GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
                            .build();
            return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
        }
    }

}
