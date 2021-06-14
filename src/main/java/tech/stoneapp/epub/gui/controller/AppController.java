package tech.stoneapp.epub.gui.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

import tech.stoneapp.epub.exception.NotEPUBException;
import tech.stoneapp.epub.gui.GUILauncher;
import tech.stoneapp.epub.model.AppState;
import tech.stoneapp.epub.model.EPUBFile;

public class AppController implements Initializable {
    @FXML AnchorPane root;
    @FXML Button importDirectoryButton;
    @FXML Button addFileButton;
    @FXML Button removeFileButton;
    @FXML Button showFileButton;

    @FXML TableView<EPUBFile> fileList;
    @FXML TableColumn<EPUBFile, String> statusColumn;
    @FXML TableColumn<EPUBFile, String> nameColumn;
    @FXML TableColumn<EPUBFile, String> pathColumn;

    @FXML Pane statusPane;
    @FXML Label progressLabel;
    @FXML Button convertButton;
    @FXML ProgressBar progressbar;

    private AppState state = new AppState();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        addFileButton.setOnMouseClicked(ev -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select EPUB files...");
            fileChooser.getExtensionFilters().addAll(
                    new ExtensionFilter("EPUB Files", "*.epub")
            );

            List<File> selectedFiles = fileChooser.showOpenMultipleDialog(GUILauncher.getMainStage());
            importEPUB(selectedFiles);
        });

        importDirectoryButton.setOnMouseClicked(ev -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Select directory...");

            File selectedDirectory = directoryChooser.showDialog(GUILauncher.getMainStage());
            List<File> files = null;
            try {
                files = Files.walk(selectedDirectory.toPath())
                        .filter(path -> path.toString().endsWith(".epub"))
                        .map(Path::toFile)
                        .collect(Collectors.toList());
            } catch (IOException e) {
                e.printStackTrace();
            }
            importEPUB(files);
        });
    }

    private void importEPUB(List<File> files) {
        if (files == null) return;

        for (File f: files) {
            EPUBFile epub;
            try {
                epub = new EPUBFile(f.getAbsolutePath());
            } catch (FileNotFoundException | NotEPUBException e) {
                continue;
            }
            state.addFile(epub);
        }
    }
}