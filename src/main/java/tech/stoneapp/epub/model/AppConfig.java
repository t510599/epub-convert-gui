package tech.stoneapp.epub.model;

import com.google.gson.Gson;
import javafx.scene.control.Alert.AlertType;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;

public class AppConfig {
    // null stands for saving to input file's directory
    private String outputDirectory = null;
    private OutputFilenameMode outputFilenameMode = OutputFilenameMode.TRANSLATE;
    private boolean overwrite = true;
    private AlertType alertLevel = AlertType.INFORMATION;

    public AppConfig() {}

    public AppConfig(String outputPath, OutputFilenameMode outputMode, boolean outputOverwrite, AlertType minAlertLevel) {
        outputDirectory = outputPath;
        outputFilenameMode = outputMode;
        overwrite = outputOverwrite;
        alertLevel = minAlertLevel;
    }

    public String getOutputDirectory() {
        return outputDirectory;
    }

    public OutputFilenameMode getOutputFilenameMode() {
        // if config got error enum value, it would become null. so set to default value.
        if (outputFilenameMode == null) outputFilenameMode = OutputFilenameMode.TRANSLATE;

        return outputFilenameMode;
    }

    public boolean isOverwrite() {
        return overwrite;
    }

    public AlertType getAlertLevel() {
        // if config got error enum value, it would become null. so set to default value.
        if (alertLevel == null) alertLevel = AlertType.INFORMATION;

        return alertLevel;
    }

    public static AppConfig loadConfig() {
        Gson gson = new Gson();
        AppConfig config;
        try {
            config = gson.fromJson(Files.readString(new File("config.json").toPath()), AppConfig.class);
        } catch (Exception e) {
            // if load from file failed, use default one.
            config = new AppConfig();
            AppConfig.saveConfig(config);

            if (!(e instanceof NoSuchFileException))
                e.printStackTrace();
        }
        return config;
    }

    public static void saveConfig(AppConfig config) {
        Gson gson = new Gson();
        try (PrintWriter out = new PrintWriter("config.json");){
            gson.toJson(config, out);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public enum OutputFilenameMode {
        /**
         * TRANSLATE: Convert filename to Tranditional Chinese by opencc4j
         * SUFFIX: Add "-tc" suffix to filename
         * BOTH: Translate, then add suffix
         */
        TRANSLATE("Translate to Tranditional Chinese"),
        SUFFIX("Add `-tc` suffix"),
        BOTH("Both");

        String description;
        OutputFilenameMode(String description) {
            this.description = description;
        }

        @Override
        public String toString() {
            return description;
        }
    }
}