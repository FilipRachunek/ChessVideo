package com.brainking.tools.services;

import com.brainking.tools.dto.Color;
import com.brainking.tools.dto.Game;
import com.brainking.tools.dto.Piece;
import com.brainking.tools.dto.Position;
import com.brainking.tools.dto.Type;
import com.brainking.tools.utils.Constants;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PositionService {

    private static final Logger log = LoggerFactory.getLogger(PositionService.class);

    public Position generateStartPosition(Game game) {
        Position position = new Position(game);
        String fen = game.getFEN();
        if (StringUtils.isNotBlank(fen)) {
            addPositionFromFEN(game, position, fen);
        } else if (game.isVariant(Constants.RACING_KINGS)) {
            addRacingKingsPosition(position);
        } else if (game.isVariant(Constants.KNIGHTMATE)) {
            addKnightmateChessPosition(position);
        } else if (game.isVariant(Constants.HORDE)) {
            addHordeChessPosition(position);
        } else if (game.isVariant(Constants.ICE_AGE)) {
            addIceAgeChessPosition(position);
        } else if (game.isVariant(Constants.LOS_ALAMOS)) {
            addLosAlamosPosition(position);
        } else if (game.isVariant(Constants.LEGAN)) {
            addLeganPosition(position);
        } else if (game.isVariant(Constants.GRAND)) {
            addGrandPosition(position);
        } else {
            addStandardChessPosition(position);
        }
        if (game.isVariant(Constants.DARK)) {
            position.resetVisibleGrid();
        }
        return position;
    }

    private void addPositionFromFEN(Game game, Position position, String fen) {
        // Example: kqnbbrrn/pppppppp/ppp5/8/8/5PPP/PPPPPPPP/NRRBBNQK w KQkq - 0 1
        log.info("Generating the position from FEN: " + fen);
        String[] rowArray = fen.split(" ")[0].split("/");
        for (int row = 7; row >= 0; row--) {
            int column = 0;
            String codes = rowArray[7 - row];
            for (int i = 0; i < codes.length(); i++) {
                char code = codes.charAt(i);
                String codeString = String.valueOf(code);
                if (Character.isDigit(code)) {
                    column += Integer.parseInt(codeString);
                } else if (Character.isUpperCase(code)) {
                    if (game.isVariant(Constants.EMBASSY)) {
                        if (codeString.equals("M")) {
                            codeString = "C";
                        } else if (codeString.equals("C")) {
                            codeString = "A";
                        }
                    }
                    position.addPiece(new Piece(Color.WHITE, Type.findByCode(codeString)), row, column);
                    column++;
                } else if (Character.isLowerCase(code)) {
                    if (game.isVariant(Constants.EMBASSY)) {
                        if (codeString.equals("m")) {
                            codeString = "c";
                        } else if (codeString.equals("c")) {
                            codeString = "a";
                        }
                    }
                    position.addPiece(new Piece(Color.BLACK, Type.findByCode(codeString)), row, column);
                    column++;
                }
            }
        }
    }

    private void addStandardChessPosition(Position position) {
        position.addPiece(new Piece(Color.WHITE, Type.ROOK), 0, 0);
        position.addPiece(new Piece(Color.WHITE, Type.KNIGHT), 0, 1);
        position.addPiece(new Piece(Color.WHITE, Type.BISHOP), 0, 2);
        position.addPiece(new Piece(Color.WHITE, Type.QUEEN), 0, 3);
        position.addPiece(new Piece(Color.WHITE, Type.KING), 0, 4);
        position.addPiece(new Piece(Color.WHITE, Type.BISHOP), 0, 5);
        position.addPiece(new Piece(Color.WHITE, Type.KNIGHT), 0, 6);
        position.addPiece(new Piece(Color.WHITE, Type.ROOK), 0, 7);
        for (int column = 0; column < 8; column++) {
            position.addPiece(new Piece(Color.WHITE, Type.PAWN), 1, column);
        }
        position.addPiece(new Piece(Color.BLACK, Type.ROOK), 7, 0);
        position.addPiece(new Piece(Color.BLACK, Type.KNIGHT), 7, 1);
        position.addPiece(new Piece(Color.BLACK, Type.BISHOP), 7, 2);
        position.addPiece(new Piece(Color.BLACK, Type.QUEEN), 7, 3);
        position.addPiece(new Piece(Color.BLACK, Type.KING), 7, 4);
        position.addPiece(new Piece(Color.BLACK, Type.BISHOP), 7, 5);
        position.addPiece(new Piece(Color.BLACK, Type.KNIGHT), 7, 6);
        position.addPiece(new Piece(Color.BLACK, Type.ROOK), 7, 7);
        for (int column = 0; column < 8; column++) {
            position.addPiece(new Piece(Color.BLACK, Type.PAWN), 6, column);
        }
    }

    private void addRacingKingsPosition(Position position) {
        position.addPiece(new Piece(Color.BLACK, Type.KING), 1, 0);
        position.addPiece(new Piece(Color.BLACK, Type.QUEEN), 0, 0);
        position.addPiece(new Piece(Color.BLACK, Type.ROOK), 1, 1);
        position.addPiece(new Piece(Color.BLACK, Type.ROOK), 0, 1);
        position.addPiece(new Piece(Color.BLACK, Type.BISHOP), 1, 2);
        position.addPiece(new Piece(Color.BLACK, Type.BISHOP), 0, 2);
        position.addPiece(new Piece(Color.BLACK, Type.KNIGHT), 1, 3);
        position.addPiece(new Piece(Color.BLACK, Type.KNIGHT), 0, 3);
        position.addPiece(new Piece(Color.WHITE, Type.KNIGHT), 1, 4);
        position.addPiece(new Piece(Color.WHITE, Type.KNIGHT), 0, 4);
        position.addPiece(new Piece(Color.WHITE, Type.BISHOP), 1, 5);
        position.addPiece(new Piece(Color.WHITE, Type.BISHOP), 0, 5);
        position.addPiece(new Piece(Color.WHITE, Type.ROOK), 1, 6);
        position.addPiece(new Piece(Color.WHITE, Type.ROOK), 0, 6);
        position.addPiece(new Piece(Color.WHITE, Type.KING), 1, 7);
        position.addPiece(new Piece(Color.WHITE, Type.QUEEN), 0, 7);
    }

    private void addKnightmateChessPosition(Position position) {
        position.addPiece(new Piece(Color.WHITE, Type.ROOK), 0, 0);
        position.addPiece(new Piece(Color.WHITE, Type.KING), 0, 1);
        position.addPiece(new Piece(Color.WHITE, Type.BISHOP), 0, 2);
        position.addPiece(new Piece(Color.WHITE, Type.QUEEN), 0, 3);
        position.addPiece(new Piece(Color.WHITE, Type.KNIGHT), 0, 4);
        position.addPiece(new Piece(Color.WHITE, Type.BISHOP), 0, 5);
        position.addPiece(new Piece(Color.WHITE, Type.KING), 0, 6);
        position.addPiece(new Piece(Color.WHITE, Type.ROOK), 0, 7);
        for (int column = 0; column < 8; column++) {
            position.addPiece(new Piece(Color.WHITE, Type.PAWN), 1, column);
        }
        position.addPiece(new Piece(Color.BLACK, Type.ROOK), 7, 0);
        position.addPiece(new Piece(Color.BLACK, Type.KING), 7, 1);
        position.addPiece(new Piece(Color.BLACK, Type.BISHOP), 7, 2);
        position.addPiece(new Piece(Color.BLACK, Type.QUEEN), 7, 3);
        position.addPiece(new Piece(Color.BLACK, Type.KNIGHT), 7, 4);
        position.addPiece(new Piece(Color.BLACK, Type.BISHOP), 7, 5);
        position.addPiece(new Piece(Color.BLACK, Type.KING), 7, 6);
        position.addPiece(new Piece(Color.BLACK, Type.ROOK), 7, 7);
        for (int column = 0; column < 8; column++) {
            position.addPiece(new Piece(Color.BLACK, Type.PAWN), 6, column);
        }
    }

    private void addHordeChessPosition(Position position) {
        position.addPiece(new Piece(Color.WHITE, Type.ROOK), 0, 0);
        position.addPiece(new Piece(Color.WHITE, Type.KNIGHT), 0, 1);
        position.addPiece(new Piece(Color.WHITE, Type.BISHOP), 0, 2);
        position.addPiece(new Piece(Color.WHITE, Type.QUEEN), 0, 3);
        position.addPiece(new Piece(Color.WHITE, Type.KING), 0, 4);
        position.addPiece(new Piece(Color.WHITE, Type.BISHOP), 0, 5);
        position.addPiece(new Piece(Color.WHITE, Type.KNIGHT), 0, 6);
        position.addPiece(new Piece(Color.WHITE, Type.ROOK), 0, 7);
        for (int column = 0; column < 8; column++) {
            position.addPiece(new Piece(Color.WHITE, Type.PAWN), 1, column);
        }
        position.addPiece(new Piece(Color.BLACK, Type.PAWN), 7, 0);
        position.addPiece(new Piece(Color.BLACK, Type.PAWN), 7, 1);
        position.addPiece(new Piece(Color.BLACK, Type.PAWN), 7, 2);
        position.addPiece(new Piece(Color.BLACK, Type.PAWN), 7, 5);
        position.addPiece(new Piece(Color.BLACK, Type.PAWN), 7, 6);
        position.addPiece(new Piece(Color.BLACK, Type.PAWN), 7, 7);
        for (int column = 0; column < 8; column++) {
            position.addPiece(new Piece(Color.BLACK, Type.PAWN), 6, column);
            position.addPiece(new Piece(Color.BLACK, Type.PAWN), 5, column);
            position.addPiece(new Piece(Color.BLACK, Type.PAWN), 4, column);
        }
        position.addPiece(new Piece(Color.BLACK, Type.PAWN), 3, 3);
        position.addPiece(new Piece(Color.BLACK, Type.PAWN), 3, 4);
    }

    private void addIceAgeChessPosition(Position position) {
        addStandardChessPosition(position);
        for (int row = 2; row < 6; row++) {
            for (int column = 0; column < 8; column++) {
                position.addPiece(new Piece(Type.ICE_CUBE), row, column);
            }
        }
    }

    private void addLosAlamosPosition(Position position) {
        position.addPiece(new Piece(Color.WHITE, Type.ROOK), 0, 0);
        position.addPiece(new Piece(Color.WHITE, Type.KNIGHT), 0, 1);
        position.addPiece(new Piece(Color.WHITE, Type.QUEEN), 0, 2);
        position.addPiece(new Piece(Color.WHITE, Type.KING), 0, 3);
        position.addPiece(new Piece(Color.WHITE, Type.KNIGHT), 0, 4);
        position.addPiece(new Piece(Color.WHITE, Type.ROOK), 0, 5);
        for (int column = 0; column < 6; column++) {
            position.addPiece(new Piece(Color.WHITE, Type.PAWN), 1, column);
        }
        position.addPiece(new Piece(Color.BLACK, Type.ROOK), 5, 0);
        position.addPiece(new Piece(Color.BLACK, Type.KNIGHT), 5, 1);
        position.addPiece(new Piece(Color.BLACK, Type.QUEEN), 5, 2);
        position.addPiece(new Piece(Color.BLACK, Type.KING), 5, 3);
        position.addPiece(new Piece(Color.BLACK, Type.KNIGHT), 5, 4);
        position.addPiece(new Piece(Color.BLACK, Type.ROOK), 5, 5);
        for (int column = 0; column < 6; column++) {
            position.addPiece(new Piece(Color.BLACK, Type.PAWN), 4, column);
        }
    }

    private void addLeganPosition(Position position) {
        position.addPiece(new Piece(Color.WHITE, Type.PAWN), 0, 3);
        position.addPiece(new Piece(Color.WHITE, Type.ROOK), 0, 4);
        position.addPiece(new Piece(Color.WHITE, Type.BISHOP), 0, 5);
        position.addPiece(new Piece(Color.WHITE, Type.KNIGHT), 0, 6);
        position.addPiece(new Piece(Color.WHITE, Type.KING), 0, 7);
        position.addPiece(new Piece(Color.WHITE, Type.PAWN), 1, 4);
        position.addPiece(new Piece(Color.WHITE, Type.PAWN), 1, 5);
        position.addPiece(new Piece(Color.WHITE, Type.QUEEN), 1, 6);
        position.addPiece(new Piece(Color.WHITE, Type.BISHOP), 1, 7);
        position.addPiece(new Piece(Color.WHITE, Type.PAWN), 2, 5);
        position.addPiece(new Piece(Color.WHITE, Type.PAWN), 2, 6);
        position.addPiece(new Piece(Color.WHITE, Type.KNIGHT), 2, 7);
        position.addPiece(new Piece(Color.WHITE, Type.PAWN), 3, 4);
        position.addPiece(new Piece(Color.WHITE, Type.PAWN), 3, 6);
        position.addPiece(new Piece(Color.WHITE, Type.ROOK), 3, 7);
        position.addPiece(new Piece(Color.WHITE, Type.PAWN), 4, 7);
        position.addPiece(new Piece(Color.BLACK, Type.PAWN), 3, 0);
        position.addPiece(new Piece(Color.BLACK, Type.ROOK), 4, 0);
        position.addPiece(new Piece(Color.BLACK, Type.PAWN), 4, 1);
        position.addPiece(new Piece(Color.BLACK, Type.PAWN), 4, 3);
        position.addPiece(new Piece(Color.BLACK, Type.KNIGHT), 5, 0);
        position.addPiece(new Piece(Color.BLACK, Type.PAWN), 5, 1);
        position.addPiece(new Piece(Color.BLACK, Type.PAWN), 5, 2);
        position.addPiece(new Piece(Color.BLACK, Type.BISHOP), 6, 0);
        position.addPiece(new Piece(Color.BLACK, Type.QUEEN), 6, 1);
        position.addPiece(new Piece(Color.BLACK, Type.PAWN), 6, 2);
        position.addPiece(new Piece(Color.BLACK, Type.PAWN), 6, 3);
        position.addPiece(new Piece(Color.BLACK, Type.KING), 7, 0);
        position.addPiece(new Piece(Color.BLACK, Type.KNIGHT), 7, 1);
        position.addPiece(new Piece(Color.BLACK, Type.BISHOP), 7, 2);
        position.addPiece(new Piece(Color.BLACK, Type.ROOK), 7, 3);
        position.addPiece(new Piece(Color.BLACK, Type.PAWN), 7, 4);
    }

    private void addGrandPosition(Position position) {
        position.addPiece(new Piece(Color.WHITE, Type.ROOK), 0, 0);
        position.addPiece(new Piece(Color.WHITE, Type.KNIGHT), 1, 1);
        position.addPiece(new Piece(Color.WHITE, Type.BISHOP), 1, 2);
        position.addPiece(new Piece(Color.WHITE, Type.QUEEN), 1, 3);
        position.addPiece(new Piece(Color.WHITE, Type.KING), 1, 4);
        position.addPiece(new Piece(Color.WHITE, Type.CHANCELLOR), 1, 5);
        position.addPiece(new Piece(Color.WHITE, Type.ARCHBISHOP), 1, 6);
        position.addPiece(new Piece(Color.WHITE, Type.BISHOP), 1, 7);
        position.addPiece(new Piece(Color.WHITE, Type.KNIGHT), 1, 8);
        position.addPiece(new Piece(Color.WHITE, Type.ROOK), 0, 9);
        for (int column = 0; column < 10; column++) {
            position.addPiece(new Piece(Color.WHITE, Type.PAWN), 2, column);
        }
        position.addPiece(new Piece(Color.BLACK, Type.ROOK), 9, 0);
        position.addPiece(new Piece(Color.BLACK, Type.KNIGHT), 8, 1);
        position.addPiece(new Piece(Color.BLACK, Type.BISHOP), 8, 2);
        position.addPiece(new Piece(Color.BLACK, Type.QUEEN), 8, 3);
        position.addPiece(new Piece(Color.BLACK, Type.KING), 8, 4);
        position.addPiece(new Piece(Color.BLACK, Type.CHANCELLOR), 8, 5);
        position.addPiece(new Piece(Color.BLACK, Type.ARCHBISHOP), 8, 6);
        position.addPiece(new Piece(Color.BLACK, Type.BISHOP), 8, 7);
        position.addPiece(new Piece(Color.BLACK, Type.KNIGHT), 8, 8);
        position.addPiece(new Piece(Color.BLACK, Type.ROOK), 9, 9);
        for (int column = 0; column < 10; column++) {
            position.addPiece(new Piece(Color.BLACK, Type.PAWN), 7, column);
        }
    }

}
