package me.lyamray.todo.repository;

import me.lyamray.todo.database.Database;
import me.lyamray.todo.model.Task;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class TaskRepository {

    private final Database db = Database.getInstance();

    public Task save(Task task) throws SQLException {
        Map<String, Object> values = new HashMap<>();
        values.put("title", task.getTitle());
        values.put("description", task.getDescription());
        values.put("status", task.getStatus());
        values.put("createdAt", task.getCreatedAt().toEpochSecond(java.time.ZoneOffset.UTC));

        if (task.getId() == null) {
            db.add("tasks", values);

            // Get the last inserted id
            List<Map<String, Object>> lastRow = db.get("tasks", "rowid = (SELECT MAX(rowid) FROM tasks)");
            if (!lastRow.isEmpty()) {
                task.setId(((Number) lastRow.get(0).get("id")).longValue());
            }
        } else {
            db.set("tasks", values, "id = ?", task.getId());
        }

        return task;
    }

    public List<Task> findAll() throws SQLException {
        List<Map<String, Object>> rows = db.get("tasks", null);
        return rows.stream().map(this::mapRow).collect(Collectors.toList());
    }

    public Optional<Task> findById(Long id) throws SQLException {
        List<Map<String, Object>> rows = db.get("tasks", "id = ?", id);
        if (rows.isEmpty()) return Optional.empty();
        return Optional.of(mapRow(rows.get(0)));
    }

    public void deleteById(Long id) throws SQLException {
        db.remove("tasks", "id = ?", id);
    }

    private Task mapRow(Map<String, Object> row) {
        Task t = new Task();
        t.setId(((Number) row.get("id")).longValue());
        t.setTitle((String) row.get("title"));
        t.setDescription((String) row.get("description"));
        t.setStatus((String) row.get("status"));
        Object createdAtObj = row.get("createdAt");
        if (createdAtObj != null) {
            t.setCreatedAt(LocalDateTime.ofEpochSecond(((Number) createdAtObj).longValue(), 0, java.time.ZoneOffset.UTC));
        } else {
            t.setCreatedAt(LocalDateTime.now());
        }
        return t;
    }
}
