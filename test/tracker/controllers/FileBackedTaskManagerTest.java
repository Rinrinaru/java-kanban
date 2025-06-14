package tracker.controllers;

import org.junit.jupiter.api.*;
import tracker.model.*;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {
    private File tempFile;
    private FileBackedTaskManager manager;

    @BeforeEach
    void setUp() throws IOException {
        tempFile = File.createTempFile("tasks", ".csv");
        manager = new FileBackedTaskManager(new InMemoryHistoryManager(), tempFile);
    }

    @AfterEach
    void tearDown() {
        tempFile.delete();
    }

    @Test
    void SaveTaskChangesTest() {
        Task task = new Task("Задача", "важное описание");
        manager.createTask(task);

        task.setName("Обновленная задача");
        task.setStatus(Task.Status.DONE);
        manager.updateTask(task);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);
        Task loadedTask = loadedManager.getTask(task.getId());

        assertNotNull(loadedTask);
        assertEquals("Обновленная задача", loadedTask.getName());
        assertEquals(Task.Status.DONE, loadedTask.getStatus());
    }

    @Test
    void shouldSaveAndLoadTaskWithTimeParameters() {
        Task task = new Task("Задача на время", "задачное описание");
        LocalDateTime startTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        Duration duration = Duration.ofHours(2);
        task.setStartTime(startTime);
        task.setDuration(duration);

        manager.createTask(task);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);
        Task loadedTask = loadedManager.getTask(task.getId());

        assertNotNull(loadedTask.getStartTime());
        assertNotNull(loadedTask.getDuration());
        assertEquals(startTime, loadedTask.getStartTime());
        assertEquals(duration, loadedTask.getDuration());
        assertEquals(startTime.plus(duration), loadedTask.getEndTime());
    }

    @Test
    void shouldSaveAndLoadEpicWithCalculatedTime() {
        Epic epic = new Epic("Эпик на время", "эпически важный текст");
        manager.createEpic(epic);

        LocalDateTime startTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);

        Subtask subtask1 = new Subtask("Подзадача 1", "Описание", epic.getId());
        subtask1.setStartTime(startTime);
        subtask1.setDuration(Duration.ofHours(1));
        manager.createSubtask(subtask1);

        Subtask subtask2 = new Subtask("Подзадача 2", "Описание", epic.getId());
        subtask2.setStartTime(startTime.plusHours(2));
        subtask2.setDuration(Duration.ofHours(3));
        manager.createSubtask(subtask2);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);
        Epic loadedEpic = loadedManager.getEpic(epic.getId());

        assertEquals(startTime, loadedEpic.getStartTime());
        assertEquals(Duration.ofHours(4), loadedEpic.getDuration());
        assertEquals(startTime.plusHours(5), loadedEpic.getEndTime());
    }
}