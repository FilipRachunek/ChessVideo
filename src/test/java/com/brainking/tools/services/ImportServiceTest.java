package com.brainking.tools.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.brainking.tools.dto.Game;

class ImportServiceTest {

    private ImportService importService;

    @BeforeEach
    void setUp() {
        importService = new ImportService();
    }

    @Test
    void shouldImportPgnFile() throws IOException {
        final File file = new File(getClass().getResource("/8672028.pgn").getFile());
        assertTrue(file.exists(), "PGN file must exist.");
        final Game game = importService.importPgn(file);
        assertEquals("1-0", game.getResult(), "Result should be 1-0.");
        assertEquals("8672028", game.getName(), "Game ID and PGN file name are identical.");
        assertEquals(37, game.getMoves().size(), "Imported game contains exactly 37 moves.");
    }

}
