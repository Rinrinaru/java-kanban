package tracker.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tracker.controllers.Managers;
import tracker.controllers.TaskManager;
import tracker.model.*;

import static org.junit.jupiter.api.Assertions.*;

class TaskManagerTest {
    private TaskManager manager;

    @BeforeEach
    void beforeEach() {
        manager = Managers.getDefault();
    }

    @Test
    void shouldDeleteTasks() {
        Task task = new Task("Удоли :D", "");
        manager.createTask(task);
        manager.deleteTask(task.getId());
        assertNull(manager.getTask(task.getId()));
    }

    @Test
    void shouldDeleteAllTasks() {
        manager.createTask(new Task("Таск 1", ""));
        manager.createTask(new Task("Таск 2", ""));
        manager.deleteAllTasks();
        assertTrue(manager.getAllTasks().isEmpty());
    }

    @Test
    void shouldNotAllowSubtaskWithoutEpic() {
        Subtask subtask = new Subtask("Сабтаск", "", 999);
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> manager.createSubtask(subtask),
                "Ожидалось исключение при создании подзадачи без эпика"
        );

        assertEquals("Epic with id 999 not found", exception.getMessage(),
                "Неверное сообщение об ошибке");
    }
}