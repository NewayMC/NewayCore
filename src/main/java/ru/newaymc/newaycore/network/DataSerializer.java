package ru.newaymc.newaycore.network;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.io.*;

public class DataSerializer {
    private static Logger LOGGER = LogUtils.getLogger();

    public static void serialization(Object obj, File file) {
        try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(file))) {
            outputStream.writeObject(obj);
        } catch (IOException e) {
            LOGGER.error("Serialization error {}", e.toString());
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

    public static File createFile() {
        return null;
    }
}
