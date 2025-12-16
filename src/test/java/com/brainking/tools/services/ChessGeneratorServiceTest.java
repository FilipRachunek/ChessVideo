package com.brainking.tools.services;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.jcodec.api.awt.AWTSequenceEncoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.brainking.tools.dto.Game;
import com.brainking.tools.dto.Position;

class ChessGeneratorServiceTest {

    private RenderService renderService;
    private PositionService positionService;
    private ImportService importService;
    private YouTubeService youTubeService;
    private EncoderService encoderService;
    private FileService fileService;

    private ChessGeneratorService chessGeneratorService;

    @BeforeEach
    void setUp() {
        renderService = mock(RenderService.class);
        positionService = mock(PositionService.class);
        importService = mock(ImportService.class);
        youTubeService = mock(YouTubeService.class);
        encoderService = mock(EncoderService.class);
        fileService = mock(FileService.class);
        chessGeneratorService = new ChessGeneratorService(renderService, positionService, importService, youTubeService, encoderService, fileService, "source", "target", false, true);
    }

    @Test
    void shouldRenderChessVideo() throws IOException {
        final String[] extensions = {"pgn"};
        final Collection<File> inputFiles = List.of(new File("file"));
        doReturn(inputFiles).when(fileService).getSourceFiles(any(File.class), eq(extensions));
        doReturn(Collections.emptyList()).when(fileService).getExistingSourceFiles(any(File.class), any(File.class));
        final Game game = new Game("name");
        game.addMetadata("White", "White");
        game.addMetadata("Black", "Black");
        game.addMetadata("Result", "1-0");
        game.setPgnCode("pgnCode");
        doReturn(game).when(importService).importPgn(any(File.class));
        final Position position = new Position(game);
        doReturn(position).when(positionService).generateStartPosition(game);
        doReturn(mock(AWTSequenceEncoder.class)).when(encoderService).createMovEncoder(anyString(), anyString());
        doReturn(mock(BufferedImage.class)).when(renderService).getRenderedImage(any(), any(), any());
        doReturn("youTubeId").when(youTubeService).uploadVideo(any(), any());
        chessGeneratorService.renderChessVideo();
        verify(encoderService).convertToMP4(anyString(), anyString());
    }

}
