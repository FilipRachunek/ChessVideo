package com.brainking.tools.dto;

import com.brainking.tools.utils.Constants;

import java.awt.image.BufferedImage;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Game {

    private static final Logger log = LoggerFactory.getLogger(Game.class);

    private final String name;
    private final Map<String, String> metadataMap = new HashMap<>();
    private final List<Move> moves = new ArrayList<>();
    private String pgnCode;
    private int width, height;
    private Color orientation;
    private Map<String, BufferedImage> whitePieceMap;
    private Map<String, BufferedImage> blackPieceMap;
    private Map<String, BufferedImage> neutralPieceMap;

    public Game(String name) {
        this.name = name;
        width = 8;
        height = 8;
    }

    public void addPieceMaps(Map<String, BufferedImage> whitePieceMap,
                             Map<String, BufferedImage> blackPieceMap,
                             Map<String, BufferedImage> neutralPieceMap) {
        this.whitePieceMap = whitePieceMap;
        this.blackPieceMap = blackPieceMap;
        this.neutralPieceMap = neutralPieceMap;
    }

    public BufferedImage getFromPieceMap(Color color, String key) {
        if (color == Color.WHITE) {
            return whitePieceMap.get(key);
        }
        if (color == Color.BLACK) {
            return blackPieceMap.get(key);
        }
        return neutralPieceMap.get(key);
    }

    public void addMetadata(String key, String value) {
        log.debug(key + ": " + value);
        metadataMap.put(key, value);
    }

    public void addMove(Move move) {
        moves.add(move);
    }

    public void setOrientationFromResult() {
        orientation = "0-1".equals(metadataMap.get("Result")) ? Color.BLACK : Color.WHITE;
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

    public void setPgnCode(String pgnCode) {
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
        return "1-0".equals(metadataMap.get("Result"));
    }

    public boolean blackWon() {
        return "0-1".equals(metadataMap.get("Result"));
    }

    public boolean draw() {
        return "1/2-1/2".equals(metadataMap.get("Result"));
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

    public boolean isVariant(String variant) {
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

    public String getFormattedDate() {
        try {
            LocalDate localDate = LocalDate.parse(getDate(), DateTimeFormatter.ofPattern("yyyy.MM.dd"));
            return DateTimeFormatter.ofPattern("d. MMMM yyyy").format(localDate);
        } catch (Exception ex) {
            return "";
        }
    }

    public String getWhite() {
        return metadataMap.get("White");
    }

    public String getBlack() {
        return metadataMap.get("Black");
    }

    public String getResult() {
        return metadataMap.get("Result");
    }

}
