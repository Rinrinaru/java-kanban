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
    void SaveAndLoadTasksTest() {
        Task task = new Task("Задача", "важное описание");
        manager.createTask(task);

        Epic epic = new Epic("Эпик", "важное эпичное описание");
        manager.createEpic(epic);

        Subtask subtask = new Subtask("Подзадача", "важное подзадачное описание", epic.getId());
        manager.createSubtask(subtask);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        List<Task> tasks = loadedManager.getAllTasks();
        assertEquals(1, tasks.size());
        assertEquals("Задача", tasks.get(0).getName());

        List<Epic> epics = loadedManager.getAllEpics();
        assertEquals(1, epics.size());
        assertEquals("Эпик", epics.get(0).getName());

        List<Subtask> subtasks = loadedManager.getAllSubtasks();
        assertEquals(1, subtasks.size());
        assertEquals("Подзадача", subtasks.get(0).getName());
        assertEquals(epic.getId(), subtasks.get(0).getEpicId());
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
    void CalculateEpicStatusAfterLoadTest() {
        Epic epic = new Epic("Эпик", "важное эпичное описание");
        manager.createEpic(epic);

        Subtask subtask = new Subtask("Подзадача", "важное подзадачное описание", epic.getId());
        subtask.setStatus(Task.Status.DONE);
        manager.createSubtask(subtask);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);
        Epic loadedEpic = loadedManager.getEpic(epic.getId());

        assertEquals(Task.Status.DONE, loadedEpic.getStatus());
    }
}