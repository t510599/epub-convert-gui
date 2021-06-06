package tech.stoneapp.epub.cli;

import java.io.*;
import java.util.Enumeration;

import com.github.houbb.opencc4j.util.ZhConverterUtil;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;

import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;

public class CommandLineApp {
    public static void main(String[] args) {
        System.out.println(System.getProperty("user.dir"));
        for (String filename: args) {
            ByteArrayOutputStream newMemoryFile = new ByteArrayOutputStream();
            try {
                File f = new File(filename);
                String result = ZhConverterUtil.toTraditional(filename);
                System.out.printf("%s, size: %d\n", result, f.length());

                ZipFile epub = new ZipFile(f);
                ZipArchiveEntry entry;
//                OutputStream newFile = new FileOutputStream(result);
                ArchiveOutputStream newZip = new ArchiveStreamFactory().createArchiveOutputStream(ArchiveStreamFactory.ZIP, newMemoryFile);

                Enumeration<ZipArchiveEntry> entries = epub.getEntries();
                while (entries.hasMoreElements()) {
                    entry = entries.nextElement();
                    InputStream stream = epub.getInputStream(entry);
                    newZip.putArchiveEntry(new ZipArchiveEntry(ZhConverterUtil.toTraditional(entry.getName())));
                    IOUtils.copy(stream, newZip);
                    newZip.closeArchiveEntry();
                    System.out.printf("[%s] %s size:%s\n", result, entry.getName(), entry.getSize());
                }

                newZip.finish();
                newMemoryFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ArchiveException e) {
                e.printStackTrace();
            }


            try {
                OutputStream newFile = new FileOutputStream(ZhConverterUtil.toTraditional(filename));
                newFile.write(newMemoryFile.toByteArray());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
