package http.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import tracker.controllers.TaskManager;
import tracker.model.Epic;

import java.io.IOException;

public class EpicsHandler extends BaseHttpHandler {
    private final TaskManager taskManager;

    public EpicsHandler(TaskManager taskManager, Gson gson) {
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
            if (path.equals("/epics")) {
                sendSuccess(exchange, taskManager.getAllEpics());
            } else if (path.matches("/epics/\\d+")) {
                int id = extractId(path);
                try {
                    Epic epic = taskManager.getEpic(id);
                    sendSuccess(exchange, epic);
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
            Epic epic = gson.fromJson(body, Epic.class);

            if (epic.getId() == 0) {
                taskManager.createEpic(epic);
                sendCreated(exchange, epic);
            } else {
                taskManager.updateEpic(epic);
                sendSuccess(exchange, epic);
            }
        } catch (JsonSyntaxException e) {
            sendBadRequest(exchange);
        }
    }

    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        try {
            if (path.matches("/epics/\\d+")) {
                int id = extractId(path);
                taskManager.deleteEpic(id);
                sendSuccess(exchange, "Epic deleted successfully");
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