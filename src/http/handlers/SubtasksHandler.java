package http.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import tracker.controllers.TaskManager;
import tracker.exceptions.TimeConflictException;
import tracker.model.Subtask;

import java.io.IOException;

public class SubtasksHandler extends BaseHttpHandler {
    private final TaskManager taskManager;

    public SubtasksHandler(TaskManager taskManager, Gson gson) {
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
            if (path.equals("/subtasks")) {
                sendSuccess(exchange, taskManager.getAllSubtasks());
            } else if (path.matches("/subtasks/\\d+")) {
                int id = extractId(path);
                try {
                    Subtask subtask = taskManager.getSubtask(id);
                    sendSuccess(exchange, subtask);
                } catch (RuntimeException e) {
                    sendNotFound(exchange);
                }
            } else {
                sendBadRequest(exchange);
            }
        } catch (NumberFormatException e) {
            sendBadRequest(exchange);
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        try {
            String body = readRequestBody(exchange);
            Subtask subtask = gson.fromJson(body, Subtask.class);

            if (subtask.getId() == 0) {
                try {
                    taskManager.createSubtask(subtask);
                    sendCreated(exchange, subtask);
                } catch (TimeConflictException e) {
                    sendHasInteractions(exchange);
                }
            } else {
                try {
                    taskManager.updateSubtask(subtask);
                    sendSuccess(exchange, subtask);
                } catch (TimeConflictException e) {
                    sendHasInteractions(exchange);
                }
            }
        } catch (JsonSyntaxException e) {
            sendBadRequest(exchange);
        }
    }

    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        try {
            if (path.matches("/subtasks/\\d+")) {
                int id = extractId(path);
                taskManager.deleteSubtask(id);
                sendSuccess(exchange, "Subtask deleted successfully");
            } else {
                sendBadRequest(exchange);
            }
        } catch (NumberFormatException e) {
            sendBadRequest(exchange);
        }
    }

    private int extractId(String path) {
        String[] parts = path.split("/");
        return Integer.parseInt(parts[2]);
    }
}