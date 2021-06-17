package tech.stoneapp.epub.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tech.stoneapp.epub.model.AppState;

import java.io.IOException;

public class GUILauncher extends Application {
    private static final int screenWidth = 720;
    private static final int screenHeight = 540;
    private static Stage mainStage;
    private static Stage settingsStage;
    private static AppState state = new AppState();

    @Override
    public void start(Stage stage) {
        try {
            mainStage = stage;

            Parent root = FXMLLoader.load(getClass().getResource("fxml/app.fxml"));
            Scene scene = new Scene(root, screenWidth, screenHeight);
            Image appIcon = new Image(getClass().getResourceAsStream("image/baseline_book_black_24dp.png"));

            mainStage.setTitle("EPUB Convert");
            mainStage.getIcons().add(appIcon);
            mainStage.setScene(scene);
            mainStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void showSettings() {
        if (settingsStage != null && settingsStage.isShowing()) return;

        try {
            settingsStage = new Stage();
            Parent root = FXMLLoader.load(GUILauncher.class.getResource("fxml/settings.fxml"));
            Scene scene = new Scene(root, screenWidth * 2 / 3, screenHeight * 2 / 3);
            Image appIcon = new Image(GUILauncher.class.getResourceAsStream("image/baseline_settings_black_24dp.png"));

            settingsStage.setTitle("Settings");
            settingsStage.getIcons().add(appIcon);
            settingsStage.setScene(scene);
            // lock main window until settings close.
            settingsStage.initModality(Modality.APPLICATION_MODAL);
            settingsStage.initOwner(mainStage);

            settingsStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Stage getMainStage() {
        return mainStage;
    }

    public static Stage getSettingsStage() {
        return settingsStage;
    }

    public static AppState getState() {
        return state;
    }

    public static void main(String[] args) {
        launch(args);
    }

}