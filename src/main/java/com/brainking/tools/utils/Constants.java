package com.brainking.tools.utils;

import com.brainking.tools.dto.Game;

import java.awt.*;

public class Constants {

    public static final int VIDEO_WIDTH = 1080;
    public static final int VIDEO_HEIGHT = 1080;
    public static final int SCREENSHOT_HEIGHT = 900;
    public static final int BOARD_SIZE = 800;
    public static final int MOVE_STEPS = 30;
    public static final int FRAMES_BETWEEN_MOVES = 15;
    public static final int FRAMES_TO_SHOW_TARGET = 25;
    public static final int FRAMES_AFTER_LAST_MOVE = 150;
    public static final Color SCREENSHOT = Color.WHITE;
    public static final Color CROSS_HATCHED_SQUARE = Color.BLACK;
    public static final Color BACKGROUND = new Color(170, 212, 163);
    public static final Color LIGHT_SQUARE = new Color(237, 208, 174);
    public static final Color DARK_SQUARE = new Color(133, 96, 52);
    public static final Color HIGHLIGHT_MOVE = new Color(0, 255, 0, 100);
    public static final Color HIGHLIGHT_TARGET = new Color(150, 150, 255);
    public static final Color HIGHLIGHT_CHECK = new Color(255, 0, 0, 100);
    public static final Color INVISIBLE_SQUARE = Color.BLACK;

    private static final String[] ROW_ARRAY = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
    private static final String[] COLUMN_ARRAY = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j"};

    public static final String EXTINCTION = "Extinction";
    public static final String THREE_CHECKS = "Three Checks";
    public static final String ANTI = "Anti";
    public static final String KNIGHT_RELAY = "Knight Relay";
    public static final String ATOMIC = "Atomic";
    public static final String CHESHIRE_CAT = "Cheshire Cat";
    public static final String RACING_KINGS = "Racing Kings";
    public static final String KNIGHTMATE = "Knightmate";
    public static final String CORNER = "Corner";
    public static final String FORTRESS = "Fortress";
    public static final String HORDE = "Horde";
    public static final String FISCHER_RANDOM = "Fischer Random";
    public static final String ICE_AGE = "Ice Age";
    public static final String AMBIGUOUS = "Ambiguous";
    public static final String LOS_ALAMOS = "Los Alamos";
    public static final String LEGAN = "Legan";
    public static final String SCREEN = "Screen";
    public static final String CRAZY_SCREEN = "Crazy Screen";
    public static final String LOOP = "Loop";
    public static final String EMBASSY = "Embassy";
    public static final String GRAND = "Grand";
    public static final String MASSACRE = "Massacre";
    public static final String JANUS = "Janus";
    public static final String CAPABLANCA_RANDOM = "Capablanca Random";
    public static final String DARK = "Dark";

    public static String getRow(final int index) {
        return ROW_ARRAY[index];
    }

    public static String getColumn(final int index) {
        return COLUMN_ARRAY[index];
    }

    public static int getSquareSize(final Game game) {
        return BOARD_SIZE / Math.max(game.getWidth(), game.getHeight());
    }

    public static int getCapturedPieceSize(final Game game) {
        return getSquareSize(game) / 2;
    }

    public static int getBoardX(final Game game) {
        return VIDEO_WIDTH / 2 - getSquareSize(game) * game.getWidth() / 2;
    }

    public static int getBoardY(final Game game) {
        return VIDEO_HEIGHT / 2 - getSquareSize(game) * game.getHeight() / 2;
    }

    public static int getScreenshotBoardY(final Game game) {
        return SCREENSHOT_HEIGHT / 2 - getSquareSize(game) * game.getHeight() / 2;
    }

}
