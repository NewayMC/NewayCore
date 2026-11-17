package ru.newaymc.newaycore.files.database;

public enum ColumnType {
    INTEGER("INTEGER"),
    BIGINT("BIGINT"),
    VARCHAR("VARCHAR(255)"),
    TEXT("CLOB"),
    BOOLEAN("BOOLEAN"),
    DOUBLE("DOUBLE"),
    TIMESTAMP("TIMESTAMP");

    private final String sqlType;

    ColumnType(String sqlType) {
        this.sqlType = sqlType;
    }

    public String getSqlType() {
        return sqlType;
    }
}
