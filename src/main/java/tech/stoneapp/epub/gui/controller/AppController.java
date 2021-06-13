package tech.stoneapp.epub.gui.controller;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

import tech.stoneapp.epub.gui.GUILauncher;
import tech.stoneapp.epub.model.EPUBFile;

public class AppController implements Initializable {
    @FXML AnchorPane root;
    @FXML Button selectDirectoryButton;
    @FXML Button removeFileButton;
    @FXML Button selectFileButton;

    @FXML TableView<EPUBFile> fileList;
    @FXML TableColumn<EPUBFile, String> statusColumn;
    @FXML TableColumn<EPUBFile, String> nameColumn;
    @FXML TableColumn<EPUBFile, String> pathColumn;

    @FXML Pane status;
    @FXML Label progressLabel;
    @FXML Button convertButton;
    @FXML ProgressBar progressbar;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select EPUB files...");
        fileChooser.getExtensionFilters().addAll(
                new ExtensionFilter("EPUB Files", "*.epub")
        );

        selectFileButton.setOnMouseClicked(ev -> {
            List<File> selectedFiles = fileChooser.showOpenMultipleDialog(GUILauncher.getMainStage());
            System.out.println(selectedFiles);
        });
    }
}