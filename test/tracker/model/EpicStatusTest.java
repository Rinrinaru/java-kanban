package tracker.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tracker.controllers.HistoryManager;
import tracker.controllers.InMemoryHistoryManager;
import tracker.controllers.InMemoryTaskManager;
import tracker.model.Task.Status;

import static org.junit.jupiter.api.Assertions.*;

class EpicStatusTest {
    private InMemoryTaskManager taskManager;
    private HistoryManager historyManager;

    @BeforeEach
    public void setUp() {
        historyManager = new InMemoryHistoryManager();
        taskManager = new InMemoryTaskManager(historyManager);
    }

    @Test
    public void testEpicStatusAllNew() {
        Epic epic = new Epic("Epic", "Description");
        taskManager.createEpic(epic);

        Subtask subtask1 = new Subtask("Sub1", "Desc", epic.getId());
        Subtask subtask2 = new Subtask("Sub2", "Desc", epic.getId());
        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);

        assertEquals(Status.NEW, epic.getStatus(), "Все подзадачи NEW - эпик должен быть NEW");
    }

    @Test
    public void testEpicStatusAllDone() {
        Epic epic = new Epic("Epic", "Description");
        taskManager.createEpic(epic);

        Subtask subtask1 = new Subtask("Sub1", "Desc", epic.getId());
        Subtask subtask2 = new Subtask("Sub2", "Desc", epic.getId());
        subtask1.setStatus(Status.DONE);
        subtask2.setStatus(Status.DONE);
        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);

        assertEquals(Status.DONE, epic.getStatus(), "Все подзадачи DONE - эпик должен быть DONE");
    }

    @Test
    public void testEpicStatusNewAndDone() {
        Epic epic = new Epic("Epic", "Description");
        taskManager.createEpic(epic);

        Subtask subtask1 = new Subtask("Sub1", "Desc", epic.getId());
        Subtask subtask2 = new Subtask("Sub2", "Desc", epic.getId());
        subtask1.setStatus(Status.NEW);
        subtask2.setStatus(Status.DONE);
        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);

        assertEquals(Status.IN_PROGRESS, epic.getStatus(), "Подзадачи NEW и DONE - эпик должен быть IN_PROGRESS");
    }

    @Test
    public void testEpicStatusInProgress() {
        Epic epic = new Epic("Epic", "Description");
        taskManager.createEpic(epic);

        Subtask subtask1 = new Subtask("Sub1", "Desc", epic.getId());
        Subtask subtask2 = new Subtask("Sub2", "Desc", epic.getId());
        subtask1.setStatus(Status.IN_PROGRESS);
        subtask2.setStatus(Status.IN_PROGRESS);
        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);

        assertEquals(Status.IN_PROGRESS, epic.getStatus(), "Подзадачи IN_PROGRESS - эпик должен быть IN_PROGRESS");
    }

    @Test
    public void testEpicStatusAutoUpdate() {
        Epic epic = new Epic("Epic", "Description");
        taskManager.createEpic(epic);

        Subtask subtask = new Subtask("Sub", "Desc", epic.getId());
        taskManager.createSubtask(subtask);

        subtask.setStatus(Status.DONE);
        taskManager.updateSubtask(subtask);

        assertEquals(Status.DONE, epic.getStatus());
    }
}