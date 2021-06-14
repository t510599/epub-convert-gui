package tech.stoneapp.epub.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import tech.stoneapp.epub.convertor.EPUBConvertor;
import tech.stoneapp.epub.exception.NotEPUBException;

import java.io.*;
import java.nio.file.Files;
import java.util.Objects;

public class EPUBFile {
    private String filename;
    private String path;
    private File file;
    private ObjectProperty<ConvertStatus> status = new SimpleObjectProperty<>(ConvertStatus.PENDING);

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
        this.status.setValue(status);
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
        return status.getValue();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EPUBFile epubFile = (EPUBFile) o;

        if (!Objects.equals(filename, epubFile.filename)) return false;
        if (!Objects.equals(path, epubFile.path)) return false;
        if (!Objects.equals(file, epubFile.file)) return false;
        return status != null ? status.getValue().equals(epubFile.status.getValue()) : epubFile.status == null;
    }

    @Override
    public int hashCode() {
        int result = filename != null ? filename.hashCode() : 0;
        result = 31 * result + (path != null ? path.hashCode() : 0);
        result = 31 * result + (file != null ? file.hashCode() : 0);
        result = 31 * result + (status != null ? status.getValue().hashCode() : 0);
        return result;
    }
}