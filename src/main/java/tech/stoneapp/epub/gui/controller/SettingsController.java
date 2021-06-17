package tech.stoneapp.epub.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import tech.stoneapp.epub.gui.GUILauncher;
import tech.stoneapp.epub.model.AppConfig;
import tech.stoneapp.epub.model.AppState;

import java.net.URL;
import java.util.ResourceBundle;

public class SettingsController implements Initializable {
    @FXML private VBox root;

    @FXML private ToggleGroup path;
    @FXML private TextField pathInput;
    @FXML private Button selectPathButton;

    @FXML private ChoiceBox<AppConfig.OutputFilenameMode> filenameChoiceBox;
    @FXML private CheckBox overwriteCheckbox;

    @FXML private ChoiceBox<Alert.AlertType> alertLevel;

    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private AppState state = GUILauncher.getState();
    private AppConfig config = state.getConfig();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        filenameChoiceBox.getItems().addAll(
                AppConfig.OutputFilenameMode.TRANSLATE,
                AppConfig.OutputFilenameMode.SUFFIX,
                AppConfig.OutputFilenameMode.BOTH
        );
        filenameChoiceBox.getSelectionModel().select(config.getOutputFilenameMode());
        overwriteCheckbox.setSelected(config.isOverwrite());

        alertLevel.getItems().addAll(
                Alert.AlertType.INFORMATION,
                Alert.AlertType.WARNING,
                Alert.AlertType.ERROR
        );
        alertLevel.getSelectionModel().select(config.getAlertLevel());

        cancelButton.setOnMouseClicked(ev -> {
            GUILauncher.getSettingsStage().close();
        });
    }
}