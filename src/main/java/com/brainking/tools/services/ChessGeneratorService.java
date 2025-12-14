package com.brainking.tools.services;

import com.brainking.tools.dto.Game;
import com.brainking.tools.dto.Move;
import com.brainking.tools.dto.Position;
import com.brainking.tools.utils.Constants;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.lang3.StringUtils;
import org.jcodec.api.awt.AWTSequenceEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class ChessGeneratorService {

    private static final Logger LOG = LoggerFactory.getLogger(ChessGeneratorService.class);

    private final RenderService renderService;
    private final PositionService positionService;
    private final ImportService importService;
    private final YouTubeService youTubeService;
    private final EncoderService encoderService;

    private final String sourceFolder;
    private final String targetFolder;
    private final boolean youTubeExportActive;
    private final boolean generateVideo;

    @Autowired
    public ChessGeneratorService(final RenderService renderService,
                                final PositionService positionService,
                                final ImportService importService,
                                final YouTubeService youTubeService,
                                final EncoderService encoderService,
                                @Value("${source.folder}") final String sourceFolder,
                                @Value("${target.folder}") final String targetFolder,
                                @Value("${youtube.export.active:false}") final boolean youTubeExportActive,
                                @Value("${generate.video:true}") final boolean generateVideo) {
        this.renderService = renderService;
        this.positionService = positionService;
        this.importService = importService;
        this.youTubeService = youTubeService;
        this.encoderService = encoderService;
        this.sourceFolder = sourceFolder;
        this.targetFolder = targetFolder;
        this.youTubeExportActive = youTubeExportActive;
        this.generateVideo = generateVideo;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void renderChessVideo() {
        final String[] extensions = {"pgn"};
        final String processedFolder = sourceFolder + "/processed";
        final File input = new File(sourceFolder);
        final File archive = new File(processedFolder);
        final Collection<File> files = FileUtils.listFiles(input, extensions, false);
        if (CollectionUtils.isEmpty(files)) {
            LOG.info("No PGN files found.");
            return;
        }
        createFolders(input, archive);
        // TODO: setup YouTube OAuth 2 flow at the start
        for (final File pgnFile : files) {
            final Collection<File> existingFiles = FileUtils.listFiles(archive, FileFilterUtils.nameFileFilter(pgnFile.getName()), null);
            if (!existingFiles.isEmpty()) {
                LOG.info("File " + pgnFile.getName() + " already processed, deleting.");
                try {
                    FileUtils.forceDelete(pgnFile);
                } catch (IOException ex) {
                    LOG.error("Error deleting the file.", ex);
                }
                continue;
            }
            final long currentTime = System.currentTimeMillis();
            try {
                final Game game = importService.importPgn(pgnFile);
                final List<Move> moves = new ArrayList<>();
                final int squareSize = Constants.getSquareSize(game);
                final Map<String, BufferedImage> whitePieceMap = renderService.getBufferedImageMap("White", squareSize);
                final Map<String, BufferedImage> blackPieceMap = renderService.getBufferedImageMap("Black", squareSize);
                game.addPieceMaps(whitePieceMap, blackPieceMap);
                moves.add(new Move());  // blank first move to show the start position
                moves.addAll(game.getMoves());
                final Position position = positionService.generateStartPosition(game);
                final String videoFolder = targetFolder + (StringUtils.isNotBlank(game.getVariant()) ? "/" + game.getVariant() : "");
                LOG.info("Rendering the video to " + videoFolder);
                FileUtils.forceMkdir(new File(videoFolder));
                final String videoName = game.getName();
                if (generateVideo) {
                    writeMetadata(game, videoFolder, videoName);
                }
                AWTSequenceEncoder encoder = null;
                if (generateVideo) {
                    encoder = AWTSequenceEncoder.create25Fps(new File(videoFolder, videoName + ".mov"));
                }
                final List<Move> processedMoves = getProcessedMoves(encoder, game, position, moves, videoFolder, videoName);
                // display result and keep it for 10 seconds
                if (encoder != null) {
                    LOG.info("Rendering the final screen");
                    final BufferedImage image = renderService.getRenderedImage(game, position, processedMoves);
                    for (int i = 0; i < Constants.FRAMES_AFTER_LAST_MOVE; i++) {
                        encoder.encodeImage(image);
                    }
                    encoder.finish();
                    LOG.info("Converting to MP4");
                    final String pathToVideo = encoderService.convertToMP4(videoFolder, videoName);
                    LOG.info("Video " + pathToVideo + " completed in " +
                            new SimpleDateFormat("mm:ss", Locale.ENGLISH). format(System.currentTimeMillis() - currentTime) +
                            " minutes");
                    LOG.info("Archiving file " + pgnFile + " to " + processedFolder);
                    FileUtils.moveFileToDirectory(pgnFile, archive, true);
                    if (youTubeExportActive) {
                        // TODO: https://explorer.lichess.ovh/master?fen=<fenCode>
                        LOG.info("Uploading video " + pathToVideo + " to YouTube");
                        final String youTubeId = youTubeService.uploadVideo(game, pathToVideo);
                        LOG.info("Video " + pathToVideo + " uploaded with ID = " + youTubeId);
                        LOG.info("SQL command (bk20 database): insert into game_external (game_id, you_tube_id) values (" +
                                game.getName() + ", '" + youTubeId + "');");
                    }
                }
            } catch (IOException ex) {
                LOG.error("Error rendering the video.", ex);
            }
        }
    }

    private void createFolders(final File input, final File archive) {
        try {
            FileUtils.forceMkdir(input);
            FileUtils.forceMkdir(archive);
        } catch (IOException ex) {
            LOG.error("Error creating folders.", ex);
        }
    }

    private void writeMetadata(final Game game, final String videoFolder, final String videoName) throws IOException {
        final String content = """
                {result}
                Visit my chess blog: https://LookIntoChess.com

                Played on BrainKing.com ({white} vs. {black}), {date}

                {pgn}
                """
                .replace("{result}", game.getResult())
                .replace("{white}", game.getWhite())
                .replace("{black}", game.getBlack())
                .replace("{date}", game.getFormattedDate().orElse(""))
                .replace("{pgn}", game.getPgnCode());
        Files.writeString(Path.of(videoFolder, videoName + ".txt"), content);
    }

    private List<Move> getProcessedMoves(
        final AWTSequenceEncoder encoder,
        final Game game,
        final Position position,
        final List<Move> moves,
        final String videoFolder,
        final String videoName) throws IOException {
        final List<Move> processedMoves = new ArrayList<>();
        for (final Move move : moves) {
            processedMoves.add(move);
            // mark target square for Ambiguous Chess
            if (encoder != null && game.isVariant(Constants.AMBIGUOUS)) {
                position.markTargetSquare(move);
                for (int i = 0; i < Constants.FRAMES_TO_SHOW_TARGET; i++) {
                    final BufferedImage image = renderService.getRenderedImage(game, position, processedMoves);
                    encoder.encodeImage(image);
                }
            }
            position.startMoving(move);
            while (position.isMoving()) {
                position.doMoveStep();
                if (encoder != null) {
                    final BufferedImage image = renderService.getRenderedImage(game, position, processedMoves);
                    encoder.encodeImage(image);
                }
            }
            position.stopMoving();
            // save screenshot
            if (move.getScreenshotId() != null) {
                final BufferedImage image = renderService.getScreenshot(game, position);
                final String screenshotName = videoName + "-" + move.getScreenshotId() + ".png";
                ImageIO.write(image, "png", new File(videoFolder, screenshotName));
                LOG.info("Screenshot " + screenshotName + " saved to " + videoFolder);
            }
            // add ten times the last frame to set a delay between moves
            if (encoder != null) {
                final BufferedImage image = renderService.getRenderedImage(game, position, processedMoves);
                for (int i = 0; i < Constants.FRAMES_BETWEEN_MOVES; i++) {
                    encoder.encodeImage(image);
                }
            }
        }
        position.finishGame();
        return processedMoves;
    }

}
