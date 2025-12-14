package com.brainking.tools.services;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.TexturePaint;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.TranscodingHints;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.util.SVGConstants;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.brainking.tools.dto.Game;
import com.brainking.tools.dto.Move;
import com.brainking.tools.dto.Notation;
import com.brainking.tools.dto.Piece;
import com.brainking.tools.dto.Position;
import com.brainking.tools.utils.Constants;
import com.brainking.tools.utils.Fonts;

@Service
public class RenderService {

    private static final Logger LOG = LoggerFactory.getLogger(RenderService.class);

    public void renderChessBoard(Graphics2D g, Game game, Position position, boolean screenshot) {
        int boardX = Constants.getBoardX(game);
        int boardY = screenshot ? Constants.getScreenshotBoardY(game) : Constants.getBoardY(game);
        int yShift = game.hasOppositeOrientation() ? Constants.getBoardY(game) - boardY : boardY - Constants.getBoardY(game);
        int squareSize = Constants.getSquareSize(game);
        for (int i = 0; i < game.getWidth(); i++) {
            for (int j = 0; j < game.getHeight(); j++) {
                int row = game.hasOppositeOrientation() ? j : game.getHeight() - 1 - j;
                int column = game.hasOppositeOrientation() ? game.getWidth() - 1 - i : i;
                if (!position.matchesHole(row, column)) {
                    int x = boardX + i * squareSize;
                    int y = boardY + j * squareSize;
                    boolean lightSquare = (i + j) % 2 == 0;
                    if (screenshot) {
                        if (!lightSquare) {
                            fillCrossHatchedSquare(g, x, y, squareSize);
                        }
                    } else {
                        g.setColor(lightSquare ? Constants.LIGHT_SQUARE : Constants.DARK_SQUARE);
                        g.fillRect(x, y, squareSize, squareSize);
                    }
                    // highlight current move
                    if (position.matchesCurrentMove(row, column)) {
                        g.setColor(Constants.HIGHLIGHT_MOVE);
                        g.fillRect(x, y, squareSize, squareSize);
                    }
                    // highlight target (Ambiguous Chess)
                    if (position.matchesTargetSquare(row, column)) {
                        g.setColor(Constants.HIGHLIGHT_TARGET);
                        g.fillRect(x, y, squareSize, squareSize);
                    }
                    // highlight king in check
                    if (!screenshot && position.matchesPieceInCheck(row, column)) {
                        g.setColor(Constants.HIGHLIGHT_CHECK);
                        g.fillRect(x, y, squareSize, squareSize);
                    }
                }
            }
        }
        // render a border with column letters and row numbers
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Font font = Fonts.ROW_COLUMN_FONT;
        g.setFont(font);
        FontMetrics fontMetrics = g.getFontMetrics(font);
        int ascent = fontMetrics.getAscent();
        int space = 10;
        g.setColor(Color.BLACK);
        for (int row = 0; row < game.getHeight(); row++) {
            int index = game.hasOppositeOrientation() ? game.getHeight() - 1 - row : row;
            int y = boardY + (game.getHeight() - row) * squareSize - (squareSize - ascent) / 2;
            g.drawString(Constants.ROW_ARRAY[index], boardX - 30, y);
            g.drawString(Constants.ROW_ARRAY[index], boardX + game.getWidth() * squareSize + space, y);
        }
        for (int column = 0; column < game.getWidth(); column++) {
            int index = game.hasOppositeOrientation() ? game.getWidth() - 1 - column : column;
            String string = Constants.COLUMN_ARRAY[index].toUpperCase();
            int x = boardX + column * squareSize + (squareSize - fontMetrics.stringWidth(string)) / 2;
            g.drawString(string, x, boardY - space);
            g.drawString(string, x, boardY + game.getHeight() * squareSize + ascent);
        }
        // render pieces
        Piece[][] pieceGrid = position.getPieceGrid();
        List<Piece> movingPieces = new ArrayList<>();  // there could be more than one, e.g. castling
        for (int row = 0; row < game.getHeight(); row++) {
            for (int column = 0; column < game.getWidth(); column++) {
                Piece piece = pieceGrid[row][column];
                if (piece != null) {
                    if (piece.isMoving()) {
                        movingPieces.add(piece);
                    } else {
                        renderPiece(g, game, piece, yShift);
                    }
                }
            }
        }
        // display moving pieces on top of others
        for (Piece movingPiece : movingPieces) {
            renderPiece(g, game, movingPiece, yShift);
        }
        // mask invisible squares
        for (int i = 0; i < game.getWidth(); i++) {
            for (int j = 0; j < game.getHeight(); j++) {
                int row = game.hasOppositeOrientation() ? j : game.getHeight() - 1 - j;
                int column = game.hasOppositeOrientation() ? game.getWidth() - 1 - i : i;
                if (!position.isVisible(row, column)) {
                    int x = boardX + i * squareSize;
                    int y = boardY + j * squareSize;
                    g.setColor(Constants.INVISIBLE_SQUARE);
                    g.fillRect(x, y, squareSize, squareSize);
                }
            }
        }
        // render captured pieces
        for (Piece piece : position.getWhiteCapturedPieces()) {
            renderPiece(g, game, piece, Constants.getCapturedPieceSize(game), yShift);
        }
        for (Piece piece : position.getBlackCapturedPieces()) {
            renderPiece(g, game, piece, Constants.getCapturedPieceSize(game), yShift);
        }
    }

    private void renderPiece(Graphics2D g, Game game, Piece piece, int yShift) {
        renderPiece(g, game, piece, Constants.getSquareSize(game), yShift);
    }

    private void renderPiece(Graphics2D g, Game game, Piece piece, int size, int yShift) {
        if (piece.getType().isVisible()) {
            int x = piece.getX();
            int y = piece.getY() + yShift;
            if (game.hasOppositeOrientation()) {
                x = Constants.VIDEO_WIDTH - x - size;
                y = Constants.VIDEO_HEIGHT - y - size;
            }
            g.drawImage(game.getFromPieceMap(piece.getColor(), piece.getType().getCode()), x, y, size, size, null);
        }
    }

    private BufferedImage getImageFromSvg(InputStream svgFileStream, int squareSize) {
        final BufferedImage[] bufferedImage = new BufferedImage[1];
        try {
            TranscodingHints transcodingHints = getTranscodingHints(squareSize);
            TranscoderInput input = new TranscoderInput(svgFileStream);
            ImageTranscoder t = new ImageTranscoder() {
                @Override
                public BufferedImage createImage(int w, int h) {
                    return new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                }

                @Override
                public void writeImage(BufferedImage image, TranscoderOutput out) {
                    bufferedImage[0] = image;
                }
            };
            t.setTranscodingHints(transcodingHints);
            t.transcode(input, null);
        } catch (Exception ex) {
            LOG.error("Error converting the image from SVG.", ex);
        }
        return bufferedImage[0];
    }

    private TranscodingHints getTranscodingHints(float squareSize) throws URISyntaxException {
        URI cssUri = getClass().getResource("/css/batik-default-override-.css").toURI();
        TranscodingHints transcodingHints = new TranscodingHints();
        transcodingHints.put(ImageTranscoder.KEY_XML_PARSER_VALIDATING, Boolean.FALSE);
        transcodingHints.put(ImageTranscoder.KEY_DOM_IMPLEMENTATION,
                SVGDOMImplementation.getDOMImplementation());
        transcodingHints.put(ImageTranscoder.KEY_DOCUMENT_ELEMENT_NAMESPACE_URI,
                SVGConstants.SVG_NAMESPACE_URI);
        transcodingHints.put(ImageTranscoder.KEY_DOCUMENT_ELEMENT, "svg");
        transcodingHints.put(ImageTranscoder.KEY_USER_STYLESHEET_URI, cssUri.toString());
        // create images for current board size, so it won't be deformed by drawImage scaling
        transcodingHints.put(ImageTranscoder.KEY_WIDTH, squareSize);
        transcodingHints.put(ImageTranscoder.KEY_HEIGHT, squareSize);
        transcodingHints.put(ImageTranscoder.KEY_ALLOW_EXTERNAL_RESOURCES, Boolean.TRUE);
        return transcodingHints;
    }

    public Map<String, BufferedImage> getBufferedImageMap(Map<String, String> imageResourceMap, int squareSize) {
        Map<String, BufferedImage> map = new HashMap<>();
        for (String key : imageResourceMap.keySet()) {
            LOG.info("Loading " + imageResourceMap.get(key));
            map.put(key, getImageFromSvg(getClass().getResourceAsStream(imageResourceMap.get(key)), squareSize));
        }
        return map;
    }

    public Map<String, String> getImageResourceMap(String prefix) {
        Map<String, String> map = new HashMap<>();
        map.put("K", "/images/chess/" + prefix + "King.svg");
        map.put("Q", "/images/chess/" + prefix + "Queen.svg");
        map.put("R", "/images/chess/" + prefix + "Rook.svg");
        map.put("B", "/images/chess/" + prefix + "Bishop.svg");
        map.put("N", "/images/chess/" + prefix + "Knight.svg");
        map.put("p", "/images/chess/" + prefix + "Pawn.svg");
        map.put("A", "/images/chess/" + prefix + "Archbishop.svg");
        map.put("C", "/images/chess/" + prefix + "Chancellor.svg");
        map.put("J", "/images/chess/" + prefix + "Archbishop.svg");
        return map;
    }

