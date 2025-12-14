package com.brainking.tools.services;

import com.brainking.tools.dto.Color;
import com.brainking.tools.dto.Game;
import com.brainking.tools.dto.Move;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ImportService {

    private static final Logger log = LoggerFactory.getLogger(ImportService.class);

    private static final Pattern metadataPattern = Pattern.compile("^\\[([^\\s]+)\\s+\"(.+)\"]$");
    private static final Pattern moveListPattern = Pattern.compile("\\d+.\\s+[^\\s]+\\s+[^\\s]+");
    private static final Pattern movePattern = Pattern.compile("(\\d+).\\s+([^\\s]+)\\s+([^\\s]+)");

    public Game importPgn(File file) throws IOException {
        log.info("Loading PGN file: " + file.getAbsolutePath());
        Game game = new Game(FilenameUtils.getBaseName(file.getName()));
        List<String> lines = FileUtils.readLines(file, "UTF-8");
        game.setPgnCode(String.join("\n", lines));
        StringBuilder moveBuilder = new StringBuilder();
        boolean moveLinesFound = false;
        for (String line : lines) {
            Matcher matcher = metadataPattern.matcher(line);
            // find metadata
            if (matcher.matches()) {
                game.addMetadata(matcher.group(1), matcher.group(2));
            } else if (line.startsWith("1.")) {
                moveLinesFound = true;
            }
            if (moveLinesFound) {
                moveBuilder.append(line).append(" ");
            }
        }
        game.setOrientationFromResult();
        game.setDimensionFromVariant();
        // make a single string from all move lines and parse them
        String allMoves = StringUtils.trim(moveBuilder.toString());
        Matcher matcher = moveListPattern.matcher(allMoves);
        while (matcher.find()) {
            Matcher m = movePattern.matcher(matcher.group());
            if (m.matches()) {
                int moveNumber = Integer.parseInt(m.group(1));
                game.addMove(new Move(game, moveNumber, m.group(2), Color.WHITE));
                String blackMove = m.group(3);
                // skip game result (do not skip castling or ambiguous moves)
                if (!blackMove.contains("-") || blackMove.contains("O") || blackMove.startsWith("?")) {
                    game.addMove(new Move(game, moveNumber, blackMove, Color.BLACK));
                }
            }
        }
        return game;
    }

}
