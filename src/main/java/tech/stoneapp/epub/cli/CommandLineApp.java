package tech.stoneapp.epub.cli;

import java.io.*;

import org.apache.commons.cli.*;
import org.apache.commons.compress.archivers.ArchiveException;
import tech.stoneapp.epub.convertor.EPUBConvertor;
import tech.stoneapp.epub.exception.NotEPUBException;
import tech.stoneapp.epub.model.AppConfig;
import tech.stoneapp.epub.model.AppConfig.OutputFilenameMode;
import tech.stoneapp.epub.model.EPUBFile;

public class CommandLineApp {
    public static void main(String[] args) {
        EPUBConvertor convertor = EPUBConvertor.getInstance();

        Options options = new Options();
        options.addOption(new Option("h", "help", false, "Show this message"));
        options.addOption(Option.builder("o")
                        .argName("directory")
                        .longOpt("output")
                        .hasArg()
                        .desc("The output directory. (Default: output to the same folder as input file(s).)")
                        .build()
        );
        options.addOption(Option.builder("f")
                .argName("mode")
                .longOpt("filename")
                .hasArg()
                .type(OutputFilenameMode.class)
                .desc("The output filename format. (Values: TRANSLATE (default), SUFFIX, BOTH)")
                .build()
        );
        options.addOption(new Option("w", "overwrite", false, "If output file exists, overwrite it"));

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            e.printStackTrace();
            return;
        }

        if (cmd.hasOption("h")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.setOptionComparator(null);
            formatter.setWidth(100);
            formatter.printHelp("commandName <FILE> [OPTIONS]",
                    "Convert simplified chinese to traditional chinese in EPUB.",
                    options,
                    "To launch GUI, run command without any argument."
            );
            return;
        }

        String path = null;
        if (cmd.hasOption("o")) {
            path = cmd.getOptionValue("o");
            if (!(new File(path).exists())) {
                System.out.println("Output path does not exists!");
                return;
            }
        }

        OutputFilenameMode mode = null;
        if (cmd.hasOption("f")) {
            mode = OutputFilenameMode.fromName(cmd.getOptionValue("f"));
        }
        if (mode == null) System.out.println("Ignored wrong mode. Use default mode (TRANSLATE).");

        AppConfig config = new AppConfig(
                path,
                mode == null ? OutputFilenameMode.TRANSLATE : mode,
                cmd.hasOption("w")
        );

        if (cmd.getArgList().size() < 1) System.out.println("No file to convert!");

        for (String filepath: cmd.getArgList()) {
            long startTime = System.currentTimeMillis();

            try {
                EPUBFile epub = new EPUBFile(filepath);
                convertor.convert(epub, config);

                long currentTime = System.currentTimeMillis();
                String message = "";
                switch (epub.getStatusValue()) {
                    case PENDING -> message = "File %s has not been converted.";
                    case SKIPPED -> message = "File %s seems to be converted. Skipped.";
                    case SUCCESS -> message = "%s has been converted successfully.";
                    case FAILED -> message = "Conversion of %s has failed.";
                    default -> message = "Unknown Error for %s.";
                }
                message = String.format(message, epub.getFilename());
                System.out.printf("%s Elapsed time: %.2f s%n", message, (float)(currentTime - startTime) / 1000);
            } catch (FileNotFoundException e) {
                System.out.printf("%s not found.%n", filepath);
            } catch(IOException | ArchiveException | InterruptedException e) {
                e.printStackTrace();
            } catch (NotEPUBException e) {
                System.out.printf("%s is not an EPUB. Skipped.%n", filepath);
            }
        }
    }
}