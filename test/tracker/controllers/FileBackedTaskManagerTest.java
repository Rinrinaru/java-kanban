package tracker.controllers;

import org.junit.jupiter.api.*;
import tracker.model.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

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
}