package tech.stoneapp.epub.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class AppState {
    private ObservableList<EPUBFile> files = FXCollections.observableArrayList();
    private ObjectProperty<AppMode> mode = new SimpleObjectProperty<>(AppMode.SELECTING);
    private int convertedFiles = 0;

    public AppState() {
        init();
    }

    public void init() {
        files.clear();
        convertedFiles = 0;
        mode.setValue(AppMode.SELECTING);
    }

    public void addFile(EPUBFile file) {
        // keep uniqueness
        if (files.contains(file)) return;

        files.add(file);
    }

    public ObservableList<EPUBFile> getFiles() {
        return files;
    }

    public AppMode getMode() {
        return mode.getValue();
    }
}