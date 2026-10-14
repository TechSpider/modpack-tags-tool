package dev.tagstool;

import dev.tagstool.discovery.ModpackDiscovery;

import java.nio.file.Path;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        System.out.println("Welcome to the modpack tags auditor!");

        if (args.length == 0) {
            System.out.println("Please run with proper arguments: TODO");
        }

        Path modpackPath = Path.of(args[0]);

        System.out.println("Modpack Path: " + modpackPath);

        ModpackDiscovery discovery = new ModpackDiscovery();
        List<Path> jars = discovery.getJars(modpackPath);
    }

}
