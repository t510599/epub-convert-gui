package tech.stoneapp.epub.gui.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

import javafx.application.HostServices;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.*;
import javafx.concurrent.Task;
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

import org.apache.commons.compress.archivers.ArchiveException;
import tech.stoneapp.epub.convertor.EPUBConvertor;
import tech.stoneapp.epub.exception.NotEPUBException;
import tech.stoneapp.epub.gui.GUILauncher;
import tech.stoneapp.epub.model.AppMode;
import tech.stoneapp.epub.model.AppState;
import tech.stoneapp.epub.model.EPUBFile;
import tech.stoneapp.epub.util.Pair;

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
    @FXML Label modeLabel;
    @FXML Label progressLabel;
    @FXML Button convertButton;
    @FXML ProgressBar progressbar;

    private AppState state = new AppState();
    private EPUBConvertor convertor = EPUBConvertor.getInstance();
    private ConversionTask conversionTask;
    private Thread conversionThread;

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

        /* status pane setup start */
        convertButton.setOnMouseClicked(ev -> {
            if (state.getModeValue() == AppMode.SELECTING)
                startConversion();
            else
                interruptConversion();
        });
        convertButton.disableProperty().bind(state.getMode().isEqualTo(AppMode.DONE)
                .or(state.getMode().isEqualTo(AppMode.INTERRUPTED)));
        convertButton.textProperty().bind(
                Bindings.when(state.getMode().isEqualTo(AppMode.CONVERTING))
                        .then("Cancel")
                        .otherwise("Convert")
        );

        // prevent conversionTask == null
        state.getMode().addListener((observable, oldValue, newValue) -> {
            bindProgressLabel(newValue);
            bindProgressBar(newValue);
        });
        // initialize with default value
        bindProgressLabel(state.getModeValue());
        bindProgressBar(state.getModeValue());

        modeLabel.textProperty().bind(state.getMode().asString());
        /* status pane setup end */
    }

    private void bindProgressLabel(AppMode mode) {
        StringBinding binding;
        if (mode == AppMode.SELECTING || conversionTask == null) {
            binding = Bindings.createStringBinding(
                    () -> MessageFormat.format("0 / {0}", state.getFiles().getSize()),
                    state.getFiles().sizeProperty()
            );
        } else {
            binding = Bindings.createStringBinding(
                    () -> MessageFormat.format("{0} / {1}", conversionTask.getWorkDone(), conversionTask.getTotalWork()),
                    conversionTask.workDoneProperty(),
                    conversionTask.totalWorkProperty()
            );
        }

        progressLabel.textProperty().bind(binding);
    }

    private void bindProgressBar(AppMode mode) {
        ReadOnlyDoubleProperty binding;
        if (mode == AppMode.SELECTING || conversionTask == null) {
            binding = new ReadOnlyDoubleWrapper(0);
        } else {
            binding = conversionTask.progressProperty();
        }

        progressbar.progressProperty().bind(binding);
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

    private void startConversion() {
        if (state.getModeValue() != AppMode.SELECTING) return;

        conversionTask = new ConversionTask(state.getFiles());
        conversionTask.setOnSucceeded(ev -> {
            state.setMode(AppMode.DONE);
        });
        conversionTask.setOnFailed(ev -> {
            state.setMode(AppMode.INTERRUPTED);
        });

        if (conversionThread != null && conversionThread.isAlive()) conversionThread.interrupt();

        conversionThread = new Thread(conversionTask);
        conversionThread.setDaemon(true);
        conversionThread.start();

        // state should change after task starts, or binding would get wrong task.
        state.setMode(AppMode.CONVERTING);
    }

    private void interruptConversion() {
        if (state.getModeValue() != AppMode.CONVERTING) return;

        if (conversionThread != null) conversionThread.interrupt();
        state.setMode(AppMode.INTERRUPTED);
    }

    private class ConversionTask extends Task<Pair<Integer, Integer>> {
        int successConversion = 0;
        int failedConversion = 0;
        List<EPUBFile> files;

        public ConversionTask(List<EPUBFile> files) {
            this.files = files;
        }

        @Override
        public Pair<Integer, Integer> call() throws InterruptedException {
            this.updateProgress(0, files.size());
            for (EPUBFile file: files) {
                try {
                    convertor.convert(file);
                    successConversion++;
                } catch (IOException | ArchiveException e) {
                    // Interruption is dealt in convertor
                    failedConversion++;
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    throw e;
                }
                this.updateProgress(successConversion + failedConversion, files.size());
            }
            return new Pair<>(successConversion, failedConversion);
        }
    }
}