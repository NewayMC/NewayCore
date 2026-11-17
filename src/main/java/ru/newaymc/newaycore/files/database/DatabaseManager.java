package ru.newaymc.newaycore.files.database;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.newaymc.newaycore.NewaycoreMod;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DatabaseManager implements AutoCloseable {
    private static final Logger LOGGER = LogManager.getLogger(NewaycoreMod.MODID + "/DatabaseManager");

    private final Map<String, DatabaseMain> databases = new ConcurrentHashMap<>();

    public synchronized DatabaseMain openDatabase(DatabaseConfig config) throws SQLException {
        if (databases.containsKey(config.getName())) {
            return databases.get(config.getName());
        }

        try {
            Files.createDirectories(config.getDirectory());
        } catch (IOException e) {
            throw new SQLException("Failed to create the directory: " + config.getDirectory(), e);
        }

        DatabaseMain db = new DatabaseMain(config);
        databases.put(config.getName(), db);
        return db;
    }

    public DatabaseMain openDatabase(String name) throws SQLException {
        return openDatabase(DatabaseConfig.builder(name).build());
    }

    public DatabaseMain getDatabase(String name) {
        DatabaseMain db = databases.get(name);
        if (db == null) {
            throw new IllegalStateException("Database '" + name + "' not opened");
        }

        return db;
    }

    public synchronized void closeDatabase(String name) {
        DatabaseMain db = databases.remove(name);
        if (db != null) {
            db.close();
        }
    }

    public boolean deleteDatabase(String name, Path directory) throws IOException {
        if (databases.containsKey(name)) {
            throw new IllegalStateException("Close the database: '" + name + "'");
        }

        Path base = directory.resolve(name);
        boolean deleted = false;
        for (String ext : new String[]{".mv.db", ".trace.db", ".lock.db", ".pgp"}) {
            Path file = Path.of(base.toString() + ext);
            if (Files.deleteIfExists(file)) {
                LOGGER.info("Deleted file: {}", file);
                deleted = true;
            }
        }

        return deleted;
    }

    public boolean hasDatabase(String name) {
        return databases.containsKey(name);
    }

    @Override
    public void close() {
        databases.values().forEach(DatabaseMain::close);
        databases.clear();
    }
}
