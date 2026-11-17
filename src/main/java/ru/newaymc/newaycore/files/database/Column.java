package ru.newaymc.newaycore.files.database;

import lombok.Getter;

@Getter
public class Column {
    private final String name;
    private final ColumnType type;
    private final boolean primaryKey;
    private final boolean autoIncrement;
    private final boolean nullable;

    private Column(Builder builder) {
        this.name = builder.name;
        this.type = builder.type;
        this.primaryKey = builder.primaryKey;
        this.autoIncrement = builder.autoIncrement;
        this.nullable = builder.nullable;
    }

    public static Builder builder(String name, ColumnType type) {
        return new Builder(name, type);
    }

    public static class Builder {
        private final String name;
        private final ColumnType type;
        private boolean primaryKey = false;
        private boolean autoIncrement = false;
        private boolean nullable = true;

        public Builder(String name, ColumnType type) {
            this.name = name;
            this.type = type;
        }

        public Builder primaryKey() {
            this.primaryKey = true;
            this.nullable = false;
            return this;
        }

        public Builder autoIncrement() {
            this.autoIncrement = true;
            this.primaryKey = true;
            this.nullable = false;
            return this;
        }

        public Builder notNull() {
            this.nullable = false;
            return this;
        }

        public Column build() {
            return new Column(this);
        }
    }
}
