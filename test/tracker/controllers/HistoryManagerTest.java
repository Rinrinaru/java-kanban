package tracker.controllers;

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
    void beforeEach() {
        manager = Managers.getDefault();
    }

    @Test
    void shouldPreserveTaskStatesInHistory() {
        Task task = new Task("Неповторимый оригинал", "");
        manager.createTask(task);

        Task firstState = manager.getTask(task.getId());

        Task modifiedTask = new Task(firstState.getName(), firstState.getDescription());
        modifiedTask.setId(firstState.getId());
        modifiedTask.setStatus(Task.Status.IN_PROGRESS);
        modifiedTask.setName("Жалкая пародия");

        manager.updateTask(modifiedTask);

        Task secondState = manager.getTask(modifiedTask.getId());

        List<Task> history = manager.getHistory();
        assertEquals(2, history.size());
        assertNotEquals(history.get(0).getName(), history.get(1).getName());
    }

    @Test
    void shouldHandleDifferentTaskTypesInHistory() {

        Task task = new Task("Таск", "");
        Epic epic = new Epic("Эпик", "");
        manager.createTask(task);
        manager.createEpic(epic);

        manager.getTask(task.getId());
        manager.getEpic(epic.getId());

        assertEquals(2, manager.getHistory().size());
        assertTrue(manager.getHistory().contains(task));
        assertTrue(manager.getHistory().contains(epic));
    }
}