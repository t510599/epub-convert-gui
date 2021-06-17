package tech.stoneapp.epub.model;

import javafx.scene.control.Alert;

public class AppConfig {
    // empty stands for saving to input file's directory
    private String outputDirectory = "";
    private OutputFilenameMode outputFilenameMode = OutputFilenameMode.TRANSLATE;
    private boolean overwrite = true;
    private Alert.AlertType alertLevel = Alert.AlertType.INFORMATION;

    public AppConfig() {}

    public OutputFilenameMode getOutputFilenameMode() {
        return outputFilenameMode;
    }

    public boolean isOverwrite() {
        return overwrite;
    }

    public Alert.AlertType getAlertLevel() {
        return alertLevel;
    }

    public static AppConfig loadConfig() {
        return new AppConfig();
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