# ChessVideo

This is my private project. It is an MP4 video generator that plays back a chess game based on an input PGN (Portable Game Notation) file.

**Note:** The finished video still contains some hard-coded text referencing the BrainKing.com game server. Hopefully, I’ll finally remove it over time.

The application requires Java 25 or higher and runs on Spring Boot 4. To successfully generate a video, at least one valid PGN file must be placed in the `data/pgn` subfolder.

The Gradle `build` task uses PMD and SpotBugs for static code analysis and, of course, runs all tests.
