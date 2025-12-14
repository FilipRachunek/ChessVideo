package com.brainking.tools.dto;

import com.brainking.tools.utils.Constants;

public class Move {

    private static final String CHECK = "+";
    private static final String CHECKMATE = "#";
    private static final String PROMOTION = "=";
    private static final String RELAY = "R";
    private static final String PLACE = "@";
    private static final String CAPTURE = "x";

    private int moveNumber;
    private String pgnCode;
    private Color color;
    private Type type;
    private Type promoteType;
    protected int fromRow = -1;
    protected int fromColumn = -1;
    protected int toRow = -1;
    protected int toColumn = -1;
    private boolean check;
    private boolean castlingShort;
    private boolean castlingLong;
    private boolean capture;
    private boolean checkmate;
    private boolean relayed;
    private boolean ambiguous;
    private boolean place;
    private String screenshotId;

    public Move() {
    }

    public Move(final Game game, final int moveNumber, final String pgnCode, final Color color) {
        this.moveNumber = moveNumber;
        this.pgnCode = pgnCode;
        this.color = color;
        checkScreenshotId(pgnCode);
        setMoveParameters(game, pgnCode, color);
    }

    public static Move from(final Move from) {
        final Move move = new Move();
        move.moveNumber = from.moveNumber;
        move.pgnCode = from.pgnCode;
        move.color = from.color;
        move.type = from.type;
        move.promoteType = from.promoteType;
        move.fromRow = from.fromRow;
        move.fromColumn = from.fromColumn;
        move.toRow = from.toRow;
        move.toColumn = from.toColumn;
        move.check = from.check;
        move.castlingShort = from.castlingShort;
        move.castlingLong = from.castlingLong;
        move.capture = from.capture;
        move.checkmate = from.checkmate;
        move.ambiguous = from.ambiguous;
        move.place = from.place;
        return move;
    }

    public boolean isEmpty() {
        return pgnCode == null && fromRow == -1;
    }

    public int getMoveNumber() {
        return moveNumber;
    }

    public String getPgnCode() {
        return pgnCode;
    }

    public Type getType() {
        return type;
    }

    public boolean isCastlingType(final String variant) {
        return Constants.KNIGHTMATE.equals(variant) && type == Type.KNIGHT ||
                !Constants.KNIGHTMATE.equals(variant) && type == Type.KING;
    }

    public boolean isPawn() {
        return type == Type.PAWN;
    }

    public Color getColor() {
        return color;
    }

    public boolean isWhite() {
        return color == Color.WHITE;
    }

    public boolean isBlack() {
        return color == Color.BLACK;
    }

    public Color getOppositeColor() {
        return color == Color.WHITE ? Color.BLACK : Color.WHITE;
    }

    public Type getPromoteType() {
        return promoteType;
    }

    public boolean isCheck() {
        return check;
    }

    public boolean isCapture() {
        return capture;
    }

    public boolean isCastlingShort() {
        return castlingShort;
    }

    public boolean isCastlingLong() {
        return castlingLong;
    }

    public boolean isCheckmate() {
        return checkmate;
    }

    public boolean isRelayed() {
        return relayed;
    }

    public boolean isAmbiguous() {
        return ambiguous;
    }

    public boolean isPlace() {
        return place;
    }

    public String getScreenshotId() {
        return screenshotId;
    }

    public void clearSquares() {
        fromColumn = -1;
        fromRow = -1;
        toColumn = -1;
        toRow = -1;
    }

    private void checkScreenshotId(final String pgnCode) {
        // check screenshot ID
        if (pgnCode.endsWith("]")) {
            final int index = pgnCode.indexOf('[');
            screenshotId = pgnCode.substring(index + 1, pgnCode.length() - 1);
            this.pgnCode = pgnCode.substring(0, index);
        }
    }

    private void setMoveParameters(final Game game, final String pgnCode, final Color color) {
        // get piece type and target square
        final String[] array = pgnCode.split("");
        int index = array.length - 1;
        if (CHECK.equals(array[index]) || CHECKMATE.equals(array[index])) {
            check = true;
            if (CHECKMATE.equals(array[index])) {
                checkmate = true;
            }
            index--;
        }
        if (pgnCode.startsWith("?-")) {
            // ?-e4
            ambiguous = true;
            toColumn = getColumnIndex(array[2]);
            toRow = getRowIndex(array[3]);
        } else if (pgnCode.startsWith("O-O-O")) {
            castlingLong = true;
            type = game.isVariant(Constants.KNIGHTMATE) ? Type.KNIGHT : Type.KING;
            fromRow = color == Color.WHITE ? 0 : game.getHeight() - 1;
            fromColumn = 4;
            toRow = fromRow;
            toColumn = game.isVariant(Constants.EMBASSY) ? 7 : 2;
            if (game.isVariant(Constants.JANUS)) {
                toColumn = 8;
            }
        } else if (pgnCode.startsWith("O-O")) {
            castlingShort = true;
            type = game.isVariant(Constants.KNIGHTMATE) ? Type.KNIGHT : Type.KING;
            fromRow = color == Color.WHITE ? 0 : game.getHeight() - 1;
            fromColumn = 4;
            toRow = fromRow;
            toColumn = game.isVariant(Constants.EMBASSY) || game.isVariant(Constants.JANUS) ? 1 : 6;
        } else {
            handleOtherSymbols(array, index);
        }
    }

