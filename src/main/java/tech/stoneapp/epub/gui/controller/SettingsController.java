package tech.stoneapp.epub.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;

import tech.stoneapp.epub.gui.GUILauncher;
import tech.stoneapp.epub.model.AppConfig;
import tech.stoneapp.epub.model.AppState;
import tech.stoneapp.epub.util.AlertHelper;

import java.io.File;
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
        String outputDirectory = config.getOutputDirectory();
        if (outputDirectory == null || !(new File(outputDirectory).exists())) {
            path.selectToggle(path.getToggles().get(0));
        } else {
            path.selectToggle(path.getToggles().get(1));
            pathInput.setText(outputDirectory);
        }
        selectPathButton.setOnMouseClicked(ev -> {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Choose output directory...");
            File dir = chooser.showDialog(GUILauncher.getSettingsStage());

            if (dir == null) return;

            if (!dir.exists()) {
                AlertHelper.show(Alert.AlertType.ERROR, "Directory does not exist!", config.getAlertLevel());
                return;
            }
            pathInput.setText(dir.getAbsolutePath());
        });
        selectPathButton.disableProperty().bind(path.getToggles().get(0).selectedProperty());

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

        saveButton.setOnMouseClicked(ev -> {
            AppConfig.saveConfig(new AppConfig(
                    path.getToggles().indexOf(path.getSelectedToggle()) == 0 ? null : pathInput.getText(),
                    filenameChoiceBox.getSelectionModel().getSelectedItem(),
                    overwriteCheckbox.isSelected(),
                    alertLevel.getSelectionModel().getSelectedItem()
            ));
            state.reloadConfig();
            GUILauncher.getSettingsStage().close();
        });
        cancelButton.setOnMouseClicked(ev -> {
            GUILauncher.getSettingsStage().close();
        });
    }
}