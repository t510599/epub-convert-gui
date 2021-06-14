package tech.stoneapp.epub.model;

public enum ConvertStatus {
    PENDING,
    CONVERTING,
    SUCCESS,
    FAILED;

    @Override
    public String toString() {
        return this.name();
    }
}