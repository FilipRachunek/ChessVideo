package com.brainking.tools.dto;

import com.brainking.tools.utils.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Position {

    private static final Logger LOG = LoggerFactory.getLogger(Position.class);

    private final Game game;
    private final Piece[][] pieceGrid;
    private final boolean[][] visibleGrid;
    private Move currentMove;
    private int targetRow;
    private int targetColumn;
    private final Map<Color, List<Piece>> capturedPieces = new EnumMap<>(Color.class);
    private final Map<Color, Integer> checkCounter = new EnumMap<>(Color.class);
    private final Map<Color, Boolean> kingMoved = new EnumMap<>(Color.class);
    private boolean finished;

    public Position(final Game game) {
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

    public boolean isVisible(final int row, final int column) {
        return visibleGrid[row][column];
    }

    public Piece[][] getPieceGrid() {
        return Arrays.copyOf(pieceGrid, pieceGrid.length);
    }

    public void addPiece(final Piece piece, final int row, final int column) {
        pieceGrid[row][column] = piece;
        piece.setXY(game, column, row);
    }

    public void markTargetSquare(final Move move) {
        targetRow = move.toRow;
        targetColumn = move.toColumn;
    }

    public int getWidth() {
        return game.getWidth();
    }

    public int getHeight() {
        return game.getHeight();
    }

    public boolean matchesTargetSquare(final int row, final int column) {
        return row == targetRow && column == targetColumn;
    }

    public void startMoving(final Move move) {
        if (move.isAmbiguous() || move.isPlace()) {
            LOG.info(move.toString());
            currentMove = Move.from(move);
            handlePlacingMoveStart(move);
            return;
        }
        // stop marking target square for Ambiguous Chess
        targetRow = -1;
        targetColumn = -1;
        if (!move.isEmpty()) {
            // calculate rows and columns from the PGN code
            addSourceSquareToMove(move);
            LOG.info(move.toString());
            currentMove = Move.from(move);
            // calculate frame steps
            pieceGrid[move.fromRow][move.fromColumn].setTargetXYAndSteps(game, move.toColumn, move.toRow);
            handleCastling(move);
        }
    }

    public boolean matchesCurrentMove(final int row, final int column) {
        return currentMove != null &&
                (currentMove.fromRow == row && currentMove.fromColumn == column ||
                        currentMove.toRow == row && currentMove.toColumn == column);
    }

    public boolean matchesPieceInCheck(final int row, final int column) {
        final Type type = game.isVariant(Constants.KNIGHTMATE) ? Type.KNIGHT : Type.KING;
        return currentMove != null &&
                currentMove.isCheck() &&
                pieceGrid[row][column] != null &&
                pieceGrid[row][column].hasType(type) &&
                pieceGrid[row][column].hasColor(currentMove.getOppositeColor());
    }

    public boolean matchesHole(final int row, final int column) {
        return pieceGrid[row][column] != null && pieceGrid[row][column].isHole();
    }

    public boolean isMoving() {
        boolean moving = false;
        for (int i = 0; i < game.getHeight(); i++) {
            for (int j = 0; j < game.getWidth(); j++) {
                if (pieceGrid[i][j] != null && pieceGrid[i][j].isMoving()) {
                    moving = true;
                    break;
                }
            }
        }
        for (final Color color : Color.values()) {
            for (final Piece piece : capturedPieces.get(color)) {
                if (piece.isMoving()) {
                    moving = true;
                    break;
                }
            }
        }
        return moving;
    }

    public void doMoveStep() {
        for (int i = 0; i < game.getHeight(); i++) {
            for (int j = 0; j < game.getWidth(); j++) {
                if (pieceGrid[i][j] != null && pieceGrid[i][j].isMoving()) {
                    pieceGrid[i][j].doMoveStep();
                }
            }
        }
        for (final Color color : Color.values()) {
            for (final Piece piece : capturedPieces.get(color)) {
                if (piece.isMoving()) {
                    piece.doMoveStep();
                }
            }
        }
    }

    public void stopMoving() {
        if (currentMove != null && !currentMove.isAmbiguous()) {
            handleStopMoving();
        }
        else if (currentMove != null && currentMove.isPlace()) {
            handlePlacingMoveEnd();
        }
    }

    @SuppressWarnings("PMD.NullAssignment")
    private void handleStopMoving() {
        if (currentMove.isCheck()) {
            checkCounter.put(currentMove.getColor(), checkCounter.get(currentMove.getColor()) + 1);
        }
        final boolean targetSquareEmpty = pieceGrid[currentMove.toRow][currentMove.toColumn] == null;
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
        if (currentMove.isCapture() && currentMove.isPawn() && targetSquareEmpty) {
            capturePiece(pieceGrid[currentMove.fromRow][currentMove.toColumn]);
            pieceGrid[currentMove.fromRow][currentMove.toColumn] = null;
        }
        pieceGrid[currentMove.fromRow][currentMove.fromColumn] = null;
        handleVariants();
        currentMove.clearSquares();
    }

    private void setVisibleGrid(final boolean visible) {
        for (int row = 0; row < game.getHeight(); row++) {
            for (int column = 0; column < game.getWidth(); column++) {
                visibleGrid[row][column] = visible;
            }
        }
    }

    public void resetVisibleGrid() {
        setVisibleGrid(false);
        final Color color = game.hasOppositeOrientation() ? Color.BLACK : Color.WHITE;
        for (int row = 0; row < game.getHeight(); row++) {
            for (int column = 0; column < game.getWidth(); column++) {
                final Piece piece = pieceGrid[row][column];
                if (piece != null && piece.hasColor(color)) {
                    visibleGrid[row][column] = true;
                    calculatePieceReach(piece, row, column);
                }
            }
        }
    }

    public void capturePiece(final Piece piece) {
        final Piece capturedPiece = Piece.from(piece);
        if (game.isVariant(Constants.LOOP)) {
            capturedPiece.setOppositeColor();
        }
        capturedPieces.get(piece.getColor()).add(capturedPiece);
        resetCapturePiecePositions(piece.getColor());
    }

    public void resetCapturePiecePositions(final Color color) {
        int index = 0;
        for (final Piece piece : capturedPieces.get(color)) {
            piece.setXY(game, color, index);
            index++;
        }
    }

    public void removePieceFromCaptured(final Color color, final Piece piece) {
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
        return game.isVariant(Constants.THREE_CHECKS) ?
        "Checks: " + checkCounter.get(Color.WHITE) + "/" + checkCounter.get(Color.BLACK) :
        "";
    }

    public String getResult() {
        String variant = game.getVariant();
        if (variant == null) {
            variant = "";
        }
        String result = switch (variant) {
            case Constants.EXTINCTION -> getExtinctionResult();
            case Constants.THREE_CHECKS -> getThreeChecksResult();
            case Constants.ANTI -> getAntiResult();
            case Constants.ATOMIC -> getAtomicResult();
            case Constants.DARK -> getDarkResult();
            case Constants.RACING_KINGS -> getRacingKingsResult();
            case Constants.MASSACRE -> getMassacreResult();
            default -> "";
        };
        if (StringUtils.isBlank(result)) {
            if (isCheckmate()) {
                result = "Checkmate";
            } else if (game.whiteWon()) {
                result = "Black resigned";
            } else if (game.blackWon()) {
                result = "White resigned";
            } else if (game.draw()) {
                result = "Draw";
            }
        }
        return result;
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
            if (currentMove.isWhite()) {
                moveNumber = currentMove.getMoveNumber();
            }
            prefix = currentMove.getMoveNumber() + ". " + (currentMove.isBlack() ? "... " : "");
            if (game.isVariant(Constants.DARK) &&
                    (game.hasOppositeOrientation() && currentMove.isWhite() ||
                            !game.hasOppositeOrientation() && currentMove.isBlack())) {
                suffix = "?";
            } else {
                symbol = getSymbol(pgnCode.substring(0, 1), currentMove.getColor());
                suffix = symbol.isBlank() ? pgnCode : pgnCode.substring(1);
            }
            if (isFinished()) {
                final String resultString = getResult();
                LOG.info("Result: " + resultString);
                result = " (" + resultString + ")";
            }
        }
        return new Notation(pgnCode, moveNumber, prefix, symbol, suffix, result);
    }

    private String getExtinctionResult() {
        String result = "";
        final Set<Type> whiteTypes = EnumSet.noneOf(Type.class);
        final Set<Type> blackTypes = EnumSet.noneOf(Type.class);
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
        for (final Type type : Type.values()) {
            if (!whiteTypes.contains(type)) {
                result = "White lost all " + type.name().toLowerCase(Locale.ENGLISH) + "s";
            }
            if (!blackTypes.contains(type)) {
                result = "Black lost all " + type.name().toLowerCase(Locale.ENGLISH) + "s";
            }
        }
        return result;
    }

    private String getThreeChecksResult() {
        String result = "";
        if (checkCounter.get(Color.WHITE) == 3 || checkCounter.get(Color.BLACK) == 3) {
            result = "Third check";
        }
        return result;
    }

    private String getAntiResult() {
        String result = "";
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
            result = "White lost all pieces";
        }
        if (blackCounter == 0) {
            result = "Black lost all pieces";
        }
        return result;
    }

    private String getAtomicResult() {
        String result = "";
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
            result = "White king exploded";
        }
        if (!blackKingFound) {
            result = "Black king exploded";
        }
        return result;
    }

    private String getDarkResult() {
        String result = "";
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
            result = "White king captured";
        }
        if (!blackKingFound) {
            result = "Black king captured";
        }
        return result;
    }

    private String getRacingKingsResult() {
        String result = "";
        boolean whiteKingLastRow = false;
        boolean blackKingLastRow = false;
        final int row = game.getHeight() - 1;
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
            result = "Both kings reached the last row";
        }
        if (whiteKingLastRow) {
            result = "White king reached the last row";
        }
        if (blackKingLastRow) {
            result = "Black king reached the last row";
        }
        return result;
    }

    private String getMassacreResult() {
        return "No more moves";
    }

    @SuppressWarnings("PMD.NullAssignment")
    private void handleCastling(final Move move) {
        // handle castling
        if (move.isCastlingType(game.getVariant())) {
            Transition rook = new Transition(move.fromRow, -1, move.toRow, -1);
            if ((game.isVariant(Constants.FISCHER_RANDOM) || game.isVariant(Constants.CAPABLANCA_RANDOM)) &&
                    (move.isCastlingShort() || move.isCastlingLong())) {
                calculateKingPosition(move);
            }
            if (move.isCastlingLong()) {
                rook = calculateLongCastling(rook, move);
            } else if (move.isCastlingShort()) {
                rook = calculateShortCastling(rook, move);
            }
            if (rook.fromColumn() >= 0) {
                pieceGrid[rook.toRow()][rook.toColumn()] = pieceGrid[rook.fromRow()][rook.fromColumn()];
                pieceGrid[rook.toRow()][rook.toColumn()].setTargetXYAndSteps(game, rook.toColumn(), rook.toRow());
                pieceGrid[rook.fromRow()][rook.fromColumn()] = null;
            }
        }
    }

    private void calculateKingPosition(final Move move) {
        // find the king column
        for (int column = 0; column < game.getWidth() - 1; column++) {
            if (pieceGrid[move.fromRow][column] != null &&
                    pieceGrid[move.fromRow][column].hasType(Type.KING)) {
                move.fromColumn = column;
            }
        }
    }

    private Transition calculateLongCastling(final Transition rook, final Move move) {
        // O-O-O
        int rookFromColumn = game.isVariant(Constants.EMBASSY) || game.isVariant(Constants.JANUS) ? game.getWidth() - 1 : 0;
        if (game.isVariant(Constants.FISCHER_RANDOM) || game.isVariant(Constants.CAPABLANCA_RANDOM)) {
            // find the rook column
            for (int column = 0; column < move.fromColumn; column++) {
                if (pieceGrid[move.fromRow][column] != null &&
                        pieceGrid[move.fromRow][column].hasType(Type.ROOK)) {
                    rookFromColumn = column;
                }
            }
        }
        int rookToColumn = game.isVariant(Constants.EMBASSY) ? 6 : 3;
        if (game.isVariant(Constants.JANUS)) {
            rookToColumn = 7;
        }
        return new Transition(rook.fromRow(), rookFromColumn, rook.toRow(), rookToColumn);
    }

    private Transition calculateShortCastling(final Transition rook, final Move move) {
        // O-O
        int rookFromColumn = game.isVariant(Constants.EMBASSY) || game.isVariant(Constants.JANUS) ? 0 : game.getWidth() - 1;
        if (game.isVariant(Constants.FISCHER_RANDOM) || game.isVariant(Constants.CAPABLANCA_RANDOM)) {
            // find the rook column
            for (int column = move.fromColumn + 1; column < game.getWidth(); column++) {
                if (pieceGrid[move.fromRow][column] != null &&
                        pieceGrid[move.fromRow][column].hasType(Type.ROOK)) {
                    rookFromColumn = column;
                }
            }
        }
        final int rookToColumn = game.isVariant(Constants.EMBASSY) || game.isVariant(Constants.JANUS) ? 2 : 5;
        return new Transition(rook.fromRow(), rookFromColumn, rook.toRow(), rookToColumn);
    }

    private void handlePlacingMoveStart(final Move move) {
        if (move.isPlace()) {
            for (final Piece piece : capturedPieces.get(currentMove.getOppositeColor())) {
                if (piece.hasType(currentMove.getType())) {
                    piece.setTargetXYAndSteps(game, currentMove.toColumn, currentMove.toRow);
                }
            }
        }
    }

    private void handlePlacingMoveEnd() {
        for (final Piece piece : capturedPieces.get(currentMove.getOppositeColor())) {
            if (piece.hasType(currentMove.getType())) {
                removePieceFromCaptured(currentMove.getOppositeColor(), piece);
                resetCapturePiecePositions(currentMove.getOppositeColor());
                addPiece(new Piece(currentMove.getColor(), currentMove.getType()), currentMove.toRow, currentMove.toColumn);
            }
        }
    }

    private void handleVariants() {
        if (game.isVariant(Constants.ATOMIC) && currentMove.isCapture()) {
            handleAtomicExplosion();
        }
        if (game.isVariant(Constants.CHESHIRE_CAT)) {
            pieceGrid[currentMove.fromRow][currentMove.fromColumn] = new Piece(Type.HOLE);
        }
        if (game.isVariant(Constants.ICE_AGE) &&
                currentMove.getMoveNumber() % 20 == 0 &&
                currentMove.isBlack()) {
            handleIceAgeEvent();
        }
        if (game.isVariant(Constants.DARK)) {
            // recalculate visible squares
            resetVisibleGrid();
        }
    }

    @SuppressWarnings("PMD.NullAssignment")
    private void handleAtomicExplosion() {
        // handle exploded pieces
        // TODO: maybe some simple animation?
        for (final int[] array : Type.KING.getMoveDirectionArray()) {
            final int explodeRow = currentMove.toRow + array[0];
            final int explodeColumn = currentMove.toColumn + array[1];
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

    private void handleIceAgeEvent() {
        // after every 20th move, perform the ice age
        // add ice cubes to empty squares not orthogonally surrounded by pieces
        for (int row = 0; row < game.getHeight(); row++) {
            for (int column = 0; column < game.getWidth(); column++) {
                if (pieceGrid[row][column] == null) {
                    addSurroundingIceCubes(row, column);
                }
            }
        }
        // freeze (capture and replace with ice cubes) pieces not connected to other pieces
        for (int row = 0; row < game.getHeight(); row++) {
            for (int column = 0; column < game.getWidth(); column++) {
                if (pieceGrid[row][column] != null && !pieceGrid[row][column].hasType(Type.ICE_CUBE)) {
                    freezePiece(row, column);
                }
            }
        }
    }

    private void addSurroundingIceCubes(final int row, final int column) {
        for (final int[] array : Type.KING.getMoveDirectionArray()) {
            if (pieceGrid[row][column] == null &&
                    (array[0] == 0 || array[1] == 0)) {
                final int testRow = row + array[0];
                final int testColumn = column + array[1];
                if (isValidSquare(testRow, testColumn) &&
                        !isPlayablePiece(pieceGrid[testRow][testColumn])) {
                    addPiece(new Piece(Type.ICE_CUBE), row, column);
                }
            }
        }
    }

    private void freezePiece(final int row, final int column) {
        boolean connectedPieceFound = false;
        for (final int[] array : Type.KING.getMoveDirectionArray()) {
            final int testRow = row + array[0];
            final int testColumn = column + array[1];
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

    private void calculatePieceReach(final Piece piece, final int row, final int column) {
        if (piece.isPawn()) {
            final int direction = piece.isWhite() ? 1 : -1;
            final int startRow = piece.isWhite() ? 1 : 6;
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
            final int[][] moveDirectionArray = piece.getMoveDirectionArray();
            final int maxMoveDistance = piece.getMaxMoveDistance();
            for (final int[] array : moveDirectionArray) {
                boolean directionSearch = true;
                for (int distance = 1; distance <= maxMoveDistance && directionSearch; distance++) {
                    final int testRow = row + array[0] * distance;
                    final int testColumn = column + array[1] * distance;
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

    private String getSymbol(final String symbolCode, final Color color) {
        final boolean isWhite = color == Color.WHITE;
        return switch (symbolCode) {
            case "N" -> isWhite ? "♘" : "♞";
            case "B" -> isWhite ? "♗" : "♝";
            case "R" -> isWhite ? "♖" : "♜";
            case "Q" -> isWhite ? "♕" : "♛";
            case "K" -> isWhite ? "♔" : "♚";
            default -> "";
        };
    }

    private boolean isValidSquare(final int row, final int column) {
        return row >= 0 && row < game.getHeight() && column >= 0 && column < game.getWidth();
    }

    private boolean isPlayablePiece(final Piece piece) {
        return piece != null && piece.isPlayable();
    }

    private void addSourceSquareToMove(final Move move) {
        if (move.isPawn() && game.isVariant(Constants.LEGAN)) {
            addSourceSquareLeganPawn(move);
        } else if (move.isPawn() && !move.isRelayed()) {
            addSourceSquareNormalPawn(move);
        } else {
            addSourceSquareForPiece(move);
        }
    }

    private void addSourceSquareLeganPawn(final Move move) {
        if (move.isCapture()) {
            if (move.fromColumn == move.toColumn) {
                move.fromRow = move.toRow + (move.isWhite() ? -1 : 1);
            } else {
                move.fromRow = move.toRow;
            }
        } else {
            move.fromRow = move.toRow + (move.isWhite() ? -1 : 1);
            move.fromColumn = move.toColumn + (move.isWhite() ? 1 : -1);
        }
    }

    private void addSourceSquareNormalPawn(final Move move) {
        int testRow = move.toRow + (move.isWhite() ? -1 : 1);
        if (move.fromColumn == -1) {
            // no capture
            move.fromColumn = move.toColumn;
            if (pieceGrid[testRow][move.fromColumn] == null ||
                    pieceGrid[testRow][move.fromColumn].isHole()) {
                testRow = move.toRow + (move.isWhite() ? -2 : 2);
            }
        }
        move.fromRow = testRow;
    }

    private void addSourceSquareForPiece(final Move move) {
        final Color color = move.getColor();
        final int[][] moveDirectionArray = move.getMoveDirectionArray();
        int maxMoveDistance = move.getMaxMoveDistance();
        if (game.isVariant(Constants.CHESHIRE_CAT) && move.isKing() && !kingMoved.get(color)) {
            // Cheshire Cat king can make the first move as a queen
            maxMoveDistance = Type.QUEEN.getMaxMoveDistance();
            kingMoved.put(color, true);
        }
        for (final int[] array : moveDirectionArray) {
            if (move.isKnightLikeType()) {
                // knight-like moves always have a fixed distance
                maxMoveDistance = Math.abs(array[0]) == 2 || Math.abs(array[1]) == 2 ? 1 : move.getMaxMoveDistance();
            }
            findSourcePiece(move, array, maxMoveDistance);
        }
    }

    private void findSourcePiece(final Move move, final int[] array, final int maxMoveDistance) {
        boolean directionSearch = true;
        for (int distance = 1; distance <= maxMoveDistance && directionSearch; distance++) {
            final int testRow = move.toRow + array[0] * distance;
            final int testColumn = move.toColumn + array[1] * distance;
            if (isValidSquare(testRow, testColumn) &&
                (move.fromRow == -1 || move.fromRow == testRow) &&
                (move.fromColumn == -1 || move.fromColumn == testColumn) &&
                pieceGrid[testRow][testColumn] != null &&
                !pieceGrid[testRow][testColumn].hasType(Type.HOLE)) {
                    directionSearch = testSquare(move, testRow, testColumn);
            }
        }
    }

    private boolean testSquare(final Move move, final int testRow, final int testColumn) {
        boolean continueSearch = true;
        if (pieceGrid[testRow][testColumn].hasType(move.getType()) &&
                pieceGrid[testRow][testColumn].hasColor(move.getColor())) {
            final boolean pieceFound = isPieceFound(move, testRow, testColumn);
            if (pieceFound) {
                move.fromRow = testRow;
                move.fromColumn = testColumn;
                continueSearch = false;
            }
        } else {
            continueSearch = false;
        }
        return continueSearch;
    }

    private boolean isPieceFound(final Move move, final int testRow, final int testColumn) {
        boolean pieceFound = true;
        if (move.isRelayed()) {
            // find relayed knight
            pieceFound = false;
            for (final int[] knightArray : Type.KNIGHT.getMoveDirectionArray()) {
                final int knightRow = testRow + knightArray[0];
                final int knightColumn = testColumn + knightArray[1];
                if (isValidSquare(knightRow, knightColumn) &&
                        pieceGrid[knightRow][knightColumn] != null &&
                        pieceGrid[knightRow][knightColumn].hasType(Type.KNIGHT) &&
                        pieceGrid[knightRow][knightColumn].hasColor(move.getColor())) {
                    pieceFound = true;
                }
            }
        }
        return pieceFound;
    }

}
