package dev.tagstool.model;

import java.nio.file.Path;
import java.util.List;

public record TagDefinition(
        TagId id,
        Path sourcePath, //Can represent parent JAR archive or plain JSON file on disk depending on source
        String jsonPath, //Relevant when JSON is inside a JAR archive, null otherwise
        boolean replace,
        List<TagEntry> definitionTags
) {
}
