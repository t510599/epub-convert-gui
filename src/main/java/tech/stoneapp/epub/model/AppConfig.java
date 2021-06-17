package tech.stoneapp.epub.model;

import javafx.scene.control.Alert.AlertType;

public class AppConfig {
    // empty stands for saving to input file's directory
    private String outputDirectory = "";
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
        return outputFilenameMode;
    }

    public boolean isOverwrite() {
        return overwrite;
    }

    public AlertType getAlertLevel() {
        return alertLevel;
    }

    public static AppConfig loadConfig() {
        // TODO: load config from disk by gson
        return new AppConfig();
    }

    public static void saveConfig(AppConfig config) {
        // TODO: save config to disk by gson
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