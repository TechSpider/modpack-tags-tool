package dev.tagstool.scanner;

import dev.tagstool.model.JarJsonPath;
import dev.tagstool.model.TagDefinition;
import dev.tagstool.model.TagEntry;
import dev.tagstool.model.TagId;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;

public class JsonScanner {
    private final ObjectMapper mapper = new ObjectMapper();

    public List<TagDefinition> parseJsons(List<JarJsonPath> paths) {
        List<TagDefinition> definitions = new ArrayList<>();

        paths.forEach(path -> {
            try (
                    JarFile jarFile = new JarFile(path.jarPath().toFile());
                    InputStream input = jarFile.getInputStream(jarFile.getJarEntry(path.jsonPath()))
            ) {
                String jsonPath = path.jsonPath();
                Path jarPath = path.jarPath();

                JsonNode root = mapper.readTree(input);

                JsonNode values = root.get("values");
                JsonNode replaceNode = root.path("replace");

                TagId tagId;
                List<TagEntry> tagEntries = new ArrayList<>();

                if (values == null || !values.isArray()) {
                    System.out.println("Missing or invalid values array in " + jarPath.getFileName() + " " + path.jsonPath());
                    return;
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
                    int typeIndex = getIndex(jsonPath, "worldgen"); //Add length of "worldgen" plus 1 for the separating "/"
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

                definitions.add(new TagDefinition(tagId, jarPath, jsonPath, replaceNode.asBoolean(false), tagEntries));

            } catch (IOException | StringIndexOutOfBoundsException | IllegalArgumentException e) {
                System.out.println("Error reading jar file " + path.jarPath().getFileName() + ": " + e.getMessage());
            }
        });

        return definitions;
    }

    //Find index of directory following 'str' string within 'jsonPath' path
    private int getIndex(String jsonPath, String str) {
        if (!jsonPath.contains(str)) {
            throw new IllegalArgumentException("Path does not contain string: " + "\"" + str + "\"");
        } else {
            return jsonPath.indexOf(str) + str.length() + 1; //Add 1 to skip over separating '/'
        }

    }
}
