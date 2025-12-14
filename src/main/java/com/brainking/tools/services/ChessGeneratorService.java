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
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
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
        String[] extensions = {"pgn"};
        String processedFolder = sourceFolder + "/processed";
        File input = new File(sourceFolder);
        File archive = new File(processedFolder);
        Collection<File> files = FileUtils.listFiles(input, extensions, false);
        if (CollectionUtils.isEmpty(files)) {
            LOG.info("No PGN files found.");
            return;
        }
        createFolders(input, archive);
        // TODO: setup YouTube OAuth 2 flow at the start
        for (File pgnFile : files) {
            Collection<File> existingFiles = FileUtils.listFiles(archive, FileFilterUtils.nameFileFilter(pgnFile.getName()), null);
            if (!existingFiles.isEmpty()) {
                LOG.info("File " + pgnFile.getName() + " already processed, deleting.");
                try {
                    FileUtils.forceDelete(pgnFile);
                } catch (Exception ex) {
                    LOG.error("Error deleting the file.", ex);
                }
                continue;
            }
            long currentTime = System.currentTimeMillis();
            try {
                List<Move> moves = new ArrayList<>();
                moves.add(new Move());  // blank first move to show the start position
                Game game = importService.importPgn(pgnFile);
                int squareSize = Constants.getSquareSize(game);
                Map<String, BufferedImage> whitePieceMap = renderService.getBufferedImageMap(renderService.getImageResourceMap("White"), squareSize);
                Map<String, BufferedImage> blackPieceMap = renderService.getBufferedImageMap(renderService.getImageResourceMap("Black"), squareSize);
                game.addPieceMaps(whitePieceMap, blackPieceMap);
                moves.addAll(game.getMoves());
                Position position = positionService.generateStartPosition(game);
                String videoFolder = targetFolder + (StringUtils.isNotBlank(game.getVariant()) ? "/" + game.getVariant() : "");
                LOG.info("Rendering the video to " + videoFolder);
                FileUtils.forceMkdir(new File(videoFolder));
                String videoName = game.getName();
                if (generateVideo) {
                    FileWriter metadataWriter = new FileWriter(videoFolder + "/" + videoName + ".txt", false);
                    metadataWriter.write(", " + game.getResult() + "\n");
                    metadataWriter.write("Visit my chess blog: https://LookIntoChess.com\n");
                    metadataWriter.write("\n");
                    metadataWriter.write("Played on BrainKing.com (" + game.getWhite() + " vs. " + game.getBlack() + "), " + game.getFormattedDate().orElse("") + "\n");
                    metadataWriter.write("\n");
                    metadataWriter.write(game.getPgnCode() + "\n");
                    metadataWriter.close();
                }
                AWTSequenceEncoder encoder = null;
                if (generateVideo) {
                    encoder = AWTSequenceEncoder.create25Fps(new File(videoFolder, videoName + ".mov"));
                }
                List<Move> processedMoves = new ArrayList<>();
                for (Move move : moves) {
                    processedMoves.add(move);
                    // mark target square for Ambiguous Chess
                    if (encoder != null) {
                        if (game.isVariant(Constants.AMBIGUOUS)) {
                            position.markTargetSquare(move);
                            for (int i = 0; i < Constants.FRAMES_TO_SHOW_TARGET; i++) {
                                BufferedImage image = renderService.getRenderedImage(game, position, processedMoves);
                                encoder.encodeImage(image);
                            }
                        }
                    }
                    position.startMoving(move);
                    while (position.isMoving()) {
                        position.doMoveStep();
                        if (encoder != null) {
                            BufferedImage image = renderService.getRenderedImage(game, position, processedMoves);
                            encoder.encodeImage(image);
                        }
                    }
                    position.stopMoving();
                    // save screenshot
                    if (move.getScreenshotId() != null) {
                        BufferedImage image = renderService.getScreenshot(game, position);
                        String screenshotName = videoName + "-" + move.getScreenshotId() + ".png";
                        ImageIO.write(image, "png", new File(videoFolder, screenshotName));
                        LOG.info("Screenshot " + screenshotName + " saved to " + videoFolder);
                    }
                    // add ten times the last frame to set a delay between moves
                    if (encoder != null) {
                        BufferedImage image = renderService.getRenderedImage(game, position, processedMoves);
                        for (int i = 0; i < Constants.FRAMES_BETWEEN_MOVES; i++) {
                            encoder.encodeImage(image);
                        }
                    }
                }
                position.finishGame();
                // display result and keep it for 10 seconds
                if (encoder != null) {
                    LOG.info("Rendering the final screen");
                    BufferedImage image = renderService.getRenderedImage(game, position, processedMoves);
                    for (int i = 0; i < Constants.FRAMES_AFTER_LAST_MOVE; i++) {
                        encoder.encodeImage(image);
                    }
                    encoder.finish();
                    LOG.info("Converting to MP4");
                    String pathToVideo = encoderService.convertToMP4(videoFolder, videoName);
                    LOG.info("Video " + pathToVideo + " completed in " +
                            (new SimpleDateFormat("mm:ss")).format(new Date(System.currentTimeMillis() - currentTime)) +
                            " minutes");
                    LOG.info("Archiving file " + pgnFile + " to " + processedFolder);
                    FileUtils.moveFileToDirectory(pgnFile, archive, true);
                    if (youTubeExportActive) {
                        // TODO: https://explorer.lichess.ovh/master?fen=<fenCode>
                        LOG.info("Uploading video " + pathToVideo + " to YouTube");
                        String youTubeId = youTubeService.uploadVideo(game, pathToVideo);
                        LOG.info("Video " + pathToVideo + " uploaded with ID = " + youTubeId);
                        LOG.info("SQL command (bk20 database): insert into game_external (game_id, you_tube_id) values (" +
                                game.getName() + ", '" + youTubeId + "');");
                    }
                }
            } catch (Exception ex) {
                LOG.error("Error rendering the video.", ex);
            }
        }
    }

    private void createFolders(File input, File archive) {
        try {
            FileUtils.forceMkdir(input);
            FileUtils.forceMkdir(archive);
        } catch (IOException ex) {
            LOG.error("Error creating folders.", ex);
        }
    }

}
