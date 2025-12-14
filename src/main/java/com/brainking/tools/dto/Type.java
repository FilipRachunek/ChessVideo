package com.brainking.tools.dto;

import java.util.Arrays;

public enum Type {

    KING("K", true, true) {
        @Override
        public int[][] getMoveDirectionArray() {
            return Arrays.copyOf(KING_MOVE_DIRECTION, KING_MOVE_DIRECTION.length);
        }

        @Override
        public int getMaxMoveDistance() {
            return 1;
        }
    },
    QUEEN("Q", true, true) {
        @Override
        public int[][] getMoveDirectionArray() {
            return Arrays.copyOf(QUEEN_MOVE_DIRECTION, QUEEN_MOVE_DIRECTION.length);
        }

        @Override
        public int getMaxMoveDistance() {
            return 7;
        }
    },
    ROOK("R", true, true) {
        @Override
        public int[][] getMoveDirectionArray() {
            return Arrays.copyOf(ROOK_MOVE_DIRECTION, ROOK_MOVE_DIRECTION.length);
        }

        @Override
        public int getMaxMoveDistance() {
            return 7;
        }
    },
    BISHOP("B", true, true) {
        @Override
        public int[][] getMoveDirectionArray() {
            return Arrays.copyOf(BISHOP_MOVE_DIRECTION, BISHOP_MOVE_DIRECTION.length);
        }

        @Override
        public int getMaxMoveDistance() {
            return 7;
        }
    },
    KNIGHT("N", true, true) {
        @Override
        public int[][] getMoveDirectionArray() {
            return Arrays.copyOf(KNIGHT_MOVE_DIRECTION, KNIGHT_MOVE_DIRECTION.length);
        }

        @Override
        public int getMaxMoveDistance() {
            return 1;
        }
    },
    PAWN("p", true, true) {
        @Override
        public int[][] getMoveDirectionArray() {
            return new int[0][];  // special rules
        }

        @Override
        public int getMaxMoveDistance() {
            return 0;  // special rules
        }
    },
    ARCHBISHOP("A", true, true) {
        @Override
        public int[][] getMoveDirectionArray() {
            return Arrays.copyOf(ARCHBISHOP_MOVE_DIRECTION, ARCHBISHOP_MOVE_DIRECTION.length);
        }

        @Override
        public int getMaxMoveDistance() {
            return 7;
        }
    },
    CHANCELLOR("C", true, true) {
        @Override
        public int[][] getMoveDirectionArray() {
            return Arrays.copyOf(CHANCELLOR_MOVE_DIRECTION, CHANCELLOR_MOVE_DIRECTION.length);
        }

        @Override
        public int getMaxMoveDistance() {
            return 7;
        }
    },
    JANUS("J", true, true) {
        @Override
        public int[][] getMoveDirectionArray() {
            return Arrays.copyOf(ARCHBISHOP_MOVE_DIRECTION, ARCHBISHOP_MOVE_DIRECTION.length);
        }

        @Override
        public int getMaxMoveDistance() {
            return 7;
        }
    },
    HOLE("h", false, false) {
        @Override
        public int[][] getMoveDirectionArray() {
            return new int[0][];
        }

        @Override
        public int getMaxMoveDistance() {
            return 0;
        }
    },
    ICE_CUBE("i", false, true) {
        @Override
        public int[][] getMoveDirectionArray() {
            return new int[0][];
        }

        @Override
        public int getMaxMoveDistance() {
            return 0;
        }
    };

    private static final int[][] KING_MOVE_DIRECTION = {
            {1, 1}, {1, 0}, {1, -1}, {0, 1}, {0, -1}, {-1, 1}, {-1, 0}, {-1, -1}
    };

    private static final int[][] QUEEN_MOVE_DIRECTION = {
            {1, 1}, {1, 0}, {1, -1}, {0, 1}, {0, -1}, {-1, 1}, {-1, 0}, {-1, -1}
    };

    private static final int[][] ROOK_MOVE_DIRECTION = {
            {1, 0}, {0, 1}, {0, -1}, {-1, 0}
    };

    private static final int[][] BISHOP_MOVE_DIRECTION = {
            {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
    };

    private static final int[][] KNIGHT_MOVE_DIRECTION = {
            {1, 2}, {1, -2}, {-1, 2}, {-1, -2}, {2, 1}, {2, -1}, {-2, 1}, {-2, -1}
    };

    private static final int[][] ARCHBISHOP_MOVE_DIRECTION = {
            {1, 2}, {1, -2}, {-1, 2}, {-1, -2}, {2, 1}, {2, -1}, {-2, 1}, {-2, -1},
            {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
    };

    private static final int[][] CHANCELLOR_MOVE_DIRECTION = {
            {1, 2}, {1, -2}, {-1, 2}, {-1, -2}, {2, 1}, {2, -1}, {-2, 1}, {-2, -1},
            {1, 0}, {0, 1}, {0, -1}, {-1, 0}
    };

    private final String code;
    private final boolean playable;
    private final boolean visible;

    Type(final String code, final boolean playable, final boolean visible) {
        this.code = code;
        this.playable = playable;
        this.visible = visible;
    }

    public String getCode() {
        return code;
    }

    public boolean isPlayable() {
        return playable;
    }

    public boolean isVisible() {
        return visible;
    }

    public static Type findByCode(final String code) {
        Type result = null;
        for (final Type value : values()) {
            if (value.code.equalsIgnoreCase(code)) {
                result = value;
            }
        }
        return result;
    }

    public abstract int[][] getMoveDirectionArray();

    public abstract int getMaxMoveDistance();

}
