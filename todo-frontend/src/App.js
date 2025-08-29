import React, { useState, useEffect } from "react";
import "./App.css";

export default function App() {
  const [tasks, setTasks] = useState([]);
  const [newTask, setNewTask] = useState("");
  const [description, setDescription] = useState("");
  const [hoveredTask, setHoveredTask] = useState(null);
  const [tasksToDelete, setTasksToDelete] = useState([]);
  const [editTask, setEditTask] = useState(null);

  useEffect(() => {
    fetch("http://localhost:8080/api/tasks")
      .then(res => res.json())
      .then(setTasks)
      .catch(err => console.error("Failed to load tasks", err));
  }, []);

  const addTask = () => {
    if (!newTask.trim()) return;

    const payload = { title: newTask, description, status: "Not Started" };

    fetch("http://localhost:8080/api/tasks", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
    })
      .then(res => res.json())
      .then(task => setTasks(prev => [...prev, task]))
      .catch(err => console.error("Failed to add task", err));

    setNewTask("");
    setDescription("");
  };

  const toggleTaskSelection = (id) => {
    setTasksToDelete(prev =>
      prev.includes(id) ? prev.filter(tid => tid !== id) : [...prev, id]
    );
  };

  const deleteSelected = () => {
    tasksToDelete.forEach(id => {
      fetch(`http://localhost:8080/api/tasks/${id}`, { method: "DELETE" })
        .catch(err => console.error("Failed to delete task", err));
    });
    setTasks(prev => prev.filter(t => !tasksToDelete.includes(t.id)));
    setTasksToDelete([]);
  };

  const saveEdit = () => {
    fetch(`http://localhost:8080/api/tasks/${editTask.id}/edit`, {
      method: "PATCH",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        title: editTask.title,
        description: editTask.description,
        status: editTask.status,
      }),
    })
      .then(res => res.json())
      .then(updatedTask => {
        setTasks(prev =>
          prev.map(t => (t.id === updatedTask.id ? updatedTask : t))
        );
      })
      .catch(err => console.error("Failed to update task", err));

    setEditTask(null);
  };

  return (
    <div className="app">
      <h1 className="title">To-Do</h1>

      <div className="input-container">
        <input
          className="task-input"
          type="text"
          placeholder="New task title..."
          maxLength={50}
          value={newTask}
          onChange={e => setNewTask(e.target.value)}
        />
        <input
          className="task-input"
          type="text"
          placeholder="Description..."
          value={description}
          onChange={e => setDescription(e.target.value)}
        />
        <div className="button-row">
          <button className="add-btn" onClick={addTask}>Add</button>
          {tasksToDelete.length > 0 && (
            <button className="delete-btn" onClick={deleteSelected}>
              Delete ({tasksToDelete.length})
            </button>
          )}
        </div>
      </div>

      <ul className="task-list">
        {tasks.map(task => (
          <div
            key={task.id}
            className="task-wrapper"
            onMouseEnter={() => setHoveredTask(task)}
            onMouseLeave={() => setHoveredTask(null)}
          >
            <li className="task">
              <span className="title">{task.title}</span>
              <div className="state-container">
                <span
                  className={`state-text ${
                    task.status === "Done"
                      ? "state-done"
                      : task.status === "Working On"
                      ? "state-working"
                      : task.status === "Cancelled"
                      ? "state-cancelled"
                      : "state-not-started"
                  }`}
                >
                  {task.status}
                </span>
                <input
                  type="checkbox"
                  checked={tasksToDelete.includes(task.id)}
                  onChange={() => toggleTaskSelection(task.id)}
                />
                <button className="edit" onClick={() => setEditTask(task)}>✎</button>
              </div>
            </li>

            {hoveredTask?.id === task.id && (
              <div className="task-hover">
                <p><strong>Description:</strong> {task.description || "None"}</p>
                <p><strong>Created:</strong> {task.createdAt ? new Date(task.createdAt).toLocaleString() : "Unknown"}</p>
              </div>
            )}
          </div>
        ))}
      </ul>

      {editTask && (
        <div className="modal-overlay">
          <div className="modal">
            <h2>Edit Task</h2>
            <input
              className="task-input"
              type="text"
              value={editTask.title}
              onChange={e => setEditTask({ ...editTask, title: e.target.value })}
            />
            <input
              className="task-input"
              type="text"
              value={editTask.description}
              onChange={e => setEditTask({ ...editTask, description: e.target.value })}
            />
            <select
              className="task-input"
              value={editTask.status}
              onChange={e => setEditTask({ ...editTask, status: e.target.value })}
            >
              <option>Not Started</option>
              <option>Working On</option>
              <option>Done</option>
              <option>Cancelled</option>
            </select>
            <div className="button-row">
              <button className="add-btn" onClick={saveEdit}>Save</button>
              <button className="delete-btn" onClick={() => setEditTask(null)}>Cancel</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
