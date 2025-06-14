package http.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;
import tracker.controllers.HistoryManager;
import tracker.controllers.InMemoryHistoryManager;
import tracker.controllers.InMemoryTaskManager;
import tracker.controllers.TaskManager;
import http.handlers.*;
import tracker.model.*;
import http.server.LocalDateTimeAdapter;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;

public class HttpTaskServer {
    private static final int PORT = 8081;
    private final HttpServer server;
    private final TaskManager taskManager;
    private static Gson gson;

    static {
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
    }

    public HttpTaskServer(TaskManager taskManager) throws IOException {
        this.taskManager = taskManager;
        this.server = HttpServer.create(new InetSocketAddress(PORT), 0);
        registerHandlers();
    }

    private void registerHandlers() {
        server.createContext("/tasks", new TasksHandler(taskManager, gson));
        server.createContext("/subtasks", new SubtasksHandler(taskManager, gson));
        server.createContext("/epics", new EpicsHandler(taskManager, gson));
        server.createContext("/history", new HistoryHandler(taskManager, gson));
        server.createContext("/prioritized", new PrioritizedHandler(taskManager, gson));
    }

    public void start() {
        server.start();
        System.out.println("HTTP Task Server started on port " + PORT);
    }

    public void stop() {
        server.stop(0);
        System.out.println("HTTP Task Server stopped");
    }

    public static Gson getGson() {
        return gson;
    }

    private static Gson createGson() {
        return new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .create();
    }

    public static void main(String[] args) throws IOException {
        HistoryManager historyManager = new InMemoryHistoryManager();
        TaskManager manager = new InMemoryTaskManager(historyManager);
        HttpTaskServer server = new HttpTaskServer(manager);
        server.start();
    }
}