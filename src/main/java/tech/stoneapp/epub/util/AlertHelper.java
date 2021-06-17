package tech.stoneapp.epub.util;

import javafx.scene.control.Alert;

public class AlertHelper {
    /**
     * Show alert with min alert level.
     * @param type Alert type to show.
     * @param content Alert content.
     * @param minAlertLevel Min alertLevel.
     */
    public static void show(Alert.AlertType type, String content, Alert.AlertType minAlertLevel) {
        if (type.ordinal() < minAlertLevel.ordinal()) return;

        new Alert(type, content).show();
    }
}