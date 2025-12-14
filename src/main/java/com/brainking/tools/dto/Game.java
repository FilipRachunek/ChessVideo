package com.brainking.tools.dto;

import com.brainking.tools.utils.Constants;

import java.awt.image.BufferedImage;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Game {

    private static final Logger LOG = LoggerFactory.getLogger(Game.class);
    private static final DateTimeFormatter INPUT = DateTimeFormatter.ofPattern("yyyy.MM.dd");
    private static final DateTimeFormatter OUTPUT = DateTimeFormatter.ofPattern("d. MMMM yyyy", Locale.ENGLISH);
    private static final String RESULT = "Result";

    private final String name;
    private final Map<String, String> metadataMap = new HashMap<>();
    private final List<Move> moves = new ArrayList<>();
    private String pgnCode;
    private int width;
    private int height;
    private Color orientation;
    private Map<String, BufferedImage> whitePieceMap;
    private Map<String, BufferedImage> blackPieceMap;

    public Game(final String name) {
        this.name = name;
        width = 8;
        height = 8;
    }

    public void addPieceMaps(final Map<String, BufferedImage> whitePieceMap,
                             final Map<String, BufferedImage> blackPieceMap) {
        this.whitePieceMap = whitePieceMap;
        this.blackPieceMap = blackPieceMap;
    }

    public BufferedImage getFromPieceMap(final Color color, final String key) {
        return switch (color) {
            case Color.WHITE -> whitePieceMap.get(key);
            case Color.BLACK -> blackPieceMap.get(key);
        };
    }

    public void addMetadata(final String key, final String value) {
        LOG.debug(key + ": " + value);
        metadataMap.put(key, value);
    }

    public void addMove(final Move move) {
        moves.add(move);
    }

    public void setOrientationFromResult() {
        orientation = "0-1".equals(metadataMap.get(RESULT)) ? Color.BLACK : Color.WHITE;
    }

    public void setDimensionFromVariant() {
        if (isVariant(Constants.LOS_ALAMOS)) {
            width = 6;
            height = 6;
        } else if (isVariant(Constants.EMBASSY) || isVariant(Constants.JANUS) || isVariant(Constants.CAPABLANCA_RANDOM)) {
            width = 10;
            height = 8;
        } else if (isVariant(Constants.GRAND)) {
            width = 10;
            height = 10;
        }
    }

    public void setPgnCode(final String pgnCode) {
        this.pgnCode = pgnCode;
    }

    public String getPgnCode() {
        return pgnCode;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean whiteWon() {
        return "1-0".equals(metadataMap.get(RESULT));
    }

    public boolean blackWon() {
        return "0-1".equals(metadataMap.get(RESULT));
    }

    public boolean draw() {
        return "1/2-1/2".equals(metadataMap.get(RESULT));
    }

    public boolean hasOppositeOrientation() {
        return orientation == Color.BLACK;
    }

    public String getName() {
        return name;
    }

    public List<Move> getMoves() {
        return moves;
    }

    public String getFEN() {
        return metadataMap.get("FEN");
    }

    public String getVariant() {
        return metadataMap.get("Variant");
    }

    public boolean isVariant(final String variant) {
        return variant.equalsIgnoreCase(getVariant());
    }

    public String getEvent() {
        return metadataMap.get("Event");
    }

    public String getSite() {
        return metadataMap.get("Site");
    }

    public String getDate() {
        return metadataMap.get("Date");
    }

    public Optional<String> getFormattedDate() {
        return Optional.ofNullable(getDate())
                .filter(s -> !s.isBlank())
                .map(s -> LocalDate.parse(s, INPUT))
                .map(OUTPUT::format);
    }

    public String getWhite() {
        return metadataMap.get("White");
    }

    public String getBlack() {
        return metadataMap.get("Black");
    }

    public String getResult() {
        return metadataMap.get(RESULT);
    }

}
