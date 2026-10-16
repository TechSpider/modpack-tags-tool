package dev.tagstool.model;

import java.nio.file.Path;

public record JarJsonPath(Path jarPath, String entryPath) {
}
