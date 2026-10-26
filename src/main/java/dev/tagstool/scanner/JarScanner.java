package dev.tagstool.scanner;

import dev.tagstool.model.JarJsonPath;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


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
                System.out.println("Error accessing .jar file " + jarPath.getFileName() + ": " + e.getMessage());
            }
        });

        return tagJsons;
    }
}

