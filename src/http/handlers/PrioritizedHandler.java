package http.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import tracker.controllers.TaskManager;
import tracker.model.Task;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PrioritizedHandler extends BaseHttpHandler {
    private final TaskManager taskManager;

    public PrioritizedHandler(TaskManager taskManager, Gson gson) {
        this.taskManager = taskManager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if ("GET".equals(exchange.getRequestMethod())) {
                List<Task> prioritizedTasks = getPrioritizedTasks();
                sendSuccess(exchange, prioritizedTasks);
            } else {
                sendBadRequest(exchange);
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendBadRequest(exchange);
        }
    }

    private List<Task> getPrioritizedTasks() {
        return Stream.concat(
                        taskManager.getAllTasks().stream(),
                        taskManager.getAllSubtasks().stream()
                )
                .filter(task -> task.getStartTime() != null)
                .sorted(Comparator.comparing(Task::getStartTime))
                .collect(Collectors.toList());
    }
}