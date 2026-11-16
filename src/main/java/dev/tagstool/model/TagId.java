package dev.tagstool.model;

public record TagId(
        String namespace,
        String path,
        String type
) {
    @Override
    public String toString() {
        return namespace + ":" + path + " [" + type + "]";
    }
}