    public BufferedImage getScreenshot(Game game, Position position) {
        BufferedImage image = new BufferedImage(Constants.VIDEO_WIDTH, Constants.SCREENSHOT_HEIGHT, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g = image.createGraphics();
        g.setColor(Constants.SCREENSHOT);
        g.fillRect(0, 0, image.getWidth(), image.getHeight());
        renderChessBoard(g, game, position, true);
        return image;
    }

    public BufferedImage getRenderedImage(Game game, Position position, List<Move> processedMoves) {
        BufferedImage image = new BufferedImage(Constants.VIDEO_WIDTH, Constants.VIDEO_HEIGHT, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g = image.createGraphics();
        g.setColor(Constants.BACKGROUND);
        g.fillRect(0, 0, image.getWidth(), image.getHeight());
        renderMetadata(g, game);
        renderChessBoard(g, game, position, false);
        renderCurrentMoveNotation(g, position);
        renderNotationLine(g, processedMoves);
        renderGameStatus(g, game, position);
        return image;
    }

    private void renderMetadata(Graphics2D g, Game game) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Font font = Fonts.METADATA_FONT;
        g.setFont(font);
        g.setColor(Color.BLACK);
        FontMetrics fontMetrics = g.getFontMetrics(font);
        String title = game.getSite() + " - " + game.getDate();
        g.drawString(title, (Constants.VIDEO_WIDTH - fontMetrics.stringWidth(title)) / 2, fontMetrics.getHeight() + 20);
        String description = (StringUtils.isNotBlank(game.getVariant()) ? "Variant: " + game.getVariant() + ", " : "") +
                "White: " + game.getWhite() + ", Black: " + game.getBlack() + ", Result: " + game.getResult();
        g.drawString(description, (Constants.VIDEO_WIDTH - fontMetrics.stringWidth(description)) / 2, fontMetrics.getHeight() * 2 + 30);
    }

    private void renderCurrentMoveNotation(Graphics2D g, Position position) {
        Notation notation = position.getCurrentMoveNotationDto();
        if (StringUtils.isNotBlank(notation.pgnCode())) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Font font = Fonts.RESULT_FONT;
            g.setFont(font);
            g.setColor(Color.BLACK);
            FontMetrics fontMetrics = g.getFontMetrics(font);
            String fullNotation = notation.pgnCode() + (notation.result() == null ? "" : notation.result());
            int x = (Constants.VIDEO_WIDTH - fontMetrics.stringWidth(fullNotation)) / 2;
            int y = Constants.VIDEO_HEIGHT - fontMetrics.getHeight() - 30;
            g.drawString(notation.prefix(), x, y);
            x += fontMetrics.stringWidth(notation.prefix());
            if (StringUtils.isNotBlank(notation.symbol())) {
                Font symbolFont = Fonts.SYMBOL_FONT;
                g.setFont(symbolFont);
                FontMetrics symbolFontMetrics = g.getFontMetrics(symbolFont);
                g.drawString(notation.symbol(), x, y);
                x += symbolFontMetrics.stringWidth(notation.symbol());
            }
            g.setFont(font);
            g.drawString(notation.suffix(), x, y);
            x += fontMetrics.stringWidth(notation.suffix());
            if (StringUtils.isNotBlank(notation.result())) {
                g.drawString(notation.result(), x, y);
            }
        }
    }

    private void renderNotationLine(Graphics2D g, List<Move> processedMoves) {
        if (processedMoves != null) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Font font = Fonts.LINE_FONT;
            g.setFont(font);
            g.setColor(Color.BLACK);
            FontMetrics fontMetrics = g.getFontMetrics(font);
            int x = 15;
            int y = Constants.VIDEO_HEIGHT - fontMetrics.getHeight() - fontMetrics.getHeight() - 10;
            for (Move move : processedMoves) {
                String code = move.getPgnCode();
                if (StringUtils.isNotBlank(code)) {
                    if (move.isWhite()) {
                        code = move.getMoveNumber() + "." + code;
                    }
                    g.drawString(code, x, y);
                    x += fontMetrics.stringWidth(code) + 5;
                    if (!move.isWhite() && (move.getMoveNumber() == 15 || move.getMoveNumber() == 29)) {
                        x = 15;
                        y += fontMetrics.getHeight();
                    }
                }
            }
        }
    }

    private void renderGameStatus(Graphics2D g, Game game, Position position) {
        String gameStatus = position.getGameStatus();
        if (StringUtils.isNotBlank(gameStatus)) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Font font = Fonts.RESULT_FONT;
            g.setFont(font);
            g.setColor(Color.BLACK);
            FontMetrics fontMetrics = g.getFontMetrics(font);
            int x = Constants.getBoardX(game);
            int y = Constants.VIDEO_HEIGHT - fontMetrics.getHeight() - 20;
            g.drawString(gameStatus, x, y);
        }
    }

    private void fillCrossHatchedSquare(Graphics2D g, int x, int y, int size) {
        int interval = 5;
        Rectangle2D square = new Rectangle2D.Double(x, y, size, size);
        BufferedImage bufferedImage = new BufferedImage(interval, interval, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D graphics2D = bufferedImage.createGraphics();
        graphics2D.setColor(Constants.SCREENSHOT);
        graphics2D.fillRect(0, 0, interval, interval);
        graphics2D.setColor(Constants.CROSS_HATCHED_SQUARE);
        graphics2D.drawLine(0, interval, interval, 0);
        Rectangle2D rectangle2D = new Rectangle2D.Double(0, 0, interval, interval);
        g.setPaint(new TexturePaint(bufferedImage, rectangle2D));
        g.fill(square);
    }

}
