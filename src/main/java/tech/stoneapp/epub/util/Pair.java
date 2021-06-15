package tech.stoneapp.epub.util;

public class Pair<K, V> {
    private K leftValue;
    private V rightValue;

    public Pair(K left, V right) {
        leftValue = left;
        rightValue = right;
    }

    public K getLeftValue() {
        return leftValue;
    }

    public V getRightValue() {
        return rightValue;
    }

    @Override
    public String toString() {
        return "Pair{" +
                "leftValue=" + leftValue +
                ", rightValue=" + rightValue +
                '}';
    }
}