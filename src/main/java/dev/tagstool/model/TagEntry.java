package dev.tagstool.model;

public record TagEntry(
        String id,
        boolean isTagReference,
        boolean isRequired
) {
}
