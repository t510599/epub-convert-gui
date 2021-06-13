package tech.stoneapp.epub.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class GUILauncher extends Application {
    private final int screenWidth = 720;
    private final int screenHeight = 540;
    private static Stage mainStage;
    private static Stage settingsStage;

    @Override
    public void start(Stage stage) {
        try {
            mainStage = stage;
            Parent root = FXMLLoader.load(getClass().getResource("fxml/app.fxml"));
            Scene scene = new Scene(root, screenWidth, screenHeight);

            mainStage.setTitle("EPUB Convert");
            mainStage.setScene(scene);
            mainStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showSettings() {
        settingsStage = new Stage();
    }

    public static Stage getMainStage() {
        return mainStage;
    }

    public static void main(String[] args) {
        launch(args);
    }

}