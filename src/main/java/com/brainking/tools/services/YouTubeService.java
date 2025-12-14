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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger LOG = LoggerFactory.getLogger(YouTubeService.class);

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String APPLICATION_NAME = "Chess Videos";
    private static final Collection<String> SCOPES = Collections.singletonList("https://www.googleapis.com/auth/youtube.upload");
    private final String apiKey;
    private final String clientSecretsFile;
    private final String channelId;

    public YouTubeService(@Value("${youtube.api.key}") String apiKey,
                        @Value("${youtube.client.secrets.file}") String clientSecretsFile,
                        @Value("${youtube.channel.id}") String channelId) {
        this.apiKey = apiKey;
        this.clientSecretsFile = clientSecretsFile;
        this.channelId = channelId;
    }

    public void listChannel() {
        try {
            YouTube youTube = getClient(apiKey);
            YouTube.Channels.List request = youTube.channels()
                    .list("snippet,contentDetails,statistics");
            ChannelListResponse response = request.setId(channelId).execute();
            LOG.info(response.toString());
        } catch (Exception ex) {
            LOG.error("Error listing the YouTube channel.", ex);
        }
    }

    public String uploadVideo(Game game, String pathToVideo) {
        try {
            YouTube youTube = getClient(null);
            Video video = new Video();
            VideoSnippet snippet = new VideoSnippet();
            snippet.setChannelId(channelId);
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
            LOG.error("Error uploading the video to YouTube.", ex);
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
        InputStream inputStream = new FileInputStream(clientSecretsFile);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(inputStream));
        GoogleAuthorizationCodeFlow flow =
                new GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
                        .build();
        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
    }

}
