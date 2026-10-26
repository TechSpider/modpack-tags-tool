package dev.tagstool;

import dev.tagstool.discovery.ModpackDiscovery;
import dev.tagstool.model.JarJsonPath;
import dev.tagstool.model.TagDefinition;
import dev.tagstool.scanner.JarScanner;
import dev.tagstool.scanner.JsonScanner;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Slf4j
public class Main {

    public static void main(String[] args) {
        log.info("Welcome to the modpack tags auditor!");


        if (args.length == 0) {
            log.warn("Usage: java -jar modpack-tags-tool.jar \"<modpack-directory>\"");
            System.exit(1);
        }

        Path modpackPath = Path.of(args[0]);

        if (!Files.isDirectory(modpackPath)) {
            log.warn("Invalid modpack directory: {}", modpackPath);
            System.exit(1);
        }

        log.info("Modpack Path: {}", modpackPath);

        ModpackDiscovery discovery = new ModpackDiscovery();
        JarScanner jarScanner = new JarScanner();
        JsonScanner jsonScanner = new JsonScanner();

        List<Path> jarPaths = discovery.getJars(modpackPath);
        log.info("Found {} JAR files in mods directory", jarPaths.size());

        List<JarJsonPath> tagJsons = jarScanner.getTagJsons(jarPaths);
        log.info("Found {} tag-defining JSON files", tagJsons.size());

        List<TagDefinition> tagDefinitions = jsonScanner.parseJsons(tagJsons);
        log.info("Successfully parsed {} tag definitions", tagDefinitions.size());
    }

}
