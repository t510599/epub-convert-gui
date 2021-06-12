package tech.stoneapp.epub.convertor;

import com.github.houbb.opencc4j.util.ZhConverterUtil;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;

import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import tech.stoneapp.epub.model.ConvertStatus;
import tech.stoneapp.epub.model.EPUBFile;

import java.io.*;
import java.util.Arrays;
import java.util.Enumeration;

public class EPUBConvertor {
    private final TextFileConvertor textFileConvertor = new TextFileConvertor();
    private final String[] textFileExtensions = { "htm", "html", "xhtml", "css", "ncx", "opf" };

    public EPUBConvertor() {}

    public void convert(EPUBFile epub) throws IOException, ArchiveException {
        if (epub.getStatus() != ConvertStatus.PENDING) return;

        epub.updateStatus(ConvertStatus.CONVERTING);

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
            if (Arrays.stream(textFileExtensions).anyMatch(extension::equals)) {
                ByteArrayOutputStream result = textFileConvertor.convert(stream);
                // replace old stream to converted stream
                stream = new ByteArrayInputStream(result.toByteArray());
            }
            // copy file to new EPUB
            IOUtils.copy(stream, newEPUB);
            newEPUB.closeArchiveEntry();
        }

        newEPUB.finish();
        newMemoryFile.close();
    }
}
