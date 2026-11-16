package dev.tagstool.discovery;

import dev.tagstool.model.TagJsonPath;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

@Slf4j
public class ModpackDiscovery {
    public List<Path> getJars(Path modpackPath) {
        BiPredicate<Path, BasicFileAttributes> jarMatcher =
                (path, attributes) ->
                        attributes.isRegularFile()
                                && (path.toString().replace('\\', '/').contains("/mods/"))
                                && path.toString().endsWith(".jar");

        try (Stream<Path> paths = Files.find(modpackPath, Integer.MAX_VALUE, jarMatcher)) {
            return paths.toList();
        } catch (IOException e) {
            throw new UncheckedIOException("Couldn't find JAR files in given directory", e);
        }
    }


    public List<TagJsonPath> getKubeJSTagJsons(Path modpackPath) {
        BiPredicate<Path, BasicFileAttributes> kubejsJsonMatcher =
                (path, attributes) ->
                        attributes.isRegularFile()
                                && (path.toString().replace('\\', '/').contains("/kubejs/data/"))
                                && (path.toString().replace('\\', '/').contains("/tags/"))
                                && path.toString().endsWith(".json");

        try (Stream<Path> paths = Files.find(modpackPath, Integer.MAX_VALUE, kubejsJsonMatcher)) {
            List<TagJsonPath> tagJsonPaths = new ArrayList<>();

            paths.forEach(path -> tagJsonPaths.add(new TagJsonPath(path, null)));

            return tagJsonPaths;
        } catch (IOException e) {
            throw new UncheckedIOException("Couldn't find KubeJS tag-defining JSON files in given directory", e);
        }
    }
}

