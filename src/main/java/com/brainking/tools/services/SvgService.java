package com.brainking.tools.services;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.TranscodingHints;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.util.SVGConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SvgService {

    private static final Logger LOG = LoggerFactory.getLogger(SvgService.class);

    private BufferedImage getImageFromSvg(final InputStream svgFileStream, final int squareSize) {
        final BufferedImage[] bufferedImage = new BufferedImage[1];
        try {
            final TranscodingHints transcodingHints = getTranscodingHints(squareSize);
            final TranscoderInput input = new TranscoderInput(svgFileStream);
            final ImageTranscoder imageTranscoder = new ImageTranscoder() {
                @Override
                public BufferedImage createImage(int w, int h) {
                    return new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                }

                @Override
                public void writeImage(BufferedImage image, TranscoderOutput out) {
                    bufferedImage[0] = image;
                }
            };
            imageTranscoder.setTranscodingHints(transcodingHints);
            imageTranscoder.transcode(input, null);
        } catch (Exception ex) {
            LOG.error("Error converting the image from SVG.", ex);
        }
        return bufferedImage[0];
    }

    private TranscodingHints getTranscodingHints(final float squareSize) throws URISyntaxException {
        final URI cssUri = getClass().getResource("/css/batik-default-override-.css").toURI();
        final TranscodingHints transcodingHints = new TranscodingHints();
        transcodingHints.put(ImageTranscoder.KEY_XML_PARSER_VALIDATING, Boolean.FALSE);
        transcodingHints.put(ImageTranscoder.KEY_DOM_IMPLEMENTATION,
                SVGDOMImplementation.getDOMImplementation());
        transcodingHints.put(ImageTranscoder.KEY_DOCUMENT_ELEMENT_NAMESPACE_URI,
                SVGConstants.SVG_NAMESPACE_URI);
        transcodingHints.put(ImageTranscoder.KEY_DOCUMENT_ELEMENT, "svg");
        transcodingHints.put(ImageTranscoder.KEY_USER_STYLESHEET_URI, cssUri.toString());
        // create images for current board size, so it won't be deformed by drawImage scaling
        transcodingHints.put(ImageTranscoder.KEY_WIDTH, squareSize);
        transcodingHints.put(ImageTranscoder.KEY_HEIGHT, squareSize);
        transcodingHints.put(ImageTranscoder.KEY_ALLOW_EXTERNAL_RESOURCES, Boolean.TRUE);
        return transcodingHints;
    }

    public Map<String, BufferedImage> getBufferedImageMap(final Map<String, String> imageResourceMap, final int squareSize) {
        final Map<String, BufferedImage> map = new HashMap<>();
        for (final String key : imageResourceMap.keySet()) {
            LOG.info("Loading " + imageResourceMap.get(key));
            map.put(key, getImageFromSvg(getClass().getResourceAsStream(imageResourceMap.get(key)), squareSize));
        }
        return map;
    }

    public Map<String, String> getImageResourceMap(final String prefix) {
        final Map<String, String> map = new HashMap<>();
        map.put("K", "/images/chess/" + prefix + "King.svg");
        map.put("Q", "/images/chess/" + prefix + "Queen.svg");
        map.put("R", "/images/chess/" + prefix + "Rook.svg");
        map.put("B", "/images/chess/" + prefix + "Bishop.svg");
        map.put("N", "/images/chess/" + prefix + "Knight.svg");
        map.put("p", "/images/chess/" + prefix + "Pawn.svg");
        map.put("A", "/images/chess/" + prefix + "Archbishop.svg");
        map.put("C", "/images/chess/" + prefix + "Chancellor.svg");
        map.put("J", "/images/chess/" + prefix + "Archbishop.svg");
        return map;
    }

}
