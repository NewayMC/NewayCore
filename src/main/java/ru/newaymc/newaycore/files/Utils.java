package ru.newaymc.newaycore.files;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class Utils {

    public static File toFile(String name, String location) {
        return new File(location, File.separator + name);
    }

    public static long getFolderSize(File folder) {
        long size = 0;

        if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        size += getFolderSize(file); // Рекурсия
                    } else {
                        size += file.length();
                    }
                }
            }
        } else {
            size = folder.length();
        }

        return size;
    }

    public static long getFolderSize(Path folder) throws IOException {
        if (!Files.isDirectory(folder)) {
            return Files.size(folder);
        }

        long size = 0;
        try (Stream<Path> walk = Files.walk(folder)) {
            size = walk.filter(Files::isRegularFile).mapToLong(p -> {
                        try {
                            return Files.size(p);
                        } catch (IOException e) {
                            return 0;
                        }
                    }).sum();
        }
        return size;
    }
}
