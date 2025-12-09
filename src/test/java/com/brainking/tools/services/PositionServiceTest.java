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

public class PositionServiceTest {

    private PositionService positionService;
    
    @BeforeEach
    public void setUp() {
        positionService = new PositionService();
    }

    @Test
    public void shouldGenerateStartPositionForChess() {
        Game game = new Game("name");
        Position position = positionService.generateStartPosition(game);
        assertNotNull(position);
        assertEquals(8, position.getPieceGrid().length);
        assertEquals(8, position.getPieceGrid()[0].length);
        assertEquals(Type.ROOK, position.getPieceGrid()[0][0].getType());
        assertEquals(Color.WHITE, position.getPieceGrid()[0][0].getColor());
        assertEquals(Type.KNIGHT, position.getPieceGrid()[7][1].getType());
        assertEquals(Color.BLACK, position.getPieceGrid()[7][1].getColor());
        for (int i = 0; i < 8; i++) {
            assertEquals(Type.PAWN, position.getPieceGrid()[1][i].getType());
            assertEquals(Color.WHITE, position.getPieceGrid()[1][i].getColor());
        }
        for (int i = 0; i < 8; i++) {
            assertEquals(Type.PAWN, position.getPieceGrid()[6][i].getType());
            assertEquals(Color.BLACK, position.getPieceGrid()[6][i].getColor());
        }
    }

    @Test
    public void shouldPlacePiece() {
        Game game = new Game("name");
        Position position = positionService.generateStartPosition(game);
        Piece piece = new Piece(Color.WHITE, Type.PAWN);
        position.addPiece(piece, 2, 2);
        assertEquals(340, piece.getX());
        assertEquals(640, piece.getY());
    }

}
