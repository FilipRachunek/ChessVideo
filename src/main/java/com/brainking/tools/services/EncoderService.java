package com.brainking.tools.services;

import com.brainking.tools.utils.Constants;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ws.schild.jave.Encoder;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;
import ws.schild.jave.encode.VideoAttributes;
import ws.schild.jave.encode.enums.X264_PROFILE;
import ws.schild.jave.info.VideoSize;

import java.io.File;
import java.util.Arrays;

@Service
public class EncoderService {

    private static final Logger LOG = LoggerFactory.getLogger(EncoderService.class);

    public String convertToMP4(String targetFolder, String videoName) {
        try {
            File source = new File(targetFolder, videoName + ".mov");
            File target = new File(targetFolder, videoName + ".mp4");
            VideoAttributes video = new VideoAttributes();
            video.setCodec("h264");
            video.setX264Profile(X264_PROFILE.MAIN);
            video.setFrameRate(25);
            video.setSize(new VideoSize(Constants.VIDEO_WIDTH, Constants.VIDEO_HEIGHT));
            EncodingAttributes attrs = new EncodingAttributes();
            attrs.setOutputFormat("mp4");
            attrs.setVideoAttributes(video);
            Encoder encoder = new Encoder();
            encoder.encode(new MultimediaObject(source), target, attrs);
            FileUtils.forceDelete(source);
            return targetFolder + "/" + videoName + ".mp4";
        } catch (Exception ex) {
            LOG.error("Error converting the video to MP4.", ex);
            return null;
        }
    }

    public void addAudioToVideo(String audioPath, String videoPath, String targetPath) {
        try {
            File audio = new File(audioPath);
            File video = new File(videoPath);
            File target = new File(targetPath);
            AudioAttributes audioAttributes = new AudioAttributes();
            VideoAttributes videoAttributes = new VideoAttributes();
            videoAttributes.setCodec("h264");
            videoAttributes.setX264Profile(X264_PROFILE.MAIN);
            videoAttributes.setFrameRate(25);
            videoAttributes.setSize(new VideoSize(Constants.VIDEO_WIDTH, Constants.VIDEO_HEIGHT));
            EncodingAttributes attrs = new EncodingAttributes();
            attrs.setOutputFormat("mp4");
            attrs.setAudioAttributes(audioAttributes);
            attrs.setVideoAttributes(videoAttributes);
            Encoder encoder = new Encoder();
            encoder.encode(Arrays.asList(new MultimediaObject(audio), new MultimediaObject(video)), target, attrs);
        } catch (Exception ex) {
            LOG.error("Error adding audio to the video.", ex);
        }
    }

}
