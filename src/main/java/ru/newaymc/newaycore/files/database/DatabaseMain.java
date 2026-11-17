package ru.newaymc.newaycore.files.database;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.newaymc.newaycore.NewaycoreMod;

import java.sql.*;
import java.util.*;

/**
 * H2 database support
 */
public class DatabaseMain implements AutoCloseable {
    private static final Logger LOGGER = LogManager.getLogger(NewaycoreMod.MODID + "/DatabaseMain");

    private final DatabaseConfig config;
    private final Connection connection;

    public DatabaseMain(DatabaseConfig config) throws SQLException {
        this.config = config;
        this.connection = DriverManager.getConnection(config.getJdbcUrl(), config.getUser(), config.getPassword());
        LOGGER.debug("Connection to database {} successful: {}", config.getName(), config.getJdbcUrl());
    }

    public String getName() {
        return config.getName();
    }

    public void createTable(String tableName, Column... columns) throws SQLException {
        if (columns == null || columns.length == 0) {
            throw new IllegalArgumentException("Columns are empty");
        }

        StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS ").append(tableName.toUpperCase()).append(" (");

        List<String> pkColumns = new ArrayList<>();
        for (int i = 0; i < columns.length; i++) {
            Column c = columns[i];
            sql.append(c.getName().toUpperCase()).append(" ").append(c.getType().getSqlType());

            if (c.isAutoIncrement()) {
                sql.append(" AUTO_INCREMENT");
            }

            if (!c.isNullable()) {
                sql.append(" NOT NULL");
            }

            if (c.isPrimaryKey() && !c.isAutoIncrement()) {
                pkColumns.add(c.getName().toUpperCase());
            }

            if (i < columns.length - 1){
                sql.append(", ");
            }
        }

        if (!pkColumns.isEmpty()) {
            sql.append(", PRIMARY KEY (");

            for (int i = 0; i < pkColumns.size(); i++) {
                sql.append(pkColumns.get(i));
                if (i < pkColumns.size() - 1) sql.append(", ");
            }
            sql.append(")");
        }

        sql.append(")");

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql.toString());
            LOGGER.debug("Table '{}' already exists in '{}' database", tableName, config.getName());
        }
    }

    public void dropTable(String tableName) throws SQLException {
        String sql = "DROP TABLE IF EXISTS " + tableName.toUpperCase();
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            LOGGER.debug("Table '{}' deleted from database '{}'", tableName, config.getName());
        }
    }

    public long insert(String tableName, Map<String, Object> values) throws SQLException {
        if (values.isEmpty()) {
            throw new IllegalArgumentException("Can't insert an empty line");
        }

        StringBuilder sql = new StringBuilder("INSERT INTO ").append(tableName.toUpperCase()).append(" (");
        StringJoiner cols = new StringJoiner(", ");
        StringJoiner placeholders = new StringJoiner(", ");
        List<Object> params = new ArrayList<>();

        for (Map.Entry<String, Object> e : values.entrySet()) {
            cols.add(e.getKey().toUpperCase());
            placeholders.add("?");
            params.add(e.getValue());
        }
        sql.append(cols).append(") VALUES (").append(placeholders).append(")");

        try (PreparedStatement ps = connection.prepareStatement(sql.toString(),
                Statement.RETURN_GENERATED_KEYS)) {
            setParameters(ps, params);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    LOGGER.debug("Вставлена строка в '{}', id={}", tableName, id);
                    return id;
                }
            }
            return -1;
        }
    }

    public int update(String tableName, Map<String, Object> values, String whereClause, Object... whereParams) throws SQLException {
        if (values.isEmpty()) {
            throw new IllegalArgumentException("Nothing to update");
        }

        StringBuilder sql = new StringBuilder("UPDATE ").append(tableName.toUpperCase()).append(" SET ");
        StringJoiner set = new StringJoiner(", ");
        List<Object> params = new ArrayList<>();

        for (Map.Entry<String, Object> e : values.entrySet()) {
            set.add(e.getKey().toUpperCase() + " = ?");
            params.add(e.getValue());
        }
        sql.append(set);
        if (whereClause != null && !whereClause.isBlank()) {
            sql.append(" WHERE ").append(whereClause.toUpperCase());
            if (whereParams != null) {
                Collections.addAll(params, whereParams);
            }
        }

        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            setParameters(ps, params);
            int count = ps.executeUpdate();
            return count;
        }
    }

    public int delete(String tableName, String whereClause, Object... whereParams) throws SQLException {
        StringBuilder sql = new StringBuilder("DELETE FROM ").append(tableName.toUpperCase());
        List<Object> params = new ArrayList<>();

        if (whereClause != null && !whereClause.isBlank()) {
            sql.append(" WHERE ").append(whereClause.toUpperCase());
            if (whereParams != null) {
                Collections.addAll(params, whereParams);
            }
        }

        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            setParameters(ps, params);
            int count = ps.executeUpdate();
            return count;
        }
    }

    public List<Map<String, Object>> select(String tableName, String whereClause, Object... whereParams) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM ").append(tableName.toUpperCase());
        List<Object> params = new ArrayList<>();

        if (whereClause != null && !whereClause.isBlank()) {
            sql.append(" WHERE ").append(whereClause.toUpperCase());
            if (whereParams != null) {
                Collections.addAll(params, whereParams);
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            setParameters(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData meta = rs.getMetaData();
                int cols = meta.getColumnCount();
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int i = 1; i <= cols; i++) {
                        row.put(meta.getColumnName(i), rs.getObject(i));
                    }
                    result.add(row);
                }
            }
        }

        return result;
    }

    public ResultSet executeQuery(String sql, Object... params) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(sql);
        setParameters(ps, Arrays.asList(params));
        return ps.executeQuery();
    }

    public int execute(String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            setParameters(ps, Arrays.asList(params));
            return ps.executeUpdate();
        }
    }

    public boolean tableExists(String tableName) throws SQLException {
        try (ResultSet rs = connection.getMetaData().getTables(null, null, tableName.toUpperCase(), null)) {
            return rs.next();
        }
    }

    @Override
    public void close() {
        if (connection != null) {
            try {
                connection.close();
                LOGGER.info("Connection with '{}' database is closed", config.getName());
            } catch (SQLException e) {
                LOGGER.error("Error with closing '{}' database", config.getName(), e);
            }
        }
    }

    private void setParameters(PreparedStatement ps, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            ps.setObject(i + 1, params.get(i));
        }
    }
}
