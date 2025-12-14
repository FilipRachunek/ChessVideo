package com.brainking.tools;

import com.brainking.tools.services.EncoderService;

public final class AudioVideoTester {

    private AudioVideoTester() {
    }

    void main() {
        final EncoderService encoderService = new EncoderService();
        encoderService.addAudioToVideo(
                "/Volumes/Samsung_T5/Albums/Singles/Path To Redemption.wav",
                "/Volumes/Samsung_T5/Video/Generated/8846341.mp4",
                "/Users/filiprachunek/Chess/ChessAndAudio.mp4"
        );
    }

}
