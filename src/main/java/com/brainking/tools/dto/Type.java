package com.brainking.tools.dto;

public enum Type {

    KING("K", true, true) {
        @Override
        public int[][] getMoveDirectionArray() {
            return KING_MOVE_DIRECTION;
        }

        @Override
        public int getMaxMoveDistance() {
            return 1;
        }
    },
    QUEEN("Q", true, true) {
        @Override
        public int[][] getMoveDirectionArray() {
            return QUEEN_MOVE_DIRECTION;
        }

        @Override
        public int getMaxMoveDistance() {
            return 7;
        }
    },
    ROOK("R", true, true) {
        @Override
        public int[][] getMoveDirectionArray() {
            return ROOK_MOVE_DIRECTION;
        }

        @Override
        public int getMaxMoveDistance() {
            return 7;
        }
    },
    BISHOP("B", true, true) {
        @Override
        public int[][] getMoveDirectionArray() {
            return BISHOP_MOVE_DIRECTION;
        }

        @Override
        public int getMaxMoveDistance() {
            return 7;
        }
    },
    KNIGHT("N", true, true) {
        @Override
        public int[][] getMoveDirectionArray() {
            return KNIGHT_MOVE_DIRECTION;
        }

        @Override
        public int getMaxMoveDistance() {
            return 1;
        }
    },
    PAWN("p", true, true) {
        @Override
        public int[][] getMoveDirectionArray() {
            return null;  // special rules
        }

        @Override
        public int getMaxMoveDistance() {
            return 0;  // special rules
        }
    },
    ARCHBISHOP("A", true, true) {
        @Override
        public int[][] getMoveDirectionArray() {
            return ARCHBISHOP_MOVE_DIRECTION;
        }

        @Override
        public int getMaxMoveDistance() {
            return 7;
        }
    },
    CHANCELLOR("C", true, true) {
        @Override
        public int[][] getMoveDirectionArray() {
            return CHANCELLOR_MOVE_DIRECTION;
        }

        @Override
        public int getMaxMoveDistance() {
            return 7;
        }
    },
    JANUS("J", true, true) {
        @Override
        public int[][] getMoveDirectionArray() {
            return ARCHBISHOP_MOVE_DIRECTION;
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

    Type(String code, boolean playable, boolean visible) {
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

    public static Type findByCode(String code) {
        for (Type value : values()) {
            if (value.code.equalsIgnoreCase(code)) {
                return value;
            }
        }
        return null;
    }

    public abstract int[][] getMoveDirectionArray();

    public abstract int getMaxMoveDistance();

}
