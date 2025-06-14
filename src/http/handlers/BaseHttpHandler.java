package http.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public abstract class BaseHttpHandler implements HttpHandler {
    protected Gson gson = new Gson();

    protected void sendText(HttpExchange exchange, String text, int statusCode) throws IOException {
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, resp.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(resp);
        }
    }

    protected void sendSuccess(HttpExchange exchange, Object response) throws IOException {
        sendText(exchange, gson.toJson(response), 200);
    }

    protected void sendCreated(HttpExchange exchange, Object response) throws IOException {
        sendText(exchange, gson.toJson(response), 201);
    }

    protected void sendNotFound(HttpExchange exchange) throws IOException {
        sendText(exchange, "{\"message\":\"Not Found\"}", 404);
    }

    protected void sendHasInteractions(HttpExchange exchange) throws IOException {
        sendText(exchange, "{\"message\":\"Task time overlaps with existing tasks\"}", 406);
    }

    protected void sendBadRequest(HttpExchange exchange) throws IOException {
        sendText(exchange, "{\"message\":\"Bad Request\"}", 400);
    }

    protected String readRequestBody(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    public abstract void handle(HttpExchange exchange) throws IOException;
}