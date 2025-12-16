package com.brainking.tools.services.utils;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EncoderCoreServiceTest {

    private EncoderCoreService encoderCoreService;

    @BeforeEach
    void setUp() {
        encoderCoreService = new EncoderCoreService();
    }

    @Test
    void shouldCreateMP4Encoder() {
        assertNotNull(encoderCoreService.createMP4Encoder(), "The MP4 encoder should be created successfully.");
    }

}
