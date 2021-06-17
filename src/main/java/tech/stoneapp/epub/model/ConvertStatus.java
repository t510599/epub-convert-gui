package tech.stoneapp.epub.model;

public enum ConvertStatus {
    PENDING,
    CONVERTING,
    SKIPPED,
    SUCCESS,
    FAILED;

    @Override
    public String toString() {
        return this.name();
    }
}