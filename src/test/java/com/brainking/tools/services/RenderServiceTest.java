package com.brainking.tools.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doReturn;

import java.awt.image.BufferedImage;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class RenderServiceTest {

    @Mock
    private SvgService svgService;

    @InjectMocks
    private RenderService renderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldReturnImageMap() {
        final String prefix = "White";
        final BufferedImage image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
        final Map<String, String> imageResourceMap = Map.of("K", "/WhiteKing.svg");
        final Map<String, BufferedImage> bufferedImageMap = Map.of("K", image);
        doReturn(imageResourceMap).when(svgService).getImageResourceMap(prefix);
        doReturn(bufferedImageMap).when(svgService).getBufferedImageMap(imageResourceMap, 10);
        final Map<String, BufferedImage> result = renderService.getBufferedImageMap(prefix, 10);
        assertEquals(1, result.size(), "Map should contain 1 entry.");
        assertNotNull(result.get("K"), "The map should contain BufferedImage.");
    }

}
