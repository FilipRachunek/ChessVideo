package com.brainking.tools;

import com.brainking.tools.services.EncoderService;

public final class AudioVideoTester {

    private AudioVideoTester() {
    }

    void main() {
        final EncoderService encoderService = new EncoderService();
        encoderService.addAudioToVideo(
                "x",
                "y",
                "z"
        );
    }

}
