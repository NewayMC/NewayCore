package ru.newaymc.newaycore.files;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.newaymc.newaycore.NewaycoreMod;

import java.io.*;
import java.nio.file.Files;
import java.util.Objects;
import java.util.Optional;

public class FileSerialization {

    public static File toFile(String name, String location) {
        return new File(location, File.separator + name);
    }

    public static class JavaSerialization {
        private static final Logger LOGGER = LogManager.getLogger(NewaycoreMod.MODID + "/JavaSerialization");

        private JavaSerialization() {

        }

        public static <T> boolean serialize(T obj, File file) {
            Objects.requireNonNull(obj, "Object to serialize cannot be null");
            Objects.requireNonNull(file, "File cannot be null");

            if (!ensureParentDirectories(file)) {
                return false;
            }

            try (ObjectOutputStream outputStream = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)))) {
                outputStream.writeObject(obj);
                outputStream.flush();
                return true;

            } catch (IOException e) {
                LOGGER.error("Serialization error for file {}: {}", file.getAbsolutePath(), e.getMessage(), e);

                return false;
            }
        }

        @SuppressWarnings("unchecked")
        public static <T> Optional<T> deserialize(File file) {
            Objects.requireNonNull(file, "File cannot be null");

            if (!file.exists() || !file.isFile()) {
                LOGGER.error("File does not exist or is not a file: {}", file.getAbsolutePath());

                return Optional.empty();
            }

            if (!file.canRead()) {
                LOGGER.error("Cannot read file: {}", file.getAbsolutePath());

                return Optional.empty();
            }

            try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)))) {
                T obj = (T) ois.readObject();
                return Optional.of(obj);

            } catch (IOException | ClassNotFoundException e) {
                LOGGER.error("Deserialization error for file {}: {}", file.getAbsolutePath(), e.getMessage(), e);
                return Optional.empty();
            }
        }

        private static boolean ensureParentDirectories(File file) {
            File parentDir = file.getParentFile();
            if (parentDir == null) {
                return true;
            }

            if (parentDir.exists()) {
                return true;
            }

            try {
                Files.createDirectories(parentDir.toPath());
                return true;

            } catch (IOException e) {
                LOGGER.error("Failed to create parent directories for: {} - {}", file.getAbsolutePath(), e.getMessage(), e);
                return false;
            }
        }
    }
}

