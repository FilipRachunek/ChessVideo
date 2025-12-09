package com.brainking.tools.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.brainking.tools.dto.Game;

public class ImportServiceTest {

    private ImportService importService;

    @BeforeEach
    public void setUp() {
        importService = new ImportService();
    }

    @Test
    public void shouldImportPgnFile() throws IOException {
        File file = new File(getClass().getResource("/8672028.pgn").getFile());
        assertTrue(file.exists());
        Game game = importService.importPgn(file);
        assertEquals("1-0", game.getResult());
        assertEquals("8672028", game.getName());
        assertEquals(37, game.getMoves().size());
    }

}
