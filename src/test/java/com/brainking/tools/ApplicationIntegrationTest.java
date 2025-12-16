package com.brainking.tools;

import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.brainking.tools.services.ChessGeneratorService;

@SpringBootTest
class ApplicationIntegrationTest {

    @MockitoBean
    private ChessGeneratorService chessGeneratorService;

    @Test
    void shouldInvokeRenderChessVideo() {
        verify(chessGeneratorService, timeout(500).times(1)).renderChessVideo();
    }

}
