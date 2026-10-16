package dev.tagstool.scanner;

import dev.tagstool.model.JarJsonPath;
import org.tomlj.Toml;
import org.tomlj.TomlArray;
import org.tomlj.TomlParseResult;
import org.tomlj.TomlTable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;


public class JarScanner {
    //Find .json files defining common 'c:' tags
    public List<JarJsonPath> getCommonDefJsons(List<Path> jars) {
        List<JarJsonPath> jsons = new ArrayList<>();

        try {
            jars.forEach(jarPath -> {
                try (JarFile jarFile = new JarFile(jarPath.toFile())) {
                    Enumeration<JarEntry> entries = jarFile.entries();

                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        String jsonPath = entry.getName();

                        if (!entry.isDirectory() && jsonPath.startsWith("data/c/tags/") && jsonPath.endsWith(".json")) {
                            jsons.add(new JarJsonPath(jarPath, jsonPath));
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Error accessing .jar file " + jarPath.getFileName() + ": " + e.getMessage());
                }
            });

            return jsons;
        } catch (Exception e) {
            System.out.println("Error searching jar files: " + e.getMessage());
            return jsons;
        }
    }


    //Find .json files defining each mod's own tags and tag usage
    public List<JarJsonPath> getModDefJsons(List<Path> jars) {
        List<JarJsonPath> jsons = new ArrayList<>();

        try {
            jars.forEach(jarPath -> {
                try (JarFile jarFile = new JarFile(jarPath.toFile())) {
                    String currentModId = null;
                    JarEntry metadataEntry = jarFile.getJarEntry("META-INF/neoforge.mods.toml");
                    Enumeration<JarEntry> entries = jarFile.entries();

                    if (metadataEntry == null) {
                        System.out.println("Missing NeoForge metadata file in " + jarFile.getName());
                        return;
                    }

                    //Find modId from metadata file
                    try (InputStream input = jarFile.getInputStream(metadataEntry);) {
                        TomlParseResult result = Toml.parse(input);
                        TomlArray modsSection = result.getArray("mods");

                        if (!result.errors().isEmpty()) {
                            System.out.println("Invalid TOML in " + jarFile.getName());
                            return;
                        }

                        if (modsSection == null) {
                            System.out.println("Missing or wrong [[mods]] section in " + jarFile.getName() + "metadata file");
                            return;
                        }

                        for (int i = 0; i < modsSection.size(); i++) {
                            TomlTable mod = modsSection.getTable(i);
                            currentModId = mod.getString("modId");
                        }
                    } catch (Exception e) {
                        System.out.println("Unknown error: " + e.getMessage());
                    }

                    //Find .json files within the mod's specific tag directory using its modId
                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        String jsonPath = entry.getName();

                        if (!entry.isDirectory() && jsonPath.startsWith("data/" + currentModId + "/tags/") && jsonPath.endsWith(".json")) {
                            jsons.add(new JarJsonPath(jarPath, jsonPath));
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Error accessing .jar file " + jarPath.getFileName() + ": " + e.getMessage());
                }
            });

            return jsons;
        } catch (Exception e) {
            System.out.println("Error searching jar files: " + e.getMessage());
            return jsons;
        }
    }
}
