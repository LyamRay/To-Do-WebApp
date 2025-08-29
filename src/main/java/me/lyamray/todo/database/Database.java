package me.lyamray.todo.database;

import lombok.Getter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.*;

public class Database {

    @Getter
    private static Database instance;
    private Connection connection;

    private Database() {
        setupDatabase();
    }

    private void setupDatabase() {
        try {
            Path dataFolder = Path.of("data");
            Files.createDirectories(dataFolder);
            connection = DriverManager.getConnection("jdbc:sqlite:" + dataFolder.resolve("todo.db"));
            try (Statement statement = connection.createStatement()) {
                statement.execute("""
                        CREATE TABLE IF NOT EXISTS tasks (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            title TEXT NOT NULL,
                            description TEXT,
                            status TEXT DEFAULT 'Not Started',
                            createdAt INTEGER
                        );
                        """);
            }
        } catch (IOException | SQLException e) {
            throw new RuntimeException("Failed to setup database", e);
        }
    }


    public void add(String table, Map<String, Object> values) throws SQLException {
        String columns = String.join(", ", values.keySet());
        String placeholders = String.join(", ", Collections.nCopies(values.size(), "?"));
        executeUpdate("INSERT INTO " + table + " (" + columns + ") VALUES (" + placeholders + ")", values.values().toArray());
    }

    public List<Map<String, Object>> get(String table, String whereClause, Object... params) throws SQLException {
        return executeQuery(buildSelectSql(table, whereClause), params);
    }

    public void set(String table, Map<String, Object> updates, String whereClause, Object... params) throws SQLException {
        List<Object> allParams = new ArrayList<>(updates.values());
        allParams.addAll(Arrays.asList(params));
        executeUpdate(buildUpdateSql(table, updates.keySet(), whereClause), allParams.toArray());
    }

    public void remove(String table, String whereClause, Object... params) throws SQLException {
        executeUpdate(buildDeleteSql(table, whereClause), params);
    }

    public boolean checkIfExists(String table, String whereClause, Object... params) throws SQLException {
        String sql = buildSelectSql(table, whereClause) + " LIMIT 1";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            bindParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }



    private String buildSelectSql(String table, String where) {
        return "SELECT * FROM " + table + (where != null && !where.isEmpty() ? " WHERE " + where : "");
    }

    private String buildUpdateSql(String table, Set<String> columns, String where) {
        String setClause = String.join(", ", columns.stream().map(k -> k + " = ?").toList());
        return "UPDATE " + table + " SET " + setClause + (where != null && !where.isEmpty() ? " WHERE " + where : "");
    }

    private String buildDeleteSql(String table, String where) {
        return "DELETE FROM " + table + (where != null && !where.isEmpty() ? " WHERE " + where : "");
    }

    private void executeUpdate(String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            bindParams(ps, params);
            ps.executeUpdate();
        }
    }

    private List<Map<String, Object>> executeQuery(String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            bindParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                return mapResultSet(rs);
            }
        }
    }

    private void bindParams(PreparedStatement ps, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
    }

    private List<Map<String, Object>> mapResultSet(ResultSet rs) throws SQLException {
        List<Map<String, Object>> result = new ArrayList<>();
        ResultSetMetaData meta = rs.getMetaData();
        int columnCount = meta.getColumnCount();
        while (rs.next()) {
            Map<String, Object> row = new HashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                row.put(meta.getColumnName(i), rs.getObject(i));
            }
            result.add(row);
        }
        return result;
    }
}
