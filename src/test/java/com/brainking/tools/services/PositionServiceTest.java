package com.brainking.tools.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.brainking.tools.dto.Color;
import com.brainking.tools.dto.Game;
import com.brainking.tools.dto.Piece;
import com.brainking.tools.dto.Position;
import com.brainking.tools.dto.Type;

class PositionServiceTest {

    private PositionService positionService;
    
    @BeforeEach
    void setUp() {
        positionService = new PositionService();
    }

    @Test
    void shouldGenerateStartPositionForChess() {
        final Game game = new Game("name");
        final Position position = positionService.generateStartPosition(game);
        assertNotNull(position, "The position must be non-null.");
        assertEquals(8, position.getPieceGrid().length, "The board has 8 rows.");
        assertEquals(8, position.getPieceGrid()[0].length, "The board has 8 columns.");
        assertEquals(Type.ROOK, position.getPieceGrid()[0][0].getType(), "A rook is on this position.");
        assertEquals(Color.WHITE, position.getPieceGrid()[0][0].getColor(), "And it's white.");
        assertEquals(Type.KNIGHT, position.getPieceGrid()[7][1].getType(), "A knight is on this position.");
        assertEquals(Color.BLACK, position.getPieceGrid()[7][1].getColor(), "And it's black.");
        for (int i = 0; i < 8; i++) {
            assertEquals(Type.PAWN, position.getPieceGrid()[1][i].getType(), "A pawn in on this position.");
            assertEquals(Color.WHITE, position.getPieceGrid()[1][i].getColor(), "And it's white.");
        }
        for (int i = 0; i < 8; i++) {
            assertEquals(Type.PAWN, position.getPieceGrid()[6][i].getType(), "A pawn is on this position.");
            assertEquals(Color.BLACK, position.getPieceGrid()[6][i].getColor(), "And it's black.");
        }
    }

    @Test
    void shouldPlacePiece() {
        final Game game = new Game("name");
        final Position position = positionService.generateStartPosition(game);
        final Piece piece = new Piece(Color.WHITE, Type.PAWN);
        position.addPiece(piece, 2, 2);
        assertEquals(340, piece.getX(), "The piece's X-coordinate is 340.");
        assertEquals(640, piece.getY(), "The piece's Y-coordinate is 640.");
    }

}
