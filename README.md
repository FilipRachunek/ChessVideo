# ChessVideo

This is my private project. It is an MP4 video generator that plays back a chess game based on an input PGN (Portable Game Notation) file.

**Note:** The finished video still contains some hard-coded text referencing the BrainKing.com game server. Hopefully, I’ll finally remove it over time.

The application requires **Java 25** or higher and runs on **Spring Boot 4**. To successfully generate a video, at least one valid PGN file must be placed in the `data/pgn` subfolder.

A sample game `8672028.pgn` should be there. Watch the generated video: https://www.youtube.com/watch?v=rcx2SzGTmWw

The Gradle `build` task uses PMD and SpotBugs for static code analysis and, of course, runs all tests.

## Why Java 25?

I try to keep up with the development of the JDK and enjoy testing new features that I find useful. In this project, I used for example:

* Factory methods for collections (Java 9)
* Unmodifiable collections (Java 10)
* Local variable type inference (Java 10)*
* Files methods (Java 11)
* Switch expressions (Java 14)
* Text blocks (Java 15)
* Records (Java 16)
* Simplified main methods (Java 25)

*In the end, it was removed because PMD had some issue with it, and I didn’t want to add another exception to the rules.
