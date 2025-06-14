package http.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import tracker.controllers.TaskManager;
import tracker.exceptions.TimeConflictException;
import tracker.model.Task;

import java.io.IOException;

public class TasksHandler extends BaseHttpHandler {
    private final TaskManager taskManager;

    public TasksHandler(TaskManager taskManager, Gson gson) {
        this.taskManager = taskManager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            switch (method) {
                case "GET":
                    handleGet(exchange, path);
                    break;
                case "POST":
                    handlePost(exchange);
                    break;
                case "DELETE":
                    handleDelete(exchange, path);
                    break;
                default:
                    sendBadRequest(exchange);
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendBadRequest(exchange);
        }
    }

    private void handleGet(HttpExchange exchange, String path) throws IOException {
        try {
            if (path.equals("/tasks")) {
                sendSuccess(exchange, taskManager.getAllTasks());
            } else if (path.matches("/tasks/\\d+")) {
                int id = extractId(path);
                try {
                    Task task = taskManager.getTask(id);
                    sendSuccess(exchange, task);
                } catch (RuntimeException e) {
                    sendNotFound(exchange);
                }
            } else {
                sendBadRequest(exchange);
            }
        } catch (NumberFormatException e) {
            sendBadRequest(exchange);
        } catch (Exception e) {
            e.printStackTrace();
            sendBadRequest(exchange);
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        try {
            String body = readRequestBody(exchange);
            Task task = gson.fromJson(body, Task.class);

            if (task.getId() == 0) {
                try {
                    taskManager.createTask(task);
                    sendCreated(exchange, task);
                } catch (TimeConflictException e) {
                    sendHasInteractions(exchange);
                }
            } else {
                try {
                    taskManager.updateTask(task);
                    sendSuccess(exchange, task);
                } catch (TimeConflictException e) {
                    sendHasInteractions(exchange);
                }
            }
        } catch (JsonSyntaxException e) {
            sendBadRequest(exchange);
        } catch (IOException e) {
            e.printStackTrace();
            sendBadRequest(exchange);
        }
    }

    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        try {
            if (path.matches("/tasks/\\d+")) {
                int id = extractId(path);
                taskManager.deleteTask(id);
                sendSuccess(exchange, "Task deleted successfully");
            } else {
                sendBadRequest(exchange);
            }
        } catch (NumberFormatException e) {
            sendBadRequest(exchange);
        } catch (Exception e) {
            e.printStackTrace();
            sendBadRequest(exchange);
        }
    }

    private int extractId(String path) {
        String[] parts = path.split("/");
        return Integer.parseInt(parts[2]);
    }
}