package dev.tagstool.scanner;

import dev.tagstool.model.TagJsonPath;
import dev.tagstool.model.TagDefinition;
import dev.tagstool.model.TagEntry;
import dev.tagstool.model.TagId;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;

@Slf4j
public class JsonScanner {
    private final ObjectMapper mapper = new ObjectMapper();

    public TagDefinition parseJson(TagJsonPath path) {
        String sourceDescription = null;
        try {
            String uniformSourcePath = path.sourcePath().toString().replace('\\', '/');
            String jsonPath = path.jsonPath() == null ? uniformSourcePath : path.jsonPath();
            sourceDescription = path.jsonPath() == null ? uniformSourcePath : path.sourcePath().getFileName() + "!" + path.jsonPath();

            JsonNode root = getRootNode(path);

            JsonNode values = root.get("values");
            JsonNode replaceNode = root.path("replace");

            TagId tagId;
            List<TagEntry> tagEntries = new ArrayList<>();

            if (values == null || !values.isArray()) {
                log.warn("Missing or invalid values array in {}", sourceDescription);
                return null;
            }

            for (JsonNode value : values) {
                if (value.isString()) {
                    String id = value.asString();
                    boolean isTagReference = id.startsWith("#");

                    tagEntries.add(new TagEntry(id, isTagReference, true));
                } else if (value.isObject()) {
                    JsonNode idNode = value.get("id");
                    JsonNode requiredNode = value.path("required");

                    String id = idNode.asString();
                    boolean isTagReference = id.startsWith("#");
                    boolean isRequired = requiredNode.asBoolean(true);

                    tagEntries.add(new TagEntry(id, isTagReference, isRequired));
                }
            }

            if (jsonPath.contains("tags/worldgen")) {
                int typeIndex = getIndex(jsonPath, "worldgen");
                int namespaceIndex = getIndex(jsonPath, "data");
                String namespace = jsonPath.substring(namespaceIndex, jsonPath.indexOf("/", namespaceIndex));
                String tagType = jsonPath.substring(typeIndex, jsonPath.indexOf("/", typeIndex));

                int pathIndex = getIndex(jsonPath, tagType);
                String tagPath = jsonPath.substring(pathIndex, jsonPath.indexOf(".json"));

                tagId = new TagId(namespace, tagPath, tagType);
            } else {
                int typeIndex = getIndex(jsonPath, "tags");
                int namespaceIndex = getIndex(jsonPath, "data");
                String namespace = jsonPath.substring(namespaceIndex, jsonPath.indexOf("/", namespaceIndex));
                String tagType = jsonPath.substring(typeIndex, jsonPath.indexOf("/", typeIndex));

                int pathIndex = getIndex(jsonPath, tagType);
                String tagPath = jsonPath.substring(pathIndex, jsonPath.indexOf(".json"));

                tagId = new TagId(namespace, tagPath, tagType);
            }

            return new TagDefinition(tagId, path.sourcePath(), path.jsonPath(), replaceNode.asBoolean(false), tagEntries);
        } catch (IOException | StringIndexOutOfBoundsException | IllegalArgumentException e) {
            log.error("Error reading JSON from {}: {}", sourceDescription, e.getMessage(), e);
            return null;
        }
    }

    public List<TagDefinition> parseJsons(List<TagJsonPath> paths) {
        List<TagDefinition> definitions = new ArrayList<>();

        paths.forEach(path -> {
            TagDefinition tagDefinition = parseJson(path);

            if (tagDefinition != null) {
                definitions.add(tagDefinition);
            }
        });

        return definitions;
    }

    private JsonNode getRootNode(TagJsonPath path) throws IOException {
        if (path.jsonPath() == null) {
            try (InputStream input = Files.newInputStream(path.sourcePath())) {
                return mapper.readTree(input);
            }
        } else {
            try (JarFile jarFile = new JarFile(path.sourcePath().toFile()); InputStream input = jarFile.getInputStream(jarFile.getJarEntry(path.jsonPath()))) {
                return mapper.readTree(input);
            }
        }
    }

    //Find index of directory following 'str' string within 'jsonPath' path
    private int getIndex(String jsonPath, String str) {
        if (!jsonPath.contains(str)) {
            throw new IllegalArgumentException("Path does not contain string: " + "\"" + str + "\"");
        } else {
            return jsonPath.indexOf(str) + str.length() + 1; //Add 1 to skip over separating character between directories
        }

    }
}
