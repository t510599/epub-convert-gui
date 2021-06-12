package tech.stoneapp.epub.model;

import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;

import java.io.*;

public class EPUBFile {
    private String filename;
    private String path;
    private File file;
    private ConvertStatus status = ConvertStatus.PENDING;

    public EPUBFile(String filepath) throws FileNotFoundException {
        this.file = new File(filepath);
        if (!this.file.exists()) {
            throw new FileNotFoundException();
        }

        this.path = this.file.getAbsolutePath();
        this.filename = this.file.getName();
    }

    public void updateStatus(ConvertStatus status) {
        this.status = status;
    }

    public String getFilename() {
        return filename;
    }

    public String getPath() {
        return path;
    }

    public File getFile() {
        return file;
    }

    public ConvertStatus getStatus() {
        return status;
    }
}
