package com.brainking.tools;

import com.brainking.tools.services.EncoderService;
import com.brainking.tools.services.FileService;
import com.brainking.tools.services.utils.EncoderCoreService;

public final class AudioVideoTester {

    private AudioVideoTester() {
    }

    void main() {
        final EncoderService encoderService = new EncoderService(new EncoderCoreService(), new FileService());
        encoderService.addAudioToVideo(
                "x",
                "y",
                "z"
        );
    }

}
