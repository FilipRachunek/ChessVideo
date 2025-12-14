package com.brainking.tools.dto;

import com.brainking.tools.utils.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Position {

    private static final Logger log = LoggerFactory.getLogger(Position.class);

    private final Game game;
    private final Piece[][] pieceGrid;
    private final boolean[][] visibleGrid;
    private Move currentMove;
    private int targetRow, targetColumn;
    private final Map<Color, List<Piece>> capturedPieces = new HashMap<>();
    private final Map<Color, Integer> checkCounter = new HashMap<>();
    private final Map<Color, Boolean> kingMoved = new HashMap<>();
    private boolean finished;

    public Position(Game game) {
        this.game = game;
        pieceGrid = new Piece[game.getHeight()][game.getWidth()];
        visibleGrid = new boolean[game.getHeight()][game.getWidth()];
        setVisibleGrid(true);
        capturedPieces.put(Color.WHITE, new ArrayList<>());
        capturedPieces.put(Color.BLACK, new ArrayList<>());
        checkCounter.put(Color.WHITE, 0);
        checkCounter.put(Color.BLACK, 0);
        kingMoved.put(Color.WHITE, false);
        kingMoved.put(Color.BLACK, false);
        targetRow = -1;
        targetColumn = -1;
    }

    public boolean isVisible(int row, int column) {
        return visibleGrid[row][column];
    }

    public Piece[][] getPieceGrid() {
        return pieceGrid;
    }

    public void addPiece(Piece piece, int row, int column) {
        pieceGrid[row][column] = piece;
        piece.setXY(game, column, row);
    }

    public void markTargetSquare(Move move) {
        targetRow = move.toRow;
        targetColumn = move.toColumn;
    }

    public int getWidth() {
        return game.getWidth();
    }

    public int getHeight() {
        return game.getHeight();
    }

    public boolean matchesTargetSquare(int row, int column) {
        return row == targetRow && column == targetColumn;
    }

    public void startMoving(Move move) {
        if (move.isAmbiguous() || move.isPlace()) {
            log.info(move.toString());
            currentMove = Move.from(move);
            if (move.isPlace()) {
                for (Piece piece : capturedPieces.get(currentMove.getOppositeColor())) {
                    if (piece.hasType(currentMove.getType())) {
                        piece.setTargetXYAndSteps(game, currentMove.toColumn, currentMove.toRow);
                        return;
                    }
                }
            }
            return;
        }
        // stop marking target square for Ambiguous Chess
        targetRow = -1;
        targetColumn = -1;
        if (!move.isEmpty()) {
            // calculate rows and columns from the PGN code
            addSourceSquareToMove(move);
            log.info(move.toString());
            currentMove = Move.from(move);
            // calculate frame steps
            pieceGrid[move.fromRow][move.fromColumn].setTargetXYAndSteps(game, move.toColumn, move.toRow);
            // handle castling
            Type castlingType = game.isVariant(Constants.KNIGHTMATE) ? Type.KNIGHT : Type.KING;
            if (move.getType() == castlingType) {
                int rookFromRow = move.fromRow;
                int rookFromColumn = -1;
                int rookToRow = move.toRow;
                int rookToColumn = -1;
                if ((game.isVariant(Constants.FISCHER_RANDOM) || game.isVariant(Constants.CAPABLANCA_RANDOM)) &&
                        (move.isCastlingShort() || move.isCastlingLong())) {
                    // find the king column
                    for (int column = 0; column < game.getWidth() - 1; column++) {
                        if (pieceGrid[move.fromRow][column] != null &&
                                pieceGrid[move.fromRow][column].hasType(Type.KING)) {
                            move.fromColumn = column;
                        }
                    }
                }
                if (move.isCastlingLong()) {
                    // O-O-O
                    rookFromColumn = game.isVariant(Constants.EMBASSY) || game.isVariant(Constants.JANUS) ? game.getWidth() - 1 : 0;
                    if (game.isVariant(Constants.FISCHER_RANDOM) || game.isVariant(Constants.CAPABLANCA_RANDOM)) {
                        // find the rook column
                        for (int column = 0; column < move.fromColumn; column++) {
                            if (pieceGrid[move.fromRow][column] != null &&
                                    pieceGrid[move.fromRow][column].hasType(Type.ROOK)) {
                                rookFromColumn = column;
                            }
                        }
                    }
                    rookToColumn = game.isVariant(Constants.EMBASSY) ? 6 : 3;
                    if (game.isVariant(Constants.JANUS)) {
                        rookToColumn = 7;
                    }
                } else if (move.isCastlingShort()) {
                    // O-O
                    rookFromColumn = game.isVariant(Constants.EMBASSY) || game.isVariant(Constants.JANUS) ? 0 : game.getWidth() - 1;
                    if (game.isVariant(Constants.FISCHER_RANDOM) || game.isVariant(Constants.CAPABLANCA_RANDOM)) {
                        // find the rook column
                        for (int column = move.fromColumn + 1; column < game.getWidth(); column++) {
                            if (pieceGrid[move.fromRow][column] != null &&
                                    pieceGrid[move.fromRow][column].hasType(Type.ROOK)) {
                                rookFromColumn = column;
                            }
                        }
                    }
                    rookToColumn = game.isVariant(Constants.EMBASSY) || game.isVariant(Constants.JANUS) ? 2 : 5;
                }
                if (rookFromColumn >= 0) {
                    pieceGrid[rookToRow][rookToColumn] = pieceGrid[rookFromRow][rookFromColumn];
                    pieceGrid[rookToRow][rookToColumn].setTargetXYAndSteps(game, rookToColumn, rookToRow);
                    pieceGrid[rookFromRow][rookFromColumn] = null;
                }
            }
        }
    }

    public boolean matchesCurrentMove(int row, int column) {
        return currentMove != null &&
                (currentMove.fromRow == row && currentMove.fromColumn == column ||
                        currentMove.toRow == row && currentMove.toColumn == column);
    }

    public boolean matchesPieceInCheck(int row, int column) {
        Type type = game.isVariant(Constants.KNIGHTMATE) ? Type.KNIGHT : Type.KING;
        return currentMove != null &&
                currentMove.isCheck() &&
                pieceGrid[row][column] != null &&
                pieceGrid[row][column].hasType(type) &&
                pieceGrid[row][column].hasColor(currentMove.getColor() == Color.WHITE ? Color.BLACK : Color.WHITE);
    }

    public boolean matchesHole(int row, int column) {
        return pieceGrid[row][column] != null && pieceGrid[row][column].getType() == Type.HOLE;
    }

    public boolean isMoving() {
        for (int i = 0; i < game.getHeight(); i++) {
            for (int j = 0; j < game.getWidth(); j++) {
                if (pieceGrid[i][j] != null && pieceGrid[i][j].isMoving()) {
                    return true;
                }
            }
        }
        for (Color color : Color.values()) {
            for (Piece piece : capturedPieces.get(color)) {
                if (piece.isMoving()) {
                    return true;
                }
            }
        }
        return false;
    }

    public void doMoveStep() {
        for (int i = 0; i < game.getHeight(); i++) {
            for (int j = 0; j < game.getWidth(); j++) {
                if (pieceGrid[i][j] != null && pieceGrid[i][j].isMoving()) {
                    pieceGrid[i][j].doMoveStep();
                }
            }
        }
        for (Color color : Color.values()) {
            for (Piece piece : capturedPieces.get(color)) {
                if (piece.isMoving()) {
                    piece.doMoveStep();
                }
            }
        }
    }

    public void stopMoving() {
        if (currentMove != null && currentMove.isPlace()) {
            for (Piece piece : capturedPieces.get(currentMove.getOppositeColor())) {
                if (piece.hasType(currentMove.getType())) {
                    removePieceFromCaptured(currentMove.getOppositeColor(), piece);
                    resetCapturePiecePositions(currentMove.getOppositeColor());
                    addPiece(new Piece(currentMove.getColor(), currentMove.getType()), currentMove.toRow, currentMove.toColumn);
                    return;
                }
            }
            return;
        }
        if (currentMove != null && !currentMove.isAmbiguous()) {
            if (currentMove.isCheck()) {
                checkCounter.put(currentMove.getColor(), checkCounter.get(currentMove.getColor()) + 1);
            }
            boolean targetSquareEmpty = pieceGrid[currentMove.toRow][currentMove.toColumn] == null;
            if (!targetSquareEmpty && currentMove.isCapture() &&
                    !pieceGrid[currentMove.toRow][currentMove.toColumn].hasType(Type.ICE_CUBE)) {
                capturePiece(pieceGrid[currentMove.toRow][currentMove.toColumn]);
            }
            pieceGrid[currentMove.toRow][currentMove.toColumn] = pieceGrid[currentMove.fromRow][currentMove.fromColumn];
            // handle promoting
            if (currentMove.getPromoteType() != null) {
                pieceGrid[currentMove.toRow][currentMove.toColumn].promoteTo(currentMove.getPromoteType());
            }
            // handle en passant capture
            if (currentMove.isCapture() && currentMove.getType() == Type.PAWN && targetSquareEmpty) {
                capturePiece(pieceGrid[currentMove.fromRow][currentMove.toColumn]);
                pieceGrid[currentMove.fromRow][currentMove.toColumn] = null;
            }
            pieceGrid[currentMove.fromRow][currentMove.fromColumn] = null;
            if (game.isVariant(Constants.ATOMIC) && currentMove.isCapture()) {
                // handle exploded pieces
                // TODO: maybe some simple animation?
                for (int[] array : Type.KING.getMoveDirectionArray()) {
                    int explodeRow = currentMove.toRow + array[0];
                    int explodeColumn = currentMove.toColumn + array[1];
                    if (isValidSquare(explodeRow, explodeColumn) &&
                            pieceGrid[explodeRow][explodeColumn] != null &&
                            !pieceGrid[explodeRow][explodeColumn].hasType(Type.PAWN)) {
                        capturePiece(pieceGrid[explodeRow][explodeColumn]);
                        pieceGrid[explodeRow][explodeColumn] = null;
                    }
                }
                capturePiece(pieceGrid[currentMove.toRow][currentMove.toColumn]);
                pieceGrid[currentMove.toRow][currentMove.toColumn] = null;
            }
            if (game.isVariant(Constants.CHESHIRE_CAT)) {
                pieceGrid[currentMove.fromRow][currentMove.fromColumn] = new Piece(Type.HOLE);
            }
            if (game.isVariant(Constants.ICE_AGE) &&
                    currentMove.getMoveNumber() % 20 == 0 &&
                    currentMove.getColor() == Color.BLACK) {
                // after every 20th move, perform the ice age
                // add ice cubes to empty squares not orthogonally surrounded by pieces
                for (int row = 0; row < game.getHeight(); row++) {
                    for (int column = 0; column < game.getWidth(); column++) {
                        if (pieceGrid[row][column] == null) {
                            for (int[] array : Type.KING.getMoveDirectionArray()) {
                                if (pieceGrid[row][column] == null &&
                                        (array[0] == 0 || array[1] == 0)) {
                                    int testRow = row + array[0];
                                    int testColumn = column + array[1];
                                    if (isValidSquare(testRow, testColumn) &&
                                            !isPlayablePiece(pieceGrid[testRow][testColumn])) {
                                        addPiece(new Piece(Type.ICE_CUBE), row, column);
                                    }
                                }
                            }
                        }
                    }
                }
                // freeze (capture and replace with ice cubes) pieces not connected to other pieces
                for (int row = 0; row < game.getHeight(); row++) {
                    for (int column = 0; column < game.getWidth(); column++) {
                        if (pieceGrid[row][column] != null && !pieceGrid[row][column].hasType(Type.ICE_CUBE)) {
                            boolean connectedPieceFound = false;
                            for (int[] array : Type.KING.getMoveDirectionArray()) {
                                int testRow = row + array[0];
                                int testColumn = column + array[1];
                                if (isValidSquare(testRow, testColumn) &&
                                        isPlayablePiece(pieceGrid[testRow][testColumn])) {
                                    connectedPieceFound = true;
                                }
                            }
                            if (!connectedPieceFound) {
                                capturePiece(pieceGrid[row][column]);
                                addPiece(new Piece(Type.ICE_CUBE), row, column);
                            }
                        }
                    }
                }
            }
            currentMove.clearSquares();
        }
        if (game.isVariant(Constants.DARK)) {
            // recalculate visible squares
            resetVisibleGrid();
        }
    }

    public void setVisibleGrid(boolean visible) {
        for (int row = 0; row < game.getHeight(); row++) {
            for (int column = 0; column < game.getWidth(); column++) {
                visibleGrid[row][column] = visible;
            }
        }
    }

    public void resetVisibleGrid() {
        setVisibleGrid(false);
        Color color = game.hasOppositeOrientation() ? Color.BLACK : Color.WHITE;
        for (int row = 0; row < game.getHeight(); row++) {
            for (int column = 0; column < game.getWidth(); column++) {
                Piece piece = pieceGrid[row][column];
                if (piece != null && piece.hasColor(color)) {
                    visibleGrid[row][column] = true;
                    Type type = piece.getType();
                    if (type == Type.PAWN) {
                        int direction = color == Color.WHITE ? 1 : -1;
                        int startRow = color == Color.WHITE ? 1 : 6;
                        visibleGrid[row + direction][column] = true;
                        if (row == startRow && !isPlayablePiece(pieceGrid[row + direction][column])) {
                            visibleGrid[row + direction + direction][column] = true;
                        }
                        if (isValidSquare(row + direction, column - 1)) {
                            visibleGrid[row + direction][column - 1] = true;
                        }
                        if (isValidSquare(row + direction, column + 1)) {
                            visibleGrid[row + direction][column + 1] = true;
                        }
                    } else {
                        int[][] moveDirectionArray = type.getMoveDirectionArray();
                        int maxMoveDistance = type.getMaxMoveDistance();
                        for (int[] array : moveDirectionArray) {
                            boolean directionSearch = true;
                            for (int distance = 1; distance <= maxMoveDistance && directionSearch; distance++) {
                                int testRow = row + array[0] * distance;
                                int testColumn = column + array[1] * distance;
                                if (isValidSquare(testRow, testColumn)) {
                                    visibleGrid[testRow][testColumn] = true;
                                    if (isPlayablePiece(pieceGrid[testRow][testColumn])) {
                                        directionSearch = false;
                                    }
                                } else {
                                    directionSearch = false;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void capturePiece(Piece piece) {
        Piece capturedPiece = Piece.from(piece);
        if (game.isVariant(Constants.LOOP)) {
            capturedPiece.setOppositeColor();
        }
        capturedPieces.get(piece.getColor()).add(capturedPiece);
        resetCapturePiecePositions(piece.getColor());
    }

    public void resetCapturePiecePositions(Color color) {
        int index = 0;
        for (Piece piece : capturedPieces.get(color)) {
            piece.setXY(game, color, index);
            index++;
        }
    }

    public void removePieceFromCaptured(Color color, Piece piece) {
        capturedPieces.get(color).remove(piece);
    }

    public List<Piece> getWhiteCapturedPieces() {
        return capturedPieces.get(Color.WHITE);
    }

    public List<Piece> getBlackCapturedPieces() {
        return capturedPieces.get(Color.BLACK);
    }

    public void finishGame() {
        this.finished = true;
        setVisibleGrid(true);
    }

    public boolean isFinished() {
        return finished;
    }

    public boolean isCheckmate() {
        return currentMove != null && currentMove.isCheckmate();
    }

    public String getGameStatus() {
        if (game.isVariant(Constants.THREE_CHECKS)) {
            return "Checks: " + checkCounter.get(Color.WHITE) + "/" + checkCounter.get(Color.BLACK);
        }
        return "";
    }

    public String getResult() {
        if (game.isVariant(Constants.EXTINCTION)) {
            Set<Type> whiteTypes = new HashSet<>();
            Set<Type> blackTypes = new HashSet<>();
            for (int i = 0; i < game.getHeight(); i++) {
                for (int j = 0; j < game.getWidth(); j++) {
                    if (pieceGrid[i][j] != null) {
                        if (pieceGrid[i][j].hasColor(Color.WHITE)) {
                            whiteTypes.add(pieceGrid[i][j].getType());
                        } else if (pieceGrid[i][j].hasColor(Color.BLACK)) {
                            blackTypes.add(pieceGrid[i][j].getType());
                        }
                    }
                }
            }
            for (Type type : Type.values()) {
                if (!whiteTypes.contains(type)) {
                    return "White lost all " + type.name().toLowerCase() + "s";
                }
                if (!blackTypes.contains(type)) {
                    return "Black lost all " + type.name().toLowerCase() + "s";
                }
            }
        }
        if (game.isVariant(Constants.THREE_CHECKS)) {
            if (checkCounter.get(Color.WHITE) == 3 || checkCounter.get(Color.BLACK) == 3) {
                return "Third check";
            }
        }
        if (game.isVariant(Constants.ANTI)) {
            int whiteCounter = 0;
            int blackCounter = 0;
            for (int i = 0; i < game.getHeight(); i++) {
                for (int j = 0; j < game.getWidth(); j++) {
                    if (pieceGrid[i][j] != null) {
                        if (pieceGrid[i][j].isWhite()) {
                            whiteCounter++;
                        } else {
                            blackCounter++;
                        }
                    }
                }
            }
            if (whiteCounter == 0) {
                return "White lost all pieces";
            }
            if (blackCounter == 0) {
                return "Black lost all pieces";
            }
        }
        if (game.isVariant(Constants.ATOMIC) || game.isVariant(Constants.DARK)) {
            boolean whiteKingFound = false;
            boolean blackKingFound = false;
            for (int i = 0; i < game.getHeight(); i++) {
                for (int j = 0; j < game.getWidth(); j++) {
                    if (pieceGrid[i][j] != null && pieceGrid[i][j].hasType(Type.KING)) {
                        if (pieceGrid[i][j].isWhite()) {
                            whiteKingFound = true;
                        } else {
                            blackKingFound = true;
                        }
                    }
                }
            }
            if (!whiteKingFound) {
                return game.isVariant(Constants.ATOMIC) ? "White king exploded" : "White king captured";
            }
            if (!blackKingFound) {
                return game.isVariant(Constants.ATOMIC) ? "Black king exploded" : "Black king captured";
            }
        }
        if (game.isVariant(Constants.RACING_KINGS)) {
            boolean whiteKingLastRow = false;
            boolean blackKingLastRow = false;
            int row = game.getHeight() - 1;
            for (int column = 0; column < game.getWidth(); column++) {
                if (pieceGrid[row][column] != null && pieceGrid[row][column].hasType(Type.KING)) {
                    if (pieceGrid[row][column].isWhite()) {
                        whiteKingLastRow = true;
                    } else {
                        blackKingLastRow = true;
                    }
                }
            }
            if (whiteKingLastRow && blackKingLastRow) {
                return "Both kings reached the last row";
            }
            if (whiteKingLastRow) {
                return "White king reached the last row";
            }
            if (blackKingLastRow) {
                return "Black king reached the last row";
            }
        }
        if (game.isVariant(Constants.MASSACRE)) {
            return "No more moves";
        }
        if (isCheckmate()) {
            return "Checkmate";
        }
        if (game.whiteWon()) {
            return "Black resigned";
        }
        if (game.blackWon()) {
            return "White resigned";
        }
        if (game.draw()) {
            return "Draw";
        }
        return null;
    }

    public Notation getCurrentMoveNotationDto() {
        String pgnCode = "";
        int moveNumber = -1;
        String prefix = "";
        String symbol = "";
        String suffix = "";
        String result = "";
        if (currentMove != null) {
            pgnCode = currentMove.getPgnCode();
            if (currentMove.getColor() == Color.WHITE) {
                moveNumber = currentMove.getMoveNumber();
            }
            prefix = currentMove.getMoveNumber() + ". " + (currentMove.getColor() == Color.BLACK ? "... " : "");
            if (game.isVariant(Constants.DARK) &&
                    (game.hasOppositeOrientation() && currentMove.getColor() == Color.WHITE ||
                            !game.hasOppositeOrientation() && currentMove.getColor() == Color.BLACK)) {
                suffix = "?";
            } else {
                symbol = getSymbol(pgnCode.substring(0, 1), currentMove.getColor());
                suffix = symbol.isBlank() ? pgnCode : pgnCode.substring(1);
            }
            if (isFinished()) {
                String r = getResult();
                log.info("Result: " + r);
                result = " (" + r + ")";
            }
        }
        return new Notation(pgnCode, moveNumber, prefix, symbol, suffix, result);
    }

    private String getSymbol(String symbolCode, Color color) {
        boolean isWhite = color == Color.WHITE;
        return switch (symbolCode) {
            case "N" -> isWhite ? "♘" : "♞";
            case "B" -> isWhite ? "♗" : "♝";
            case "R" -> isWhite ? "♖" : "♜";
            case "Q" -> isWhite ? "♕" : "♛";
            case "K" -> isWhite ? "♔" : "♚";
            default -> "";
        };
    }

    private boolean isValidSquare(int row, int column) {
        return row >= 0 && row < game.getHeight() && column >= 0 && column < game.getWidth();
    }

    private boolean isPlayablePiece(Piece piece) {
        return piece != null && piece.isPlayable();
    }

    private void addSourceSquareToMove(Move move) {
        if (move.getType() == Type.PAWN && game.isVariant(Constants.LEGAN)) {
            if (move.isCapture()) {
                if (move.fromColumn == move.toColumn) {
                    move.fromRow = move.toRow + (move.getColor() == Color.WHITE ? -1 : 1);
                } else {
                    move.fromRow = move.toRow;
                }
            } else {
                move.fromRow = move.toRow + (move.getColor() == Color.WHITE ? -1 : 1);
                move.fromColumn = move.toColumn + (move.getColor() == Color.WHITE ? 1 : -1);
            }
        } else if (move.getType() == Type.PAWN && !move.isRelayed()) {
            int testRow = move.toRow + (move.getColor() == Color.WHITE ? -1 : 1);
            if (move.fromColumn == -1) {
                // no capture
                move.fromColumn = move.toColumn;
                if (pieceGrid[testRow][move.fromColumn] == null ||
                        pieceGrid[testRow][move.fromColumn].hasType(Type.HOLE)) {
                    testRow = move.toRow + (move.getColor() == Color.WHITE ? -2 : 2);
                }
            }
            move.fromRow = testRow;
        } else {
            addSourceSquareForPiece(move);
        }
    }

    private void addSourceSquareForPiece(Move move) {
        Type type = move.isRelayed() ? Type.KNIGHT : move.getType();
        Color color = move.getColor();
        int[][] moveDirectionArray = type.getMoveDirectionArray();
        int maxMoveDistance = type.getMaxMoveDistance();
        if (game.isVariant(Constants.CHESHIRE_CAT) && type == Type.KING && !kingMoved.get(color)) {
            // Cheshire Cat king can make the first move as a queen
            maxMoveDistance = Type.QUEEN.getMaxMoveDistance();
            kingMoved.put(color, true);
        }
        for (int[] array : moveDirectionArray) {
            if (type == Type.ARCHBISHOP || type == Type.CHANCELLOR || type == Type.JANUS) {
                // knight-like moves always have a fixed distance
                maxMoveDistance = Math.abs(array[0]) == 2 || Math.abs(array[1]) == 2 ? 1 : type.getMaxMoveDistance();
            }
            boolean directionSearch = true;
            for (int distance = 1; distance <= maxMoveDistance && directionSearch; distance++) {
                int testRow = move.toRow + array[0] * distance;
                int testColumn = move.toColumn + array[1] * distance;
                if (isValidSquare(testRow, testColumn)) {
                    // check pre-filled fromRow or fromColumn
                    if ((move.fromRow == -1 || move.fromRow == testRow) &&
                            (move.fromColumn == -1 || move.fromColumn == testColumn) &&
                            pieceGrid[testRow][testColumn] != null &&
                            !pieceGrid[testRow][testColumn].hasType(Type.HOLE)) {
                        if (pieceGrid[testRow][testColumn].hasType(move.getType()) &&
                                pieceGrid[testRow][testColumn].hasColor(color)) {
                            boolean pieceFound = true;
                            if (move.isRelayed()) {
                                // find relayed knight
                                pieceFound = false;
                                for (int[] knightArray : Type.KNIGHT.getMoveDirectionArray()) {
                                    int knightRow = testRow + knightArray[0];
                                    int knightColumn = testColumn + knightArray[1];
                                    if (isValidSquare(knightRow, knightColumn) &&
                                            pieceGrid[knightRow][knightColumn] != null &&
                                            pieceGrid[knightRow][knightColumn].hasType(Type.KNIGHT) &&
                                            pieceGrid[knightRow][knightColumn].hasColor(color)) {
                                        pieceFound = true;
                                    }
                                }
                            }
                            if (pieceFound) {
                                move.fromRow = testRow;
                                move.fromColumn = testColumn;
                                return;
                            }
                        } else {
                            directionSearch = false;
                        }
                    }
                }
            }
        }
    }

}
