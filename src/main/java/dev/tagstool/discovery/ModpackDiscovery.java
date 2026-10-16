package dev.tagstool.discovery;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.stream.Stream;


public class ModpackDiscovery {


    public List<Path> getJars(Path modpackPath) {
        BiPredicate<Path, BasicFileAttributes> jarMatcher =
                (path, attributes) ->
                        attributes.isRegularFile()
                                && path.toString().endsWith(".jar");

        try (Stream<Path> paths = Files.find(modpackPath, Integer.MAX_VALUE, jarMatcher)) {
            return paths.toList();
        } catch (IOException e) {
            System.out.println("Error finding .jar files: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

}
