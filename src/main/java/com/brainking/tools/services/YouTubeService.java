package com.brainking.tools.services;

import com.brainking.tools.dto.Game;
import com.brainking.tools.services.utils.YouTubeCoreService;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.youtube.YouTube;
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
import java.io.IOException;
import java.nio.file.Files;
import java.security.GeneralSecurityException;

@Service
@SuppressWarnings("PMD.LooseCoupling")
public class YouTubeService {

    private static final Logger LOG = LoggerFactory.getLogger(YouTubeService.class);

    private final YouTubeCoreService youTubeCoreService;
    private final String apiKey;
    private final String clientSecretsFile;
    private final String channelId;

    public YouTubeService(final YouTubeCoreService youTubeCoreService,
                        @Value("${youtube.api.key}") final String apiKey,
                        @Value("${youtube.client.secrets.file}") final String clientSecretsFile,
                        @Value("${youtube.channel.id}") final String channelId) {
        this.youTubeCoreService = youTubeCoreService;
        this.apiKey = apiKey;
        this.clientSecretsFile = clientSecretsFile;
        this.channelId = channelId;
    }

    public void listChannel() {
        try {
            final YouTube youTube = youTubeCoreService.getClient(apiKey, clientSecretsFile);
            final YouTube.Channels.List request = youTube.channels()
                    .list("snippet,contentDetails,statistics");
            final ChannelListResponse response = request.setId(channelId).execute();
            LOG.info(response.toString());
        } catch (GeneralSecurityException | IOException ex) {
            LOG.error("Error listing the YouTube channel.", ex);
        }
    }

    public String uploadVideo(final Game game, final String pathToVideo) {
        String videoId = "";
        try {
            final YouTube youTube = youTubeCoreService.getClient(null, clientSecretsFile);
            final Video video = new Video();
            final VideoSnippet snippet = new VideoSnippet();
            snippet.setChannelId(channelId);
            snippet.setTitle(game.getDate() + " (" + game.getWhite() + " vs " + game.getBlack() + ")");
            snippet.setDescription("Played on BrainKing.com");
            video.setSnippet(snippet);
            final VideoStatus status = new VideoStatus();
            status.setPrivacyStatus("private");
            video.setStatus(status);
            final File mediaFile = new File(pathToVideo);
            final InputStreamContent mediaContent =
                    new InputStreamContent("application/octet-stream",
                            new BufferedInputStream(Files.newInputStream(mediaFile.toPath())));
            mediaContent.setLength(mediaFile.length());
            final YouTube.Videos.Insert request = youTube.videos()
                    .insert("snippet,status", video, mediaContent);
            final Video response = request.execute();
            videoId = response.getId();
        } catch (GeneralSecurityException | IOException ex) {
            LOG.error("Error uploading the video to YouTube.", ex);
        }
        return videoId;
    }

}
