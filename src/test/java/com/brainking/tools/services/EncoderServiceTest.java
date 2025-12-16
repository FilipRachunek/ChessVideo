package com.brainking.tools.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.brainking.tools.services.utils.EncoderCoreService;

import ws.schild.jave.Encoder;

class EncoderServiceTest {

    @Mock
    private EncoderCoreService encoderCoreService;

    @Mock
    private FileService fileService;

    @InjectMocks
    private EncoderService encoderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldConvertToMP4() {
        doReturn(mock(Encoder.class)).when(encoderCoreService).createMP4Encoder();
        final String result = encoderService.convertToMP4("targetFolder", "videoName");
        assertEquals("targetFolder/videoName.mp4", result, "The converted video path should match the expected format.");
    }

}
