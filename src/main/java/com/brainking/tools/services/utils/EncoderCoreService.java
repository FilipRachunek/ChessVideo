package com.brainking.tools.services.utils;

import java.io.File;
import java.io.IOException;

import org.jcodec.api.awt.AWTSequenceEncoder;
import org.springframework.stereotype.Service;

import ws.schild.jave.Encoder;

@Service
public class EncoderCoreService {

    public AWTSequenceEncoder createMovEncoder(final String videoFolder, final String videoName) throws IOException {
        return AWTSequenceEncoder.create25Fps(new File(videoFolder, videoName + ".mov"));
    }

    public Encoder createMP4Encoder() {
        return new Encoder();
    }

}
