package tracker.test.java;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tracker.controllers.Managers;
import tracker.controllers.TaskManager;
import tracker.model.*;
import static org.junit.jupiter.api.Assertions.*;

class TaskManagerTest {
    private TaskManager manager;

    @BeforeEach
    void setUp() {
        manager = Managers.getDefault();
    }

    @Test
    void shouldCreateAndFindTasks() {
        Task task = new Task("Task", "Description");
        manager.createTask(task);
        assertEquals(task, manager.getTask(task.getId()));
    }

    @Test
    void shouldCreateAndFindEpics() {
        Epic epic = new Epic("Epic", "Description");
        manager.createEpic(epic);
        assertEquals(epic, manager.getEpic(epic.getId()));
    }

    @Test
    void shouldCreateAndFindSubtasks() {
        Epic epic = new Epic("Epic", "");
        manager.createEpic(epic);
        Subtask subtask = new Subtask("Subtask", "", epic.getId());
        manager.createSubtask(subtask);
        assertEquals(subtask, manager.getSubtask(subtask.getId()));
    }

    @Test
    void shouldUpdateTask() {
        Task task = new Task("Original", "Desc");
        manager.createTask(task);

        Task updated = new Task("Updated", "New desc");
        updated.setId(task.getId());
        manager.updateTask(updated);

        assertEquals("Updated", manager.getTask(task.getId()).getName());
    }

    @Test
    void shouldDeleteTasks() {
        Task task = new Task("To delete", "");
        manager.createTask(task);
        manager.deleteTask(task.getId());
        assertNull(manager.getTask(task.getId()));
    }

    @Test
    void shouldDeleteAllTasks() {
        manager.createTask(new Task("Task 1", ""));
        manager.createTask(new Task("Task 2", ""));
        manager.deleteAllTasks();
        assertTrue(manager.getAllTasks().isEmpty());
    }

    @Test
    void shouldNotAllowSubtaskWithoutEpic() {
        Subtask subtask = new Subtask("Sub", "", 999); // Несуществующий эпик
        assertThrows(IllegalArgumentException.class, () -> manager.createSubtask(subtask));
    }
}