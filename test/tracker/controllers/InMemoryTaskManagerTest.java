package tracker.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tracker.model.Epic;
import tracker.model.Subtask;
import tracker.model.Task;
import tracker.model.Task.Status;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {
    private InMemoryTaskManager manager;
    private HistoryManager historyManager;

    @BeforeEach
    void beforeEach() {
        historyManager = new InMemoryHistoryManager();
        manager = new InMemoryTaskManager(historyManager);
    }

    @Test
    void createAndGetTask() {
        Task task = new Task("Тест Таск", "Описание");
        manager.createTask(task);

        Task savedTask = manager.getTask(task.getId());

        assertNotNull(savedTask, "Задача должна сохраняться");
        assertEquals(task, savedTask, "Задачи должны быть одинаковыми");
    }

    @Test
    void createAndGetEpic() {
        Epic epic = new Epic("Тест Эпик", "Описание очень важное");
        manager.createEpic(epic);

        Epic savedEpic = manager.getEpic(epic.getId());

        assertNotNull(savedEpic, "Эпик должен сохраняться");
        assertEquals(Status.NEW, savedEpic.getStatus(), "Новый эпик должен иметь статус NEW");
    }

    @Test
    void createAndGetSubtask() {
        Epic epic = new Epic("Тест Эпик", "Описание очень важное");
        manager.createEpic(epic);

        Subtask subtask = new Subtask("Тест Сабтаск", "Описание", epic.getId());
        manager.createSubtask(subtask);

        Subtask savedSubtask = manager.getSubtask(subtask.getId());

        assertNotNull(savedSubtask, "Подзадача должна сохраняться");
        assertEquals(epic.getId(), savedSubtask.getEpicId(), "ID эпика должен совпадать");
    }

    @Test
    void updateTaskStatus() {
        Task task = new Task("Тест Таск", "Описание");
        manager.createTask(task);

        task.setStatus(Status.IN_PROGRESS);
        manager.updateTask(task);

        assertEquals(Status.IN_PROGRESS, manager.getTask(task.getId()).getStatus(), "Статус должен обновляться");
    }

    @Test
    void updateEpicStatus() {
        Epic epic = new Epic("Тест Эпик", "Описание очень важное");
        manager.createEpic(epic);

        Subtask subtask1 = new Subtask("Тест Сабтаск1", "Описание", epic.getId());
        Subtask subtask2 = new Subtask("Тест Сабтаск2", "Описание", epic.getId());
        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);

        assertEquals(Status.NEW, manager.getEpic(epic.getId()).getStatus());

        subtask1.setStatus(Status.DONE);
        manager.updateSubtask(subtask1);
        manager.updateEpicStatus(epic.getId());
        assertEquals(Status.IN_PROGRESS, manager.getEpic(epic.getId()).getStatus());

        subtask2.setStatus(Status.DONE);
        manager.updateSubtask(subtask2);
        assertEquals(Status.DONE, manager.getEpic(epic.getId()).getStatus());
    }

    @Test
    void deleteTasks() {
        Task task = new Task("Тест Таск", "Описание");
        manager.createTask(task);

        manager.deleteTask(task.getId());

        assertNull(manager.getTask(task.getId()), "Задача должна удаляться");
        assertTrue(manager.getAllTasks().isEmpty(), "Список задач должен быть пустым");
    }

    @Test
    void deleteEpicWithSubtasks() {
        Epic epic = new Epic("Тест Эпик", "Описание очень важное");
        manager.createEpic(epic);

        Subtask subtask = new Subtask("Test Subtask", "Description", epic.getId());
        manager.createSubtask(subtask);

        manager.deleteEpic(epic.getId());

        assertNull(manager.getEpic(epic.getId()), "Эпик должен удаляться");
        assertNull(manager.getSubtask(subtask.getId()), "Подзадачи должны удаляться");
    }

    @Test
    void getAllTasks() {
        Task task1 = new Task("Тест Таск1", "Описание");
        Epic task2 = new Epic("Тест Таск2", "Описание очень важное");
        manager.createTask(task1);
        manager.createTask(task2);

        List<Task> tasks = manager.getAllTasks();

        assertEquals(2, tasks.size(), "Должны возвращаться все задачи");
        assertTrue(tasks.contains(task1) && tasks.contains(task2), "Список должен содержать все задачи");
    }

    @Test
    void getHistory() {
        Task task = new Task("Тест Таск", "Описание");
        Epic epic = new Epic("Тест Эпик", "Описание очень важное");
        manager.createTask(task);
        manager.createEpic(epic);

        manager.getTask(task.getId());
        manager.getEpic(epic.getId());

        List<Task> history = manager.getHistory();

        assertEquals(2, history.size(), "История должна содержать 2 элемента");
        assertTrue(history.contains(task) && history.contains(epic), "История должна содержать просмотренные задачи");
    }

    @Test
    void shouldNotAddNullTasks() {
        manager.createTask(null);
        manager.createEpic(null);
        manager.createSubtask(null);

        assertTrue(manager.getAllTasks().isEmpty(), "Не должна добавляться null задача");
        assertTrue(manager.getAllEpics().isEmpty(), "Не должен добавляться null эпик");
        assertTrue(manager.getAllSubtasks().isEmpty(), "Не должна добавляться null подзадача");
    }


    @Test
    void shouldRemoveSubtaskIdFromEpicWhenSubtaskDeleted() {
        Epic epic = new Epic("Epic", "");
        manager.createEpic(epic);

        Subtask subtask = new Subtask("Subtask", "", epic.getId());
        manager.createSubtask(subtask);

        // Проверяем, что эпик содержит подзадачу
        assertTrue(epic.getSubtaskIds().contains(subtask.getId()));

        manager.deleteSubtask(subtask.getId());

        assertFalse(epic.getSubtaskIds().contains(subtask.getId()));
    }

    @Test
    void shouldCleanAllSubtasksWhenEpicDeleted() {
        Epic epic = new Epic("Epic", "");
        manager.createEpic(epic);

        Subtask subtask1 = new Subtask("Sub1", "", epic.getId());
        Subtask subtask2 = new Subtask("Sub2", "", epic.getId());
        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);

        assertEquals(2, manager.getAllSubtasks().size());

        manager.deleteEpic(epic.getId());

        assertTrue(manager.getAllSubtasks().isEmpty());
        assertNull(manager.getSubtask(subtask1.getId()));
        assertNull(manager.getSubtask(subtask2.getId()));

        assertNull(manager.getEpic(epic.getId()));
    }

}