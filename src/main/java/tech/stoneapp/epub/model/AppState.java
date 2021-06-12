package tech.stoneapp.epub.model;

import java.util.ArrayList;

public class AppState {
    private ArrayList<EPUBFile> files;
    private int convertedFiles = 0;
    private AppMode mode;

    public AppState() {
        init();
    }

    public void init() {
        this.files = new ArrayList<>();
        this.convertedFiles = 0;
        mode = AppMode.SELECTING;
    }

    public void addFile(EPUBFile file) {
        files.add(file);
    }

    public ArrayList<EPUBFile> getFiles() {
        return files;
    }
}
