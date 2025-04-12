package tracker.test.java;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tracker.controllers.Managers;
import tracker.controllers.TaskManager;
import tracker.model.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HistoryManagerTest {
    private TaskManager manager;

    @BeforeEach
    void setUp() {
        manager = Managers.getDefault();
    }

    @Test
    void shouldAddTasksToHistory() {
        Task task = new Task("Task", "");
        manager.createTask(task);
        manager.getTask(task.getId());
        assertEquals(1, manager.getHistory().size());
        assertEquals(task, manager.getHistory().get(0));
    }

    @Test
    void shouldLimitHistorySize() {
        for (int i = 0; i < 15; i++) {
            Task task = new Task("Task " + i, "");
            manager.createTask(task);
            manager.getTask(task.getId());
        }
        assertEquals(10, manager.getHistory().size());
    }

    @Test
    void shouldPreserveTaskStatesInHistory() {
        // Создаем и добавляем первоначальную задачу
        Task task = new Task("Original", "");
        manager.createTask(task);

        // Первое состояние - получаем задачу
        Task firstState = manager.getTask(task.getId());

        // Создаем КОПИЮ задачи с изменениями
        Task modifiedTask = new Task(firstState.getName(), firstState.getDescription());
        modifiedTask.setId(firstState.getId());
        modifiedTask.setStatus(Task.Status.IN_PROGRESS);
        modifiedTask.setName("Modified");

        // Обновляем задачу в менеджере
        manager.updateTask(modifiedTask);

        // Второе состояние - получаем измененную задачу
        Task secondState = manager.getTask(modifiedTask.getId());

        // Проверяем историю
        List<Task> history = manager.getHistory();
        assertEquals(2, history.size());
        assertNotEquals(history.get(0).getName(), history.get(1).getName());
    }

    @Test
    void shouldHandleDifferentTaskTypesInHistory() {
        // Создаем задачи разных типов
        Task task = new Task("Task", "");
        Epic epic = new Epic("Epic", "");
        manager.createTask(task);
        manager.createEpic(epic);

        // Получаем задачи, чтобы добавить в историю
        manager.getTask(task.getId());
        manager.getEpic(epic.getId());

        // Проверяем историю
        assertEquals(2, manager.getHistory().size());
        assertTrue(manager.getHistory().contains(task));
        assertTrue(manager.getHistory().contains(epic));
    }
}