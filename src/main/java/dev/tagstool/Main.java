package dev.tagstool;

import dev.tagstool.audit.TagsAuditor;
import dev.tagstool.discovery.ModpackDiscovery;
import dev.tagstool.model.TagAuditResult;
import dev.tagstool.model.TagJsonPath;
import dev.tagstool.model.TagDefinition;
import dev.tagstool.scanner.JarScanner;
import dev.tagstool.scanner.JsonScanner;
import lombok.extern.slf4j.Slf4j;

import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.List;

@Slf4j
public class Main {

    public static void main(String[] args) {
        log.info("Welcome to the modpack tags auditor!");


        if (args.length != 1) {
            log.error("Usage: java -jar modpack-tags-tool.jar \"<modpack-directory>\"");
            System.exit(1);
        }

        Path modpackPath;

        try {
            modpackPath = Path.of(args[0]);
        } catch (InvalidPathException e) {
            log.error("Invalid path supplied: {}", args[0], e);
            System.exit(1);
            return;
        }

        if (!Files.isDirectory(modpackPath)) {
            log.error("Invalid modpack directory: {}", modpackPath);
            System.exit(1);
            return;
        }

        log.info("Modpack Path: {}", modpackPath);

        List<Path> jarPaths;
        List<TagJsonPath> modTagJsons;
        List<TagJsonPath> kubejsTagJsons;
        List<TagDefinition> tagDefinitions;
        List<TagAuditResult> tagAuditResults;

        try {
            ModpackDiscovery discovery = new ModpackDiscovery();
            JarScanner jarScanner = new JarScanner();
            JsonScanner jsonScanner = new JsonScanner();
            TagsAuditor tagsAuditor = new TagsAuditor();

            log.info("Searching mods directory for JAR files...");
            jarPaths = discovery.getJars(modpackPath);
            log.info("Found {} JAR files in mods directory", jarPaths.size());

            log.info("Searching directories and archives for JSON files...");
            modTagJsons = jarScanner.getTagJsons(jarPaths);
            kubejsTagJsons = discovery.getKubeJSTagJsons(modpackPath);
            log.info("Found {} tag-defining JSON files", modTagJsons.size() + kubejsTagJsons.size());

            log.info("Attempting to parse tag definitions from JSON files...");
            tagDefinitions = jsonScanner.parseJsons(modTagJsons);
            tagDefinitions.addAll(jsonScanner.parseJsons(kubejsTagJsons));
            log.info("Successfully parsed {} tag definitions", tagDefinitions.size());
            log.info("Checking tag definitions for errors...");
            tagAuditResults = tagsAuditor.audit(tagDefinitions);

        } catch (UncheckedIOException e) {
            log.error("Error analyzing modpack: {}", e.getMessage(), e);
            System.exit(1);
            return;
        }



    }
}
