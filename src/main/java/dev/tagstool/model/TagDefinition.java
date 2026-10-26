package dev.tagstool.model;

import java.nio.file.Path;
import java.util.List;

public record TagDefinition(
        TagId id,
        Path sourceJar,
        String sourceJsonPath,
        boolean replace,
        List<TagEntry> definitionTags
) {
}
