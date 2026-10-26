package dev.tagstool.scanner;

import dev.tagstool.model.JarJsonPath;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@Slf4j
public class JarScanner {
    //Find all tag-defining jsons
    public List<JarJsonPath> getTagJsons(List<Path> jars) {
        List<JarJsonPath> tagJsons = new ArrayList<>();

        jars.forEach(jarPath -> {
            try (JarFile jar = new JarFile(jarPath.toFile())) {
                Enumeration<JarEntry> entries = jar.entries();

                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String jsonPath = entry.getName();

                    if (!entry.isDirectory()
                            && jsonPath.startsWith("data/")
                            && jsonPath.contains("/tags/")
                            && jsonPath.endsWith(".json")) {

                        tagJsons.add(new JarJsonPath(jarPath, jsonPath));
                    }
                }
            } catch (IOException e) {
                log.error("Error accessing JAR file {}: {}", jarPath.getFileName(), e.getMessage(), e);
            }
        });

        return tagJsons;
    }
}

