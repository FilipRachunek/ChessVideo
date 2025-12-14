package com.brainking.tools.dto;

import com.brainking.tools.utils.Constants;

import java.util.Arrays;

public class Piece {

    private Color color;
    private Type type;
    private int pieceX;
    private int pieceY;
    private int[] movingX;
    private int[] movingY;
    private int moveStepIndex;

    public static Piece from(final Piece from) {
        return new Piece(from.color, from.type);
    }

    public Piece(final Type type) {
        // for neutral pieces (holes, ice cubes)
        this.type = type;
    }

    public Piece(final Color color, final Type type) {
        this.color = color;
        this.type = type;
    }

    public void setXY(final Game game, final int column, final int row) {
        pieceX = Constants.getBoardX(game) + column * Constants.getSquareSize(game);
        pieceY = Constants.getBoardY(game) + (game.getHeight() - 1 - row) * Constants.getSquareSize(game);
    }

    public void setXY(final Game game, final Color color, final int index) {
        final int boardX = Constants.getBoardX(game);
        final int boardY = Constants.getBoardY(game);
        final int squareSize = Constants.getSquareSize(game);
        final int capturedPieceSize = Constants.getCapturedPieceSize(game);
        int step = capturedPieceSize;
        pieceX = boardX - squareSize;
        pieceY = boardY;
        if (color == Color.BLACK) {
            pieceX = boardX + (game.getWidth() + 1) * squareSize - capturedPieceSize;
            pieceY = boardY + game.getHeight() * squareSize - capturedPieceSize;
            step = -step;
        }
        pieceY += step * index;
    }

    public void setTargetXYAndSteps(final Game game, final int toColumn, final int toRow) {
        final int squareSize = Constants.getSquareSize(game);
        final int targetX = Constants.getBoardX(game) + toColumn * squareSize;
        final int targetY = Constants.getBoardY(game) + (game.getHeight() - 1 - toRow) * squareSize;
        movingX = new int[Constants.MOVE_STEPS];
        movingY = new int[Constants.MOVE_STEPS];
        for (int i = 0; i < Constants.MOVE_STEPS; i++) {
            movingX[i] = pieceX + i * (targetX - pieceX) / Constants.MOVE_STEPS;
            movingY[i] = pieceY + i * (targetY - pieceY) / Constants.MOVE_STEPS;
        }
        movingX[Constants.MOVE_STEPS - 1] = targetX;
        movingY[Constants.MOVE_STEPS - 1] = targetY;
    }

    public void promoteTo(final Type newType) {
        type = newType;
    }

    public int getX() {
        return pieceX;
    }

    public int getY() {
        return pieceY;
    }

    public boolean isMoving() {
        return movingX != null && movingY != null;
    }

    public void doMoveStep() {
        pieceX = movingX == null ? -1 : movingX[moveStepIndex];
        pieceY = movingY == null ? -1 : movingY[moveStepIndex];
        moveStepIndex++;
        if (moveStepIndex == Constants.MOVE_STEPS) {
            stopMoving();
        }
    }

    @SuppressWarnings("PMD.NullAssignment")
    public void stopMoving() {
        movingX = null;
        movingY = null;
        moveStepIndex = 0;
    }

    public boolean isWhite() {
        return color == Color.WHITE;
    }

    public void setOppositeColor() {
        color = color == Color.WHITE ? Color.BLACK : Color.WHITE;
    }

    public boolean isNeutral() {
        return color == null;
    }

    public Type getType() {
        return type;
    }

    public String getCode() {
        return type.getCode();
    }

    public boolean isVisible() {
        return type.isVisible();
    }

    public boolean isPawn() {
        return type == Type.PAWN;
    }

    public boolean isHole() {
        return type == Type.HOLE;
    }

    public boolean hasType(final Type type) {
        return this.type == type;
    }

    public boolean hasColor(final Color color) {
        return this.color == color;
    }

    public Color getColor() {
        return color;
    }

    public boolean isPlayable() {
        return type.isPlayable();
    }

    public int[][] getMoveDirectionArray() {
        return type.getMoveDirectionArray();
    }

    public int getMaxMoveDistance() {
        return type.getMaxMoveDistance();
    }

    @Override
    public String toString() {
        return "Piece{" +
                "color=" + color +
                ", type=" + type +
                ", pieceX=" + pieceX +
                ", pieceY=" + pieceY +
                ", movingX=" + Arrays.toString(movingX) +
                ", movingY=" + Arrays.toString(movingY) +
                ", moveStepIndex=" + moveStepIndex +
                '}';
    }
}
