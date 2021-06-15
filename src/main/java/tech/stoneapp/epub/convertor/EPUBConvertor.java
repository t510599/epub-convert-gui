package tech.stoneapp.epub.convertor;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import com.github.houbb.opencc4j.util.ZhConverterUtil;

import tech.stoneapp.epub.model.EPUBFile;
import tech.stoneapp.epub.model.ConvertStatus;

public class EPUBConvertor {
    // signature security
    // https://stackoverflow.com/a/18634125/9039813
    public static final class EPUBAccessor { private EPUBAccessor() {} }
    private static final EPUBAccessor accessor = new EPUBAccessor();

    private final TextFileConvertor textFileConvertor = new TextFileConvertor();
    private final List textFileExtensions = Arrays.asList("htm", "html", "xhtml", "css", "ncx", "opf");

    // singleton
    private static EPUBConvertor instance;

    private EPUBConvertor() {}

    public static EPUBConvertor getInstance() {
        if (instance == null) instance = new EPUBConvertor();
        return instance;
    }

    public void convert(EPUBFile epub) throws IOException, ArchiveException {
        if (epub.getStatusValue() != ConvertStatus.PENDING) return;

        epub.updateStatus(ConvertStatus.CONVERTING, accessor);

        try {
            ZipFile file = new ZipFile(epub.getPath());
            ZipArchiveEntry entry;

            ByteArrayOutputStream newMemoryFile = new ByteArrayOutputStream();
            ArchiveOutputStream newEPUB = new ArchiveStreamFactory().createArchiveOutputStream(ArchiveStreamFactory.ZIP, newMemoryFile);

            Enumeration<ZipArchiveEntry> entries = file.getEntries();
            while (entries.hasMoreElements()) {
                entry = entries.nextElement();

                if (entry == null || entry.isDirectory()) continue;

                String filename = entry.getName();
                String extension = filename.contains(".") ? filename.substring(filename.lastIndexOf(".") + 1) : "";

                InputStream stream = file.getInputStream(entry);
                newEPUB.putArchiveEntry(new ZipArchiveEntry(ZhConverterUtil.toTraditional(filename)));
                // convert only text file
                if (textFileExtensions.contains(extension)) {
                    // replace old stream to converted stream
                    stream = textFileConvertor.convert(stream);
                }
                // copy file to new EPUB
                IOUtils.copy(stream, newEPUB);
                newEPUB.closeArchiveEntry();
            }

            file.close();
            newEPUB.finish();
            newMemoryFile.close();

            // save to disk
            Path outputPath = Paths.get(epub.getFile().getParent(), ZhConverterUtil.toTraditional(epub.getFilename()));
            OutputStream newFile = new FileOutputStream(outputPath.toString());
            newFile.write(newMemoryFile.toByteArray());
            newFile.close();

            epub.updateStatus(ConvertStatus.SUCCESS, accessor);
        } catch (IOException | ArchiveException ex) {
            epub.updateStatus(ConvertStatus.FAILED, accessor);
            throw ex;
        }
    }
}