package me.lyamray.todo.service;

import me.lyamray.todo.model.Task;
import me.lyamray.todo.repository.TaskRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class TaskService {

    private final TaskRepository repo = new TaskRepository();

    public Task create(String title, String description, String status) throws SQLException {
        Task t = new Task();
        t.setTitle(title);
        t.setDescription(description);
        t.setStatus(status);
        return repo.save(t);
    }

    public List<Task> all() throws SQLException {
        return repo.findAll();
    }

    public void delete(Long id) throws SQLException {
        repo.deleteById(id);
    }

    public Task update(Task task) throws SQLException {
        return repo.save(task);
    }

    public Optional<Task> findById(Long id) throws SQLException {
        return repo.findById(id);
    }
}
