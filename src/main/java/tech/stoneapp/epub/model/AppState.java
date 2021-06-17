package tech.stoneapp.epub.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;

public class AppState {
    private SimpleListProperty<EPUBFile> files = new SimpleListProperty<>(FXCollections.observableArrayList());
    private ObjectProperty<AppMode> mode = new SimpleObjectProperty<>(AppMode.SELECTING);
    private AppConfig config = AppConfig.loadConfig();

    public AppState() {
        init();
    }

    public void init() {
        files.clear();
        mode.setValue(AppMode.SELECTING);
    }

    public void addFile(EPUBFile file) {
        // keep uniqueness
        if (files.contains(file)) return;

        files.add(file);
    }

    public void removeFile(EPUBFile file) {
        files.remove(file);
    }

    public SimpleListProperty<EPUBFile> getFiles() {
        return files;
    }

    public ObjectProperty<AppMode> getMode() {
        return mode;
    }

    public AppMode getModeValue() {
        return mode.getValue();
    }

    public void setMode(AppMode mode) {
        this.mode.set(mode);
    }

    public void reloadConfig() {
        config = AppConfig.loadConfig();
    }

    public AppConfig getConfig() {
        return config;
    }
}