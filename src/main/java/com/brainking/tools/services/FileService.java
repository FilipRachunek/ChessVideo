package com.brainking.tools.services;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class FileService {

    private static final Logger LOG = LoggerFactory.getLogger(FileService.class);

    @SuppressWarnings("PMD.UseVarargs")
    public Collection<File> getSourceFiles(final File input, final String[] extensions) {
        return FileUtils.listFiles(input, extensions, false);
    }

    public void createSourceFolders(final File input, final File archive) {
        try {
            FileUtils.forceMkdir(input);
            FileUtils.forceMkdir(archive);
        } catch (IOException ex) {
            LOG.error("Error creating folders.", ex);
        }
    }

    public Collection<File> getExistingSourceFiles(final File archive, final File pgnFile) {
        return FileUtils.listFiles(archive, FileFilterUtils.nameFileFilter(pgnFile.getName()), null);
    }

    public void deleteFile(final File file) {
        try {
            FileUtils.forceDelete(file);
        } catch (IOException ex) {
            LOG.error("Error deleting file.", ex);
        }
    }

    public void createFolder(final String name) {
        try {
            FileUtils.forceMkdir(new File(name));
        } catch (IOException ex) {
            LOG.error("Error creating folder.", ex);
        }
    }

    public void moveFileToFolder(final File file, final File folder) {
        try {
            FileUtils.moveFileToDirectory(file, folder, true);
        } catch (IOException ex) {
            LOG.error("Error moving file to folder.", ex);
        }
    }

}
