package com.brainking.tools;

import com.brainking.tools.dto.Game;
import com.brainking.tools.services.YouTubeService;

public final class YouTubeTester {

    private YouTubeTester() {
    }

    void main() {
        final YouTubeService youTubeService = new YouTubeService("x", "y", "z");
        youTubeService.listChannel();
        final Game game = new Game("TestGame");
        game.addMetadata("White", "White");
        game.addMetadata("Black", "Black");
        game.addMetadata("Result", "1-0");
        final String pathToVideo = "/Volumes/Samsung_T5/Video/Generated/2479782.mp4";
        youTubeService.uploadVideo(game, pathToVideo);

    }

}
