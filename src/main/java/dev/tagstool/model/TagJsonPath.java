package dev.tagstool.model;

import java.nio.file.Path;

public record TagJsonPath(
        Path sourcePath,
        String jsonPath //Relevant when JSON is inside a JAR archive, null otherwise
) {}
