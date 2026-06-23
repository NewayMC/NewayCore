package ru.newaymc.newaycore.network;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.io.*;
import java.util.Objects;

public class DataSerializer {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static <T> boolean serialization(T obj, File file) {
        Objects.requireNonNull(obj, "Object to serialization cannot be null");
        Objects.requireNonNull(file, "File cannot be null");

        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            boolean created = parentDir.mkdirs();
            if (!created) {
                LOGGER.error("Failed to create parent directories for: {}", file.getAbsolutePath());
                return false;
            }
        }

        try (ObjectOutputStream outputStream = new ObjectOutputStream(
                new BufferedOutputStream(new FileOutputStream(file)))) {
            outputStream.writeObject(obj);
            outputStream.flush();
            return true;
        } catch (IOException e) {
            LOGGER.error("Serialization error for file {}: {}", file.getAbsolutePath(), e.getMessage(), e);
            return false;
        }
    }

    public static Object deserialization(File file) throws IOException {
        Object obj = null;
        try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(file))) {
            obj = inputStream.readObject();
        } catch (ClassNotFoundException e) {
            LOGGER.error("Deserialization error {}", e.toString());
        }
        return obj;
    }
}
