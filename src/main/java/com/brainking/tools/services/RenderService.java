package com.brainking.tools.services;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.TexturePaint;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
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

    private final SvgService svgService;

    @Autowired
    public RenderService(final SvgService svgService) {
        this.svgService = svgService;
    }

    public void renderChessBoard(
        final Graphics2D graphics2d,
        final Game game,
        final Position position,
        final boolean screenshot) {
        final int boardX = Constants.getBoardX(game);
        final int boardY = screenshot ? Constants.getScreenshotBoardY(game) : Constants.getBoardY(game);
        final int yShift = game.hasOppositeOrientation() ? Constants.getBoardY(game) - boardY : boardY - Constants.getBoardY(game);
        final int squareSize = Constants.getSquareSize(game);
        // render empty board
        renderEmptyBoard(graphics2d, game, position, boardX, boardY, squareSize, screenshot);
        // render a border with column letters and row numbers
        renderBorder(graphics2d, game, boardX, boardY, squareSize);
        // render pieces
        final Piece[][] pieceGrid = position.getPieceGrid();
        final List<Piece> movingPieces = new ArrayList<>();  // there could be more than one, e.g. castling
        for (int row = 0; row < game.getHeight(); row++) {
            for (int column = 0; column < game.getWidth(); column++) {
                final Piece piece = pieceGrid[row][column];
                if (piece != null) {
                    if (piece.isMoving()) {
                        movingPieces.add(piece);
                    } else {
                        renderPiece(graphics2d, game, piece, yShift);
                    }
                }
            }
        }
        // display moving pieces on top of others
        for (final Piece movingPiece : movingPieces) {
            renderPiece(graphics2d, game, movingPiece, yShift);
        }
        // mask invisible squares
        for (int i = 0; i < game.getWidth(); i++) {
            for (int j = 0; j < game.getHeight(); j++) {
                final int row = game.hasOppositeOrientation() ? j : game.getHeight() - 1 - j;
                final int column = game.hasOppositeOrientation() ? game.getWidth() - 1 - i : i;
                if (!position.isVisible(row, column)) {
                    final int xPos = boardX + i * squareSize;
                    final int yPos = boardY + j * squareSize;
                    graphics2d.setColor(Constants.INVISIBLE_SQUARE);
                    graphics2d.fillRect(xPos, yPos, squareSize, squareSize);
                }
            }
        }
        // render captured pieces
        for (final Piece piece : position.getWhiteCapturedPieces()) {
            renderPiece(graphics2d, game, piece, Constants.getCapturedPieceSize(game), yShift);
        }
        for (final Piece piece : position.getBlackCapturedPieces()) {
            renderPiece(graphics2d, game, piece, Constants.getCapturedPieceSize(game), yShift);
        }
    }

    public BufferedImage getScreenshot(final Game game, final Position position) {
        final BufferedImage image = new BufferedImage(Constants.VIDEO_WIDTH, Constants.SCREENSHOT_HEIGHT, BufferedImage.TYPE_3BYTE_BGR);
        final Graphics2D graphics2d = image.createGraphics();
        graphics2d.setColor(Constants.SCREENSHOT);
        graphics2d.fillRect(0, 0, image.getWidth(), image.getHeight());
        renderChessBoard(graphics2d, game, position, true);
        return image;
    }

    public Map<String, BufferedImage> getBufferedImageMap(final String prefix, final int squareSize) {
        return svgService.getBufferedImageMap(svgService.getImageResourceMap(prefix), squareSize);
    }

    private void renderEmptyBoard(final Graphics2D graphics2d, final Game game, final Position position, final int boardX, final int boardY, final int squareSize, final boolean screenshot) {
        for (int i = 0; i < game.getWidth(); i++) {
            for (int j = 0; j < game.getHeight(); j++) {
                final int row = game.hasOppositeOrientation() ? j : game.getHeight() - 1 - j;
                final int column = game.hasOppositeOrientation() ? game.getWidth() - 1 - i : i;
                if (!position.matchesHole(row, column)) {
                    final int xPos = boardX + i * squareSize;
                    final int yPos = boardY + j * squareSize;
                    final boolean lightSquare = (i + j) % 2 == 0;
                    renderSquare(graphics2d, xPos, yPos, squareSize, screenshot, lightSquare);
                    renderHighlights(graphics2d, position, row, column, xPos, yPos, squareSize, screenshot);
                }
            }
        }
    }

    private void renderSquare(final Graphics2D graphics2d, final int xPos, final int yPos, final int squareSize, final boolean screenshot, final boolean lightSquare) {
        if (screenshot) {
            if (!lightSquare) {
                fillCrossHatchedSquare(graphics2d, xPos, yPos, squareSize);
            }
        } else {
            graphics2d.setColor(lightSquare ? Constants.LIGHT_SQUARE : Constants.DARK_SQUARE);
            graphics2d.fillRect(xPos, yPos, squareSize, squareSize);
        }
    }

    private void renderHighlights(final Graphics2D graphics2d, final Position position, final int row, final int column, final int xPos, final int yPos, final int squareSize, final boolean screenshot) {
        // highlight current move
        if (position.matchesCurrentMove(row, column)) {
            graphics2d.setColor(Constants.HIGHLIGHT_MOVE);
            graphics2d.fillRect(xPos, yPos, squareSize, squareSize);
        }
        // highlight target (Ambiguous Chess)
        if (position.matchesTargetSquare(row, column)) {
            graphics2d.setColor(Constants.HIGHLIGHT_TARGET);
            graphics2d.fillRect(xPos, yPos, squareSize, squareSize);
        }
        // highlight king in check
        if (!screenshot && position.matchesPieceInCheck(row, column)) {
            graphics2d.setColor(Constants.HIGHLIGHT_CHECK);
            graphics2d.fillRect(xPos, yPos, squareSize, squareSize);
        }
    }

    private void renderBorder(final Graphics2D graphics2d, final Game game, final int boardX, final int boardY, final int squareSize) {
        graphics2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        final Font font = Fonts.ROW_COLUMN_FONT;
        graphics2d.setFont(font);
        final FontMetrics fontMetrics = graphics2d.getFontMetrics(font);
        final int ascent = fontMetrics.getAscent();
        final int space = 10;
        graphics2d.setColor(Color.BLACK);
        for (int row = 0; row < game.getHeight(); row++) {
            final int index = game.hasOppositeOrientation() ? game.getHeight() - 1 - row : row;
            final int yPos = boardY + (game.getHeight() - row) * squareSize - (squareSize - ascent) / 2;
            graphics2d.drawString(Constants.ROW_ARRAY[index], boardX - 30, yPos);
            graphics2d.drawString(Constants.ROW_ARRAY[index], boardX + game.getWidth() * squareSize + space, yPos);
        }
        for (int column = 0; column < game.getWidth(); column++) {
            final int index = game.hasOppositeOrientation() ? game.getWidth() - 1 - column : column;
            final String string = Constants.COLUMN_ARRAY[index].toUpperCase(Locale.ENGLISH);
            final int xPos = boardX + column * squareSize + (squareSize - fontMetrics.stringWidth(string)) / 2;
            graphics2d.drawString(string, xPos, boardY - space);
            graphics2d.drawString(string, xPos, boardY + game.getHeight() * squareSize + ascent);
        }
    }

    private void renderPiece(final Graphics2D graphics2d, final Game game, final Piece piece, final int yShift) {
        renderPiece(graphics2d, game, piece, Constants.getSquareSize(game), yShift);
    }

    private void renderPiece(final Graphics2D graphics2d, final Game game, final Piece piece, final int size, final int yShift) {
        if (piece.getType().isVisible()) {
            int xPos = piece.getX();
            int yPos = piece.getY() + yShift;
            if (game.hasOppositeOrientation()) {
                xPos = Constants.VIDEO_WIDTH - xPos - size;
                yPos = Constants.VIDEO_HEIGHT - yPos - size;
            }
            graphics2d.drawImage(game.getFromPieceMap(piece.getColor(), piece.getType().getCode()), xPos, yPos, size, size, null);
        }
    }

    public BufferedImage getRenderedImage(final Game game, final Position position, final List<Move> processedMoves) {
        final BufferedImage image = new BufferedImage(Constants.VIDEO_WIDTH, Constants.VIDEO_HEIGHT, BufferedImage.TYPE_3BYTE_BGR);
        final Graphics2D graphics2d = image.createGraphics();
        graphics2d.setColor(Constants.BACKGROUND);
        graphics2d.fillRect(0, 0, image.getWidth(), image.getHeight());
        renderMetadata(graphics2d, game);
        renderChessBoard(graphics2d, game, position, false);
        renderCurrentMoveNotation(graphics2d, position);
        renderNotationLine(graphics2d, processedMoves);
        renderGameStatus(graphics2d, game, position);
        return image;
    }

    private void renderMetadata(final Graphics2D graphics2d, final Game game) {
        graphics2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        final Font font = Fonts.METADATA_FONT;
        graphics2d.setFont(font);
        graphics2d.setColor(Color.BLACK);
        final FontMetrics fontMetrics = graphics2d.getFontMetrics(font);
        final String title = game.getSite() + " - " + game.getDate();
        graphics2d.drawString(title, (Constants.VIDEO_WIDTH - fontMetrics.stringWidth(title)) / 2, fontMetrics.getHeight() + 20);
        final String description = (StringUtils.isNotBlank(game.getVariant()) ? "Variant: " + game.getVariant() + ", " : "") +
                "White: " + game.getWhite() + ", Black: " + game.getBlack() + ", Result: " + game.getResult();
        graphics2d.drawString(description, (Constants.VIDEO_WIDTH - fontMetrics.stringWidth(description)) / 2, fontMetrics.getHeight() * 2 + 30);
    }

    private void renderCurrentMoveNotation(final Graphics2D graphics2d, final Position position) {
        final Notation notation = position.getCurrentMoveNotationDto();
        if (StringUtils.isNotBlank(notation.pgnCode())) {
            graphics2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            final Font font = Fonts.RESULT_FONT;
            graphics2d.setFont(font);
            graphics2d.setColor(Color.BLACK);
            final FontMetrics fontMetrics = graphics2d.getFontMetrics(font);
            final String fullNotation = notation.pgnCode() + (notation.result() == null ? "" : notation.result());
            int xPos = (Constants.VIDEO_WIDTH - fontMetrics.stringWidth(fullNotation)) / 2;
            final int yPos = Constants.VIDEO_HEIGHT - fontMetrics.getHeight() - 30;
            graphics2d.drawString(notation.prefix(), xPos, yPos);
            xPos += fontMetrics.stringWidth(notation.prefix());
            if (StringUtils.isNotBlank(notation.symbol())) {
                final Font symbolFont = Fonts.SYMBOL_FONT;
                graphics2d.setFont(symbolFont);
                final FontMetrics symbolFontMetrics = graphics2d.getFontMetrics(symbolFont);
                graphics2d.drawString(notation.symbol(), xPos, yPos);
                xPos += symbolFontMetrics.stringWidth(notation.symbol());
            }
            graphics2d.setFont(font);
            graphics2d.drawString(notation.suffix(), xPos, yPos);
            xPos += fontMetrics.stringWidth(notation.suffix());
            if (StringUtils.isNotBlank(notation.result())) {
                graphics2d.drawString(notation.result(), xPos, yPos);
            }
        }
    }

    private void renderNotationLine(final Graphics2D graphics2d, final List<Move> processedMoves) {
        if (processedMoves != null) {
            graphics2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            final Font font = Fonts.LINE_FONT;
            graphics2d.setFont(font);
            graphics2d.setColor(Color.BLACK);
            final FontMetrics fontMetrics = graphics2d.getFontMetrics(font);
            int xPos = 15;
            int yPos = Constants.VIDEO_HEIGHT - fontMetrics.getHeight() - fontMetrics.getHeight() - 10;
            for (final Move move : processedMoves) {
                String code = move.getPgnCode();
                if (StringUtils.isNotBlank(code)) {
                    if (move.isWhite()) {
                        code = move.getMoveNumber() + "." + code;
                    }
                    graphics2d.drawString(code, xPos, yPos);
                    xPos += fontMetrics.stringWidth(code) + 5;
                    if (!move.isWhite() && (move.getMoveNumber() == 15 || move.getMoveNumber() == 29)) {
                        xPos = 15;
                        yPos += fontMetrics.getHeight();
                    }
                }
            }
        }
    }

    private void renderGameStatus(final Graphics2D graphics2d, final Game game, final Position position) {
        final String gameStatus = position.getGameStatus();
        if (StringUtils.isNotBlank(gameStatus)) {
            graphics2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            final Font font = Fonts.RESULT_FONT;
            graphics2d.setFont(font);
            graphics2d.setColor(Color.BLACK);
            final FontMetrics fontMetrics = graphics2d.getFontMetrics(font);
            final int xPos = Constants.getBoardX(game);
            final int yPos = Constants.VIDEO_HEIGHT - fontMetrics.getHeight() - 20;
            graphics2d.drawString(gameStatus, xPos, yPos);
        }
    }

    private void fillCrossHatchedSquare(final Graphics2D graphics2d, final int xPos, final int yPos, final int size) {
        final int interval = 5;
        final Rectangle2D square = new Rectangle2D.Double(xPos, yPos, size, size);
        final BufferedImage bufferedImage = new BufferedImage(interval, interval, BufferedImage.TYPE_3BYTE_BGR);
        final Graphics2D graphics2D = bufferedImage.createGraphics();
        graphics2D.setColor(Constants.SCREENSHOT);
        graphics2D.fillRect(0, 0, interval, interval);
        graphics2D.setColor(Constants.CROSS_HATCHED_SQUARE);
        graphics2D.drawLine(0, interval, interval, 0);
        final Rectangle2D rectangle2D = new Rectangle2D.Double(0, 0, interval, interval);
        graphics2d.setPaint(new TexturePaint(bufferedImage, rectangle2D));
        graphics2d.fill(square);
    }

}
