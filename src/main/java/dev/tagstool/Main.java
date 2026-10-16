package dev.tagstool;

import dev.tagstool.discovery.ModpackDiscovery;
import dev.tagstool.scanner.JarScanner;
import java.nio.file.Path;


public class Main {

    public static void main(String[] args) {
        System.out.println("Welcome to the modpack tags auditor!");

        if (args.length == 0) {
            System.out.println("Please run with proper arguments: TODO");
        }

        Path modpackPath = Path.of(args[0]);

        System.out.println("Modpack Path: " + modpackPath);

        ModpackDiscovery discovery = new ModpackDiscovery();
        JarScanner jarScanner = new JarScanner();

        System.out.println(jarScanner.getCommonDefJsons(discovery.getJars(modpackPath)).size());
        System.out.println(jarScanner.getModDefJsons(discovery.getJars(modpackPath)).size());
    }

}
