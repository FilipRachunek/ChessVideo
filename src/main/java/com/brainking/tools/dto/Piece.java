package com.brainking.tools.dto;

import com.brainking.tools.utils.Constants;

import java.util.Arrays;

public class Piece {

    private Color color;
    private Type type;
    private int x, y;
    private int[] movingX, movingY;
    private int moveStepIndex;
    private boolean visible;

    public static Piece from(Piece from) {
        return new Piece(from.color, from.type);
    }

    public Piece(Type type) {
        // for neutral pieces (holes, ice cubes)
        this.type = type;
    }

    public Piece(Color color, Type type) {
        this.color = color;
        this.type = type;
    }

    public void setXY(Game game, int column, int row) {
        x = Constants.getBoardX(game) + column * Constants.getSquareSize(game);
        y = Constants.getBoardY(game) + (game.getHeight() - 1 - row) * Constants.getSquareSize(game);
    }

    public void setXY(Game game, Color color, int index) {
        int boardX = Constants.getBoardX(game);
        int boardY = Constants.getBoardY(game);
        int squareSize = Constants.getSquareSize(game);
        int capturedPieceSize = Constants.getCapturedPieceSize(game);
        int step = capturedPieceSize;
        x = boardX - squareSize;
        y = boardY;
        if (color == Color.BLACK) {
            x = boardX + (game.getWidth() + 1) * squareSize - capturedPieceSize;
            y = boardY + game.getHeight() * squareSize - capturedPieceSize;
            step = -step;
        }
        y += step * index;
    }

    public void setTargetXYAndSteps(Game game, int toColumn, int toRow) {
        int squareSize = Constants.getSquareSize(game);
        int targetX = Constants.getBoardX(game) + toColumn * squareSize;
        int targetY = Constants.getBoardY(game) + (game.getHeight() - 1 - toRow) * squareSize;
        movingX = new int[Constants.MOVE_STEPS];
        movingY = new int[Constants.MOVE_STEPS];
        for (int i = 0; i < Constants.MOVE_STEPS; i++) {
            movingX[i] = x + i * (targetX - x) / Constants.MOVE_STEPS;
            movingY[i] = y + i * (targetY - y) / Constants.MOVE_STEPS;
        }
        movingX[Constants.MOVE_STEPS - 1] = targetX;
        movingY[Constants.MOVE_STEPS - 1] = targetY;
    }

    public void promoteTo(Type newType) {
        type = newType;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean isMoving() {
        return movingX != null && movingY != null;
    }

    public void doMoveStep() {
        x = movingX[moveStepIndex];
        y = movingY[moveStepIndex];
        moveStepIndex++;
        if (moveStepIndex == Constants.MOVE_STEPS) {
            stopMoving();
        }
    }

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

    public boolean hasType(Type type) {
        return this.type == type;
    }

    public boolean hasColor(Color color) {
        return this.color == color;
    }

    public Color getColor() {
        return color;
    }

    public boolean isPlayable() {
        return type.isPlayable();
    }

    @Override
    public String toString() {
        return "Piece{" +
                "color=" + color +
                ", type=" + type +
                ", x=" + x +
                ", y=" + y +
                ", movingX=" + Arrays.toString(movingX) +
                ", movingY=" + Arrays.toString(movingY) +
                ", moveStepIndex=" + moveStepIndex +
                ", visible=" + visible +
                '}';
    }
}
