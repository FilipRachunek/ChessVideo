package com.brainking.tools.services;

import com.brainking.tools.dto.Game;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeRequestInitializer;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.api.services.youtube.model.VideoStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.util.Collection;
import java.util.Collections;

@Service
public class YouTubeService {

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String APPLICATION_NAME = "Chess Videos";
    @Value("${youtube.api.key}")
    private static String API_KEY;
    @Value("${youtube.client.secrets}")
    private static String CLIENT_SECRETS;
    @Value("${youtube.channel.id}")
    private static String CHANNEL_ID;
    private static final Collection<String> SCOPES = Collections.singletonList("https://www.googleapis.com/auth/youtube.upload");

    public void listChannel() {
        try {
            YouTube youTube = getClient(API_KEY);
            YouTube.Channels.List request = youTube.channels()
                    .list("snippet,contentDetails,statistics");
            ChannelListResponse response = request.setId(CHANNEL_ID).execute();
            System.out.println(response);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public String uploadVideo(Game game, String pathToVideo) {
        try {
            YouTube youTube = getClient(null);
            Video video = new Video();
            VideoSnippet snippet = new VideoSnippet();
            snippet.setChannelId("UCAYbluuAXilmrz3N_fTxO1g");
            snippet.setTitle(game.getDate() + " (" + game.getWhite() + " vs " + game.getBlack() + ")");
            snippet.setDescription("Played on BrainKing.com");
            video.setSnippet(snippet);
            VideoStatus status = new VideoStatus();
            status.setPrivacyStatus("private");
            video.setStatus(status);
            File mediaFile = new File(pathToVideo);
            InputStreamContent mediaContent =
                    new InputStreamContent("application/octet-stream",
                            new BufferedInputStream(Files.newInputStream(mediaFile.toPath())));
            mediaContent.setLength(mediaFile.length());
            YouTube.Videos.Insert request = youTube.videos()
                    .insert("snippet,status", video, mediaContent);
            Video response = request.execute();
            return response.getId();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private YouTube getClient(String apiKey) throws GeneralSecurityException, IOException {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        if (apiKey != null) {
            return new YouTube.Builder(httpTransport, JSON_FACTORY, null)
                    .setApplicationName(APPLICATION_NAME)
                    .setYouTubeRequestInitializer(new YouTubeRequestInitializer(apiKey))
                    .build();
        }
        Credential credential = authorize(httpTransport);
        return new YouTube.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    private Credential authorize(final NetHttpTransport httpTransport) throws IOException {
        InputStream inputStream = new FileInputStream(CLIENT_SECRETS);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(inputStream));
        GoogleAuthorizationCodeFlow flow =
                new GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
                        .build();
        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
    }

}
