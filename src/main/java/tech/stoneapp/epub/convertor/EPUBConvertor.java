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

import tech.stoneapp.epub.model.AppConfig;
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

    public void convert(EPUBFile epub, AppConfig config) throws IOException, ArchiveException, InterruptedException {
        if (epub.getStatusValue() != ConvertStatus.PENDING) return;

        Path outputPath = Paths.get(
                config.getOutputDirectory().equals("") ? epub.getFile().getParent() : config.getOutputDirectory(),
                generateFilename(config.getOutputFilenameMode(), epub.getFilename())
        );

        // do not overwrite the exist file
        if (!config.isOverwrite() && outputPath.toFile().exists()) {
            epub.updateStatus(ConvertStatus.SKIPPED, accessor);
            return;
        }

        epub.updateStatus(ConvertStatus.CONVERTING, accessor);

        try {
            ZipFile file = new ZipFile(epub.getPath());
            ZipArchiveEntry entry;

            ByteArrayOutputStream newMemoryFile = new ByteArrayOutputStream();
            ArchiveOutputStream newEPUB = new ArchiveStreamFactory().createArchiveOutputStream(ArchiveStreamFactory.ZIP, newMemoryFile);

            Enumeration<ZipArchiveEntry> entries = file.getEntries();
            while (entries.hasMoreElements()) {
                // stop conversion if interrupt signal occurs.
                if (Thread.currentThread().isInterrupted()) throw new InterruptedException();

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

            // save to disk, if not interruption
            // in case interrupt signal wasn't caught in while loop above
            if (Thread.currentThread().isInterrupted()) throw new InterruptedException();

            OutputStream newFile = new FileOutputStream(outputPath.toString());
            newFile.write(newMemoryFile.toByteArray());
            newFile.close();

            epub.updateOutputPath(outputPath.toAbsolutePath().toString(), accessor);
            epub.updateStatus(ConvertStatus.SUCCESS, accessor);
        } catch (Exception ex) {
            epub.updateStatus(ConvertStatus.FAILED, accessor);
            throw ex;
        }
    }

    private String generateFilename(AppConfig.OutputFilenameMode mode, String originalFilename) {
        String filename = originalFilename;
        switch (mode) {
            case TRANSLATE -> filename = ZhConverterUtil.toTraditional(originalFilename);
            case SUFFIX -> filename = originalFilename.replace(".epub", "-tc.epub");
            case BOTH -> filename = ZhConverterUtil.toTraditional(originalFilename).replace(".epub", "-tc.epub");
        }
        return filename;
    }
}