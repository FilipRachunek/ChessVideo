package com.brainking.tools.services;

import com.brainking.tools.utils.Constants;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ws.schild.jave.Encoder;
import ws.schild.jave.EncoderException;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;
import ws.schild.jave.encode.VideoAttributes;
import ws.schild.jave.encode.enums.X264_PROFILE;
import ws.schild.jave.info.VideoSize;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

@Service
public class EncoderService {

    private static final Logger LOG = LoggerFactory.getLogger(EncoderService.class);

    public String convertToMP4(final String targetFolder, final String videoName) {
        String result = "";
        try {
            final File source = new File(targetFolder, videoName + ".mov");
            final File target = new File(targetFolder, videoName + ".mp4");
            final VideoAttributes video = new VideoAttributes();
            video.setCodec("h264");
            video.setX264Profile(X264_PROFILE.MAIN);
            video.setFrameRate(25);
            video.setSize(new VideoSize(Constants.VIDEO_WIDTH, Constants.VIDEO_HEIGHT));
            final EncodingAttributes attrs = new EncodingAttributes();
            attrs.setOutputFormat("mp4");
            attrs.setVideoAttributes(video);
            final Encoder encoder = new Encoder();
            encoder.encode(new MultimediaObject(source), target, attrs);
            FileUtils.forceDelete(source);
            result = targetFolder + "/" + videoName + ".mp4";
        } catch (EncoderException | IOException ex) {
            LOG.error("Error converting the video to MP4.", ex);
        }
        return result;
    }

    public void addAudioToVideo(final String audioPath, final String videoPath, final String targetPath) {
        try {
            final File audio = new File(audioPath);
            final File video = new File(videoPath);
            final File target = new File(targetPath);
            final AudioAttributes audioAttributes = new AudioAttributes();
            final VideoAttributes videoAttributes = new VideoAttributes();
            videoAttributes.setCodec("h264");
            videoAttributes.setX264Profile(X264_PROFILE.MAIN);
            videoAttributes.setFrameRate(25);
            videoAttributes.setSize(new VideoSize(Constants.VIDEO_WIDTH, Constants.VIDEO_HEIGHT));
            final EncodingAttributes attrs = new EncodingAttributes();
            attrs.setOutputFormat("mp4");
            attrs.setAudioAttributes(audioAttributes);
            attrs.setVideoAttributes(videoAttributes);
            final Encoder encoder = new Encoder();
            encoder.encode(Arrays.asList(new MultimediaObject(audio), new MultimediaObject(video)), target, attrs);
        } catch (EncoderException ex) {
            LOG.error("Error adding audio to the video.", ex);
        }
    }

}
