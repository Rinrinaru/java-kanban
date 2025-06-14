package tracker.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tracker.exceptions.TimeConflictException;
import tracker.model.*;

import java.time.Duration;
import java.time.LocalDateTime;

import tracker.model.Task.Status;

import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskManagerTest<T extends TaskManager> {
    protected T manager;

    protected abstract T createTaskManager();

    @BeforeEach
    void setUp() {
        manager = createTaskManager();
    }

    @Test
    void shouldCreateAndGetTask() {
        Task task = new Task("Test", "Description");
        manager.createTask(task);
        Task savedTask = manager.getTask(task.getId());

        assertNotNull(savedTask, "Задача не найдена");
        assertEquals(task, savedTask, "Задачи не совпадают");
    }

    @Test
    void shouldUpdateTask() {
        Task task = new Task("Test", "Description");
        manager.createTask(task);

        Task updated = new Task("Updated", "New desc");
        updated.setId(task.getId());
        manager.updateTask(updated);

        assertEquals("Updated", manager.getTask(task.getId()).getName(), "Имя задачи не обновилось");
    }

    @Test
    void shouldDeleteTask() {
        Task task = new Task("Test", "Description");
        manager.createTask(task);
        manager.deleteTask(task.getId());

        assertNull(manager.getTask(task.getId()), "Задача не удалилась");
    }

    @Test
    void shouldDeleteAllTasks() {
        manager.createTask(new Task("Task 1", ""));
        manager.createTask(new Task("Task 2", ""));
        manager.deleteAllTasks();

        assertTrue(manager.getAllTasks().isEmpty(), "Не все задачи удалены");
    }

    @Test
    void shouldCalculateEpicStatus() {
        Epic epic = new Epic("Epic", "Description");
        manager.createEpic(epic);

        Subtask sub1 = new Subtask("Sub 1", "", epic.getId());
        Subtask sub2 = new Subtask("Sub 2", "", epic.getId());
        manager.createSubtask(sub1);
        manager.createSubtask(sub2);

        assertEquals(Status.NEW, epic.getStatus(), "Все подзадачи NEW -> эпик NEW");

        sub1.setStatus(Status.DONE);
        manager.updateSubtask(sub1);
        assertEquals(Status.IN_PROGRESS, epic.getStatus(), "NEW и DONE -> эпик IN_PROGRESS");

        sub2.setStatus(Status.DONE);
        manager.updateSubtask(sub2);
        assertEquals(Status.DONE, epic.getStatus(), "Все подзадачи DONE -> эпик DONE");

        sub1.setStatus(Status.IN_PROGRESS);
        manager.updateSubtask(sub1);
        assertEquals(Status.IN_PROGRESS, epic.getStatus(), "Любая подзадача IN_PROGRESS -> эпик IN_PROGRESS");
    }

    @Test
    void shouldNotAllowSubtaskWithoutEpic() {
        Subtask subtask = new Subtask("Subtask", "", 999);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> manager.createSubtask(subtask),
                "Ожидалось исключение при создании подзадачи без эпика"
        );

        assertEquals("Epic with id 999 not found", exception.getMessage());
    }

    @Test
    void shouldCalculateTaskEndTime() {
        Task task = new Task("Task", "Description");
        LocalDateTime start = LocalDateTime.now();
        task.setStartTime(start);
        task.setDuration(Duration.ofHours(2));

        assertEquals(start.plusHours(2), task.getEndTime(), "Неверное время окончания");
    }

    @Test
    void shouldCalculateEpicTimeFields() {
        Epic epic = new Epic("Epic", "");
        manager.createEpic(epic);

        LocalDateTime now = LocalDateTime.now();

        Subtask sub1 = new Subtask("Sub 1", "", epic.getId());
        sub1.setStartTime(now);
        sub1.setDuration(Duration.ofHours(1));

        Subtask sub2 = new Subtask("Sub 2", "", epic.getId());
        sub2.setStartTime(now.plusHours(2));
        sub2.setDuration(Duration.ofHours(3));

        manager.createSubtask(sub1);
        manager.createSubtask(sub2);

        assertEquals(now, epic.getStartTime(), "Неверное время начала эпика");
        assertEquals(now.plusHours(5), epic.getEndTime(), "Неверное время окончания эпика");
        assertEquals(Duration.ofHours(4), epic.getDuration(), "Неверная продолжительность эпика");
    }

    @Test
    void shouldDetectTimeOverlap() {
        Task task1 = new Task("Task 1", "");
        task1.setStartTime(LocalDateTime.now());
        task1.setDuration(Duration.ofHours(1));
        manager.createTask(task1);

        Task task2 = new Task("Task 2", "");
        task2.setStartTime(LocalDateTime.now().plusMinutes(30));
        task2.setDuration(Duration.ofHours(1));

        assertThrows(TimeConflictException.class,
                () -> manager.createTask(task2),
                "Ожидалось исключение при пересечении времени задач"
        );
    }

    @Test
    void shouldAddToHistoryWhenGettingTask() {
        Task task = new Task("Task", "");
        manager.createTask(task);
        manager.getTask(task.getId());

        assertFalse(manager.getHistory().isEmpty(), "Задача должна добавиться в историю");
        assertEquals(task, manager.getHistory().get(0), "Неверная задача в истории");
    }

    @Test
    void shouldNotDuplicateInHistory() {
        Task task = new Task("Task", "");
        manager.createTask(task);

        manager.getTask(task.getId());
        manager.getTask(task.getId());

        assertEquals(1, manager.getHistory().size(), "История не должна содержать дубликатов");
    }
}