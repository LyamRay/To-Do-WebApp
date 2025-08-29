package me.lyamray.todo.controller;

import me.lyamray.todo.model.Task;
import me.lyamray.todo.service.TaskService;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "http://localhost:3000") //using ngrok you can create a free link so that users can acces it not only from localhost, place that also here!
public class TaskController {

    private final TaskService service = new TaskService();

    @GetMapping
    public List<Task> all() throws SQLException {
        return service.all();
    }

    @PostMapping
    public Task create(@RequestBody Task task) throws SQLException {
        return service.create(task.getTitle(), task.getDescription(), task.getStatus());
    }

    @PatchMapping("/{id}/edit")
    public Task edit(@PathVariable Long id, @RequestBody Task task) throws SQLException {
        task.setId(id);
        return service.update(task);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) throws SQLException {
        service.delete(id);
    }
}
