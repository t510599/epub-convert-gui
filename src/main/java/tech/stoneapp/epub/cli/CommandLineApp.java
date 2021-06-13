package tech.stoneapp.epub.cli;

import java.io.*;

import org.apache.commons.compress.archivers.ArchiveException;
import tech.stoneapp.epub.convertor.EPUBConvertor;
import tech.stoneapp.epub.exception.NotEPUBException;
import tech.stoneapp.epub.model.EPUBFile;

public class CommandLineApp {
    public static void main(String[] args) {
        EPUBConvertor convertor = EPUBConvertor.getInstance();

        for (String filepath: args) {
            long startTime = System.currentTimeMillis();

            try {
                EPUBFile epub = new EPUBFile(filepath);
                convertor.convert(epub);

                long currentTime = System.currentTimeMillis();
                String message = "";
                switch (epub.getStatus()) {
                    case PENDING -> message = "File %s has not been converted.";
                    case SUCCESS -> message = "%s has been converted successfully.";
                    case FAILED -> message = "Conversion of %s has failed.";
                    default -> message = "Unknown Error for %s.";
                }
                message = String.format(message, epub.getFilename());
                System.out.printf("%s Elapsed time: %.2f s%n", message, (float)(currentTime - startTime) / 1000);
            } catch (IOException | ArchiveException e) {
                e.printStackTrace();
            } catch (NotEPUBException e) {
                System.out.printf("%s is not an EPUB. Skipped.%n", filepath);
            }
        }
    }
}