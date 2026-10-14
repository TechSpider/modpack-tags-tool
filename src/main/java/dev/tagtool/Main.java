package dev.tagtool;

public class Main {

    public static void main(String[] args) {
        System.out.println("Welcome to the modpack tag auditor");

        if (args.length == 0) {
            System.out.println("Please run with proper arguments: TODO");
        }

        String modpackPath = args[0];

        System.out.println("Modpack Path: " + modpackPath);

    }

}