    private void handleOtherSymbols(final String[] array, final int previousIndex) {
        int index = getIndexAfterPromoting(array, previousIndex);
        index = getIndexAfterRelaying(array, index);
        index = getIndexAfterMoving(array, index);
        // simple pawn moves would end here, so let's check if the index is not already at the start
        if (index > 0 && PLACE.equals(array[index])) {
            getIndexAfterPlacing(array, index);
        } else if (index > 0 && CAPTURE.equals(array[index])) {
            getIndexAfterCapturing(array, index);
        } else {
            finishTheMove(array, index);
        }
    }

    private int getIndexAfterPromoting(final String[] array, final int previousIndex) {
        // handle pawn promoting
        int index = previousIndex;
        if (PROMOTION.equals(array[index - 1])) {
            // e8=Q (sample promotion)
            promoteType = Type.findByCode(array[index]);
            index--;
            index--;
        }
        return index;
    }

    private int getIndexAfterRelaying(final String[] array, final int previousIndex) {
        // handle knight relay
        int index = previousIndex;
        if (RELAY.equals(array[index])) {
            // e4R
            relayed = true;
            index--;
        }
        return index;
    }

    private int getIndexAfterMoving(final String[] array, final int previousIndex) {
        // after we removed the check and promotion, the move ends with the target square
        int index = previousIndex;
        toRow = getRowIndex(array[index - 1] + array[index]);
        if (toRow >= 0) {  // row 10 detected, two digits
            index--;
        } else {
            toRow = getRowIndex(array[index]);
        }
        index--;
        toColumn = getColumnIndex(array[index]);
        index--;
        return index;
    }

    private int getIndexAfterPlacing(final String[] array, final int previousIndex) {
        // place extra piece
        int index = previousIndex;
        place = true;
        index--;
        type = Type.findByCode(array[index]);
        return index;
    }

    private int getIndexAfterCapturing(final String[] array, final int previousIndex) {
        // // after removing the target square, the move should end with a capture sign
        int index = previousIndex;
        capture = true;
        index--;
        final int fromColumnIndex = getColumnIndex(array[index]);
        final int fromRowIndex = getRowIndex(array[index]);
        if (fromColumnIndex >= 0) {
            // ?dxe4 (sample capture)
            fromColumn = fromColumnIndex;
            index--;
            if (index < 0) {
                // dxe4
                type = Type.PAWN;
            } else {
                // Ndxe4
                type = Type.findByCode(array[index]);
            }
        } else if (fromRowIndex >= 0) {
            // N6xd4
            fromRow = fromRowIndex;
            index--;
            type = Type.findByCode(array[index]);
        } else {
            type = Type.findByCode(array[index]);
        }
        return index;
    }

    private void finishTheMove(final String[] array, final int previousIndex) {
        int index = previousIndex;
        if (index < 0) {
            type = Type.PAWN;
        } else {
            final int fromColumnIndex = getColumnIndex(array[index]);
            final int fromRowIndex = getRowIndex(array[index]);
            if (fromColumnIndex >= 0) {
                fromColumn = fromColumnIndex;
                index--;
            } else if (fromRowIndex >= 0) {
                fromRow = fromRowIndex;
                index--;
            }
            type = Type.findByCode(array[index]);
        }
    }

    private int getColumnIndex(final String columnCode) {
        int index = -1;
        for (int i = 0; i < 10; i++) {
            if (Constants.COLUMN_ARRAY[i].equals(columnCode)) {
                index = i;
                break;
            }
        }
        return index;
    }

    private int getRowIndex(final String rowCode) {
        int index = -1;
        for (int i = 0; i < 10; i++) {
            if (Constants.ROW_ARRAY[i].equals(rowCode)) {
                index = i;
                break;
            }
        }
        return index;
    }

    @Override
    public String toString() {
        return "moveNumber: " + moveNumber +
                ", pgnCode: " + pgnCode +
                ", color: " + color +
                ", type: " + type +
                ", squares: [" + fromRow + ", " + fromColumn + ", " + toRow + ", " + toColumn + "]";
    }

}
