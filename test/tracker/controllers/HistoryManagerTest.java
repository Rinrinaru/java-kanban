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