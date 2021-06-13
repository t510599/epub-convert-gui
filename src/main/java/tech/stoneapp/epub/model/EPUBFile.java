package tech.stoneapp.epub.model;

import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;
import tech.stoneapp.epub.convertor.EPUBConvertor;
import tech.stoneapp.epub.exception.NotEPUBException;

import java.io.*;
import java.nio.file.Files;
import java.util.Objects;

public class EPUBFile {
    private String filename;
    private String path;
    private File file;
    private ConvertStatus status = ConvertStatus.PENDING;

    public EPUBFile(String filepath) throws FileNotFoundException, NotEPUBException {
        this.file = new File(filepath).getAbsoluteFile();
        if (!this.file.exists()) {
            throw new FileNotFoundException();
        }
        if (this.file.isDirectory() || !isEPUB(this.file)) {
            throw new NotEPUBException();
        }

        this.path = this.file.getAbsolutePath();
        this.filename = this.file.getName();
    }

    // signature security
    // https://stackoverflow.com/a/18634125/9039813
    // only EPUBConvertor should be able to update status
    public void updateStatus(ConvertStatus status, EPUBConvertor.EPUBAccessor accessor) {
        // slap you with NullPointerException
        Objects.requireNonNull(accessor);
        this.status = status;
    }

    public static boolean isEPUB(File file) {
        try {
            return Files.probeContentType(file.toPath()).equals("application/epub+zip");
        } catch (IOException e) {
            return false;
        }
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