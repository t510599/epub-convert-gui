package tech.stoneapp.epub.model;

public enum AppMode {
    SELECTING,
    CONVERTING,
    INTERRUPTED,
    DONE;

    @Override
    public String toString() {
        return this.name();
    }
}