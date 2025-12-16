package com.brainking.tools.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockStatic;

import java.io.File;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class FileServiceTest {

    private MockedStatic<FileUtils> fileUtils;
    private FileService fileService;

    @BeforeEach
    void setUp() {
        fileUtils = mockStatic(FileUtils.class);
        fileService = new FileService();
    }

    @AfterEach
    void tearDown() {
        fileUtils.close();
    }

    @Test
    void shouldGetSourceFiles() {
        final File input = new File("source");
        final String[] extensions = {"pgn"};
        final Collection<File> files = List.of(new File("file"));
        fileUtils.when(() -> FileUtils.listFiles(input, extensions, false)).thenReturn(files);
        final Collection<File> result = fileService.getSourceFiles(input, extensions);
        assertEquals(files, result, "Returns expected files.");
    }

    @Test
    void shouldCreateSourceFolders() {
        final File input = new File("source");
        final File archive = new File("source/processed");
        fileService.createSourceFolders(input, archive);
        fileUtils.verify(() -> FileUtils.forceMkdir(archive));
    }

    @Test
    void shouldDeleteFile() {
        final File file = new File("file");
        fileService.deleteFile(file);
        fileUtils.verify(() -> FileUtils.forceDelete(file));
    }

    @Test
    void shouldMoveFileToFolder() {
        final File file = new File("file");
        final File folder = new File("folder");
        fileService.moveFileToFolder(file, folder);
        fileUtils.verify(() -> FileUtils.moveFileToDirectory(file, folder, true));
    }

}
