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

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.*;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

import tech.stoneapp.epub.convertor.EPUBConvertor;
import tech.stoneapp.epub.exception.NotEPUBException;
import tech.stoneapp.epub.gui.GUILauncher;
import tech.stoneapp.epub.model.AppMode;
import tech.stoneapp.epub.model.AppState;
import tech.stoneapp.epub.model.ConvertStatus;
import tech.stoneapp.epub.model.EPUBFile;
import tech.stoneapp.epub.util.AlertHelper;
import tech.stoneapp.epub.util.DesktopAPI;
import tech.stoneapp.epub.util.Pair;

public class AppController implements Initializable {
    @FXML GridPane root;
    @FXML Button settingsButton;
    @FXML Button importDirectoryButton;
    @FXML Button addFileButton;
    @FXML Button removeFileButton;
    @FXML Button showFileButton;

    @FXML TableView<EPUBFile> fileList;
    @FXML TableColumn<EPUBFile, String> statusColumn;
    @FXML TableColumn<EPUBFile, String> nameColumn;
    @FXML TableColumn<EPUBFile, String> pathColumn;

    @FXML Label modeLabel;
    @FXML Label progressLabel;
    @FXML Button convertButton;
    @FXML ProgressBar progressbar;

    private AppState state = GUILauncher.getState();
    private EPUBConvertor convertor = EPUBConvertor.getInstance();
    private ConversionTask conversionTask;
    private Thread conversionThread;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        /* file list setup start */

        // set table
        fileList.setItems(state.getFiles());
        fileList.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        pathColumn.minWidthProperty().bind(fileList.widthProperty().multiply(0.66));

        statusColumn.setCellValueFactory(data -> data.getValue().getStatus().asString());
        statusColumn.setCellFactory(data -> new TableCell<>() {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(item);
                if (empty) return;

                toggleClassMap(this, Map.of("positive", item.equals("SUCCESS"), "negative", item.equals("FAILED")));
            }
        });
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

        // we should not modify file list while converting.
        Arrays.asList(importDirectoryButton, addFileButton)
                .forEach(btn ->
                        btn.disableProperty().bind(state.getMode().isEqualTo(AppMode.CONVERTING))
                );

        // table item buttons
        showFileButton.setOnMouseClicked(ev -> {
            EPUBFile selectedFile = fileList.getSelectionModel().getSelectedItem();
            String targetPath = selectedFile.getPath();

            if (selectedFile.getStatusValue() == ConvertStatus.SUCCESS) {
                targetPath = selectedFile.getOutputPath();
            }

            File targetFile = new File(targetPath);
            if (!targetFile.exists()) {
                AlertHelper.show(Alert.AlertType.ERROR, "File may be moved or deleted.", state.getConfig().getAlertLevel());
                return;
            }

            if (!DesktopAPI.showInFolder(targetFile)) {
                AlertHelper.show(Alert.AlertType.ERROR, "Show In Folder is not supported on your system.", state.getConfig().getAlertLevel());
            };
        });
        // only available if file got selected
        showFileButton.disableProperty().bind(Bindings.isEmpty(fileList.getSelectionModel().getSelectedItems()));

        removeFileButton.setOnMouseClicked(ev -> {
            EPUBFile selectedFile = fileList.getSelectionModel().getSelectedItem();
            state.removeFile(selectedFile);

            if (state.getModeValue() != AppMode.SELECTING) state.setMode(AppMode.SELECTING);
        });
        // we should not modify file list while converting.
        removeFileButton.disableProperty().bind(Bindings.isEmpty(fileList.getSelectionModel().getSelectedItems())
                .or(state.getMode().isEqualTo(AppMode.CONVERTING)));

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

                // only show alert if there is only one file and it is not an EPUB.
                if (files.size() == 1 && files.get(0).isFile() && !EPUBFile.isEPUB(files.get(0))) {
                    AlertHelper.show(Alert.AlertType.WARNING, "This file is not EPUB!", state.getConfig().getAlertLevel());
                    ev.consume();
                    return;
                }

                // import files
                List<File> epubFiles = new ArrayList<>();
                for (var f: files) {
                    if (!f.exists()) continue;
                    if (f.isFile() && f.getName().endsWith(".epub")) epubFiles.add(f);
                    if (f.isDirectory()) {
                        try {
                            epubFiles.addAll(
                                    Files.walk(f.toPath()).filter(path -> path.toString().endsWith(".epub"))
                                            .map(Path::toFile).collect(Collectors.toList())
                            );
                        } catch (IOException e) {
                            continue;
                        }
                    }
                }
                importEPUB(epubFiles);
            }
            ev.consume();
        });

        // unselect file
        root.setOnKeyPressed(ev -> {
            if (ev.getCode() == KeyCode.ESCAPE) {
                fileList.getSelectionModel().clearSelection();
            }
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
                .or(state.getMode().isEqualTo(AppMode.INTERRUPTED))
                .or(state.getFiles().sizeProperty().isEqualTo(0))); // No file
        convertButton.textProperty().bind(
                Bindings.when(state.getMode().isNotEqualTo(AppMode.CONVERTING))
                        .then("Convert")
                        .otherwise("Cancel")
        );

        // prevent conversionTask == null
        // complex binding for state changes
        state.getMode().addListener((observable, oldValue, newValue) -> {
            bindProgressLabel(newValue);
            bindProgressBar(newValue);

            Map<String, Boolean> convertButtonClassMap;
            if (newValue != AppMode.CONVERTING) {
                convertButtonClassMap = Map.of("primary", true, "negative", false);
            } else {
                convertButtonClassMap = Map.of("primary", false, "negative", true);
            }
            toggleClassMap(convertButton, convertButtonClassMap);

            Map<String, Boolean> progressbarClassMap;
            switch (newValue) {
                case DONE -> progressbarClassMap = Map.of("positive", true, "negative", false);
                case INTERRUPTED -> progressbarClassMap = Map.of("positive", false, "negative", true);
                default -> progressbarClassMap = Map.of("positive", false, "negative", false);
            }
            toggleClassMap(progressbar, progressbarClassMap);
        });
        // initialize with default value
        bindProgressLabel(state.getModeValue());
        bindProgressBar(state.getModeValue());

        modeLabel.textProperty().bind(state.getMode().asString());
        /* status pane setup end */

        settingsButton.setOnMouseClicked(ev -> {
            GUILauncher.showSettings();
        });
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

    private void toggleClass(Node node, String className, boolean enable) {
        List<String> classList = node.getStyleClass();
        if (classList.contains(className) != enable) {
            if (enable) classList.add(className);
            else classList.remove(className);
        }
    }

    private void toggleClassMap(Node node, Map<String, Boolean> classMap) {
        for (var entry: classMap.entrySet()) {
            toggleClass(node, entry.getKey(), entry.getValue());
        }
    }

    private void importEPUB(List<File> files) {
        // reset
        if (state.getModeValue() != AppMode.SELECTING) state.init();
        if (files == null) return;

        int beforeImportFilesCount = state.getFiles().size();

        for (File f: files) {
            EPUBFile epub;
            try {
                epub = new EPUBFile(f.getAbsolutePath());
            } catch (FileNotFoundException | NotEPUBException e) {
                continue;
            }
            state.addFile(epub);
        }

        AlertHelper.show(
                Alert.AlertType.INFORMATION,
                MessageFormat.format("Imported Files: {0}", state.getFiles().size() - beforeImportFilesCount),
                state.getConfig().getAlertLevel()
        );
    }

    private void startConversion() {
        if (state.getModeValue() != AppMode.SELECTING) return;

        conversionTask = new ConversionTask(state.getFiles());
        // bind event listeners
        conversionTask.setOnSucceeded(ev -> {
            state.setMode(AppMode.DONE);
        });
        conversionTask.setOnFailed(ev -> {
            state.setMode(AppMode.INTERRUPTED);
        });
        conversionTask.setOnCancelled(ev -> {
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

        if (conversionTask != null) conversionTask.cancel();
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
            // put initial value so the progress won't be -1/-1
            this.updateProgress(0, files.size());
            for (EPUBFile file: files) {
                try {
                    convertor.convert(file, state.getConfig());
                    successConversion++;
                } catch (InterruptedException e) {
                    throw e;
                } catch (Exception e) {
                    // Interruption is dealt in convertor
                    failedConversion++;
                    e.printStackTrace();
                }
                this.updateProgress(successConversion + failedConversion, files.size());
            }
            return new Pair<>(successConversion, failedConversion);
        }
    }
}