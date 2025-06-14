package http;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import http.server.HttpTaskServer;
import org.junit.jupiter.api.*;
import tracker.controllers.*;
import tracker.model.*;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HttpTaskServerTest {
    private static final int PORT = 8080;
    private static final String BASE_URL = "http://localhost:" + PORT;

    private TaskManager manager;
    private HttpTaskServer taskServer;
    private HttpClient client;
    private Gson gson;

    @BeforeEach
    void setUp() throws IOException {
        HistoryManager historyManager = new InMemoryHistoryManager();
        manager = new InMemoryTaskManager(historyManager);
        taskServer = new HttpTaskServer(manager);
        taskServer.start();
        client = HttpClient.newHttpClient();
        gson = HttpTaskServer.getGson();

        manager.deleteAllTasks();
        manager.deleteAllEpics();
        manager.deleteAllSubtasks();
    }

    @AfterEach
    void tearDown() {
        taskServer.stop();
    }

    private Task createTestTask() {
        Task task = new Task("Test task", "Description");
        task.setStatus(Task.Status.NEW);
        task.setDuration(Duration.ofMinutes(30));
        task.setStartTime(LocalDateTime.now());
        return task;
    }

    private Epic createTestEpic() {
        return new Epic("Test epic", "Description");
    }

    private Subtask createTestSubtask(Epic epic) {
        Subtask subtask = new Subtask("Test subtask", "Description", epic.getId());
        subtask.setStatus(Task.Status.NEW);
        subtask.setDuration(Duration.ofMinutes(15));
        subtask.setStartTime(LocalDateTime.now().plusHours(2));
        return subtask;
    }

    @Test
    void testCreateAndGetTask() throws Exception {
        Task task = createTestTask();
        String taskJson = gson.toJson(task);

        HttpRequest createRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> createResponse = client.send(createRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, createResponse.statusCode());

        HttpRequest getAllRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tasks"))
                .GET()
                .build();

        HttpResponse<String> getAllResponse = client.send(getAllRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, getAllResponse.statusCode());

        List<Task> tasks = gson.fromJson(getAllResponse.body(), new TypeToken<List<Task>>(){}.getType());
        assertEquals(1, tasks.size());
        assertEquals("Test task", tasks.get(0).getName());
    }

    @Test
    void testUpdateTask() throws Exception {
        Task task = createTestTask();
        manager.createTask(task);

        task.setName("Updated task");
        task.setStatus(Task.Status.IN_PROGRESS);
        String updatedJson = gson.toJson(task);

        HttpRequest updateRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(updatedJson))
                .build();

        HttpResponse<String> updateResponse = client.send(updateRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, updateResponse.statusCode());

        Task updatedTask = manager.getTask(task.getId());
        assertEquals("Updated task", updatedTask.getName());
        assertEquals(Task.Status.IN_PROGRESS, updatedTask.getStatus());
    }

    @Test
    void testDeleteTask() throws Exception {
        Task task = createTestTask();
        manager.createTask(task);

        HttpRequest deleteRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tasks/" + task.getId()))
                .DELETE()
                .build();

        HttpResponse<String> deleteResponse = client.send(deleteRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, deleteResponse.statusCode());

        assertNull(manager.getTask(task.getId()));
        assertTrue(manager.getAllTasks().isEmpty());
    }

    @Test
    void testCreateAndGetEpic() throws Exception {
        Epic epic = createTestEpic();
        String epicJson = gson.toJson(epic);

        HttpRequest createRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/epics"))
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();

        HttpResponse<String> createResponse = client.send(createRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, createResponse.statusCode());

        HttpRequest getAllRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/epics"))
                .GET()
                .build();

        HttpResponse<String> getAllResponse = client.send(getAllRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, getAllResponse.statusCode());

        List<Epic> epics = gson.fromJson(getAllResponse.body(), new TypeToken<List<Epic>>(){}.getType());
        assertEquals(1, epics.size());
        assertEquals("Test epic", epics.get(0).getName());
    }

    @Test
    void testEpicStatusCalculation() throws Exception {
        Epic epic = createTestEpic();
        manager.createEpic(epic);

        Subtask subtask1 = createTestSubtask(epic);
        subtask1.setStatus(Task.Status.DONE);
        manager.createSubtask(subtask1);

        Subtask subtask2 = createTestSubtask(epic);
        subtask2.setStatus(Task.Status.IN_PROGRESS);
        manager.createSubtask(subtask2);

        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/epics/" + epic.getId()))
                .GET()
                .build();

        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, getResponse.statusCode());

        Epic retrievedEpic = gson.fromJson(getResponse.body(), Epic.class);
        assertEquals(Task.Status.IN_PROGRESS, retrievedEpic.getStatus());
    }

    @Test
    void testCreateAndGetSubtask() throws Exception {
        Epic epic = createTestEpic();
        manager.createEpic(epic);

        Subtask subtask = createTestSubtask(epic);
        String subtaskJson = gson.toJson(subtask);

        HttpRequest createRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/subtasks"))
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();

        HttpResponse<String> createResponse = client.send(createRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, createResponse.statusCode());

        HttpRequest getAllRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/subtasks"))
                .GET()
                .build();

        HttpResponse<String> getAllResponse = client.send(getAllRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, getAllResponse.statusCode());

        List<Subtask> subtasks = gson.fromJson(getAllResponse.body(), new TypeToken<List<Subtask>>(){}.getType());
        assertEquals(1, subtasks.size());
        assertEquals("Test subtask", subtasks.get(0).getName());
    }

    @Test
    void testGetEpicSubtasks() throws Exception {
        Epic epic = createTestEpic();
        manager.createEpic(epic);

        Subtask subtask1 = createTestSubtask(epic);
        manager.createSubtask(subtask1);

        Subtask subtask2 = createTestSubtask(epic);
        manager.createSubtask(subtask2);

        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/epics/" + epic.getId() + "/subtasks"))
                .GET()
                .build();

        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, getResponse.statusCode());

        List<Subtask> subtasks = gson.fromJson(getResponse.body(), new TypeToken<List<Subtask>>(){}.getType());
        assertEquals(2, subtasks.size());
    }

    @Test
    void testGetHistory() throws Exception {
        Task task = createTestTask();
        manager.createTask(task);
        manager.getTask(task.getId());

        Epic epic = createTestEpic();
        manager.createEpic(epic);
        manager.getEpic(epic.getId());

        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/history"))
                .GET()
                .build();

        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, getResponse.statusCode());

        List<Task> history = gson.fromJson(getResponse.body(), new TypeToken<List<Task>>(){}.getType());
        assertEquals(2, history.size());
        assertEquals(task.getId(), history.get(0).getId());
        assertEquals(epic.getId(), history.get(1).getId());
    }

    @Test
    void testGetNonExistentTask() throws Exception {
        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tasks/999"))
                .GET()
                .build();

        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, getResponse.statusCode());
    }

    @Test
    void testInvalidTaskCreation() throws Exception {
        String invalidJson = "{invalid json}";

        HttpRequest createRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(invalidJson))
                .build();

        HttpResponse<String> createResponse = client.send(createRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, createResponse.statusCode());
    }

    @Test
    void testSubtaskWithInvalidEpic() throws Exception {
        Subtask subtask = new Subtask("Invalid", "Description", 999);
        String subtaskJson = gson.toJson(subtask);

        HttpRequest createRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/subtasks"))
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();

        HttpResponse<String> createResponse = client.send(createRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, createResponse.statusCode());
    }
}