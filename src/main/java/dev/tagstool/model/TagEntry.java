package dev.tagstool.model;

public record TagEntry(
        String value,
        boolean isTagReference,
        boolean isRequired
) {
}
