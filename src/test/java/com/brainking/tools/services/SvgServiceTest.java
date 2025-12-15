package com.brainking.tools.services;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.image.BufferedImage;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SvgServiceTest {

    private SvgService svgService;

    @BeforeEach
    void setUp() {
        svgService = new SvgService();
    }

    @Test
    void shouldReturnImageResourceMap() {
        final String prefix = "White";
        final Map<String, String> map = svgService.getImageResourceMap(prefix);
        assertEquals(9, map.size(), "Map should contain 9 entries.");
        assertEquals("/images/chess/WhiteKing.svg", map.get("K"), "The entries should have the correct prefix.");
    }

    @Test
    void shouldReturnBufferedImageMap() {
        final Map<String, String> map = Map.of("K", "/WhiteKing.svg");
        final Map<String, BufferedImage> imageMap = svgService.getBufferedImageMap(map, 10);
        assertEquals(1, imageMap.size(), "Map should contain 1 entry.");
    }

}
