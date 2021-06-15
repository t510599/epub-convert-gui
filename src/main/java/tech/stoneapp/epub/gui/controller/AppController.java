package tech.stoneapp.epub.gui.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import javafx.application.HostServices;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

import tech.stoneapp.epub.exception.NotEPUBException;
import tech.stoneapp.epub.gui.GUILauncher;
import tech.stoneapp.epub.model.AppMode;
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
        /* file list setup start */

        // set table
        fileList.setItems(state.getFiles());
        statusColumn.setCellValueFactory(data -> data.getValue().getStatus().asString());
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("filename"));
        pathColumn.setCellValueFactory(new PropertyValueFactory<>("path"));

        // file buttons
        importDirectoryButton.setOnMouseClicked(ev -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Select directory...");

            File selectedDirectory = directoryChooser.showDialog(GUILauncher.getMainStage());
            List<File> files = null;
            try {
                if (selectedDirectory != null) {
                    files = Files.walk(selectedDirectory.toPath())
                            .filter(path -> path.toString().endsWith(".epub"))
                            .map(Path::toFile)
                            .collect(Collectors.toList());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            importEPUB(files);
        });

        addFileButton.setOnMouseClicked(ev -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select EPUB files...");
            fileChooser.getExtensionFilters().addAll(
                    new ExtensionFilter("EPUB Files", "*.epub")
            );

            List<File> selectedFiles = fileChooser.showOpenMultipleDialog(GUILauncher.getMainStage());
            importEPUB(selectedFiles);
        });

        // table item buttons
        showFileButton.setOnMouseClicked(ev -> {
            EPUBFile selectedFile = fileList.getSelectionModel().getSelectedItem();

            HostServices host = GUILauncher.getHost();

            host.showDocument(selectedFile.getFile().toURI().toString());
        });

        removeFileButton.setOnMouseClicked(ev -> {
            EPUBFile selectedFile = fileList.getSelectionModel().getSelectedItem();
            state.removeFile(selectedFile);
        });

        Arrays.asList(showFileButton, removeFileButton)
                .forEach(btn ->
                        btn.disableProperty().bind(Bindings.isEmpty(fileList.getSelectionModel().getSelectedItems()))
                );

        // drag and drop
        fileList.setOnDragOver(ev -> {
            if (ev.getDragboard().hasFiles()) {
                ev.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                // TODO: animation of drag over
            }
            ev.consume();
        });

        fileList.setOnDragDropped(ev -> {
            if (ev.getDragboard().hasFiles()) {
                List<File> files = ev.getDragboard().getFiles();

                // import files
                importEPUB(files.stream()
                        .filter(File::isFile)
                        .filter(f -> f.getName().endsWith(".epub"))
                        .collect(Collectors.toList()));

                // import directories
                List<File> directories = files.stream().filter(File::isDirectory).collect(Collectors.toList());
                List<List<File>> recursiveFiles = new ArrayList<>();
                for (File directory: directories) {
                    try {
                        List<File> fileInDirectory = Files.walk(directory.toPath())
                                .filter(path -> path.toString().endsWith(".epub"))
                                .map(Path::toFile)
                                .collect(Collectors.toList());
                        recursiveFiles.add(fileInDirectory);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                importEPUB(recursiveFiles.stream().flatMap(List::stream).collect(Collectors.toList()));
            }
            ev.consume();
        });
        /* file list setup end */
    }

    private void importEPUB(List<File> files) {
        // reset
        if (state.getModeValue() != AppMode.SELECTING) state.init();
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