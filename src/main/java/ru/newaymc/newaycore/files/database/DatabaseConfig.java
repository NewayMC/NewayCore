package ru.newaymc.newaycore.files.database;

import lombok.Getter;

import java.nio.file.Path;

@Getter
public class DatabaseConfig {
    private final String name;
    private final Path directory;
    private final String user;
    private final String password;

    private DatabaseConfig(Builder builder) {
        this.name = builder.name;
        this.directory = builder.directory;
        this.user = builder.user;
        this.password = builder.password;
    }

    /**
     * JDBC URL: jdbc:h2:file:PATH/NAME;AUTO_SERVER=TRUE
     */
    public String getJdbcUrl() {
        Path file = directory.resolve(name);
        return "jdbc:h2:file:" + file.toAbsolutePath() + ";AUTO_SERVER=TRUE;MODE=MySQL";
    }

    public static Builder builder(String name) {
        return new Builder(name);
    }

    public static class Builder {
        private final String name;
        private Path directory = Path.of(System.getProperty("user.home"), ".game_db");
        private String user = "sa";
        private String password = "";

        public Builder(String name) {
            this.name = name;
        }

        public Builder directory(Path directory) {
            this.directory = directory;
            return this;
        }

        public Builder credentials(String user, String password) {
            this.user = user;
            this.password = password;
            return this;
        }

        public DatabaseConfig build() {
            return new DatabaseConfig(this);
        }
    }
}
