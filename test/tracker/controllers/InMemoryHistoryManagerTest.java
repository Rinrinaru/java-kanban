package tracker.controllers;

import org.junit.jupiter.api.Test;
import tracker.model.Task;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {

    @Test
    void addAndGetHistoryTest() {
        HistoryManager manager = new InMemoryHistoryManager();
        Task task1 = new Task("Электроника", "Отчет по лабе 3");
        Task task2 = new Task("ЧМИ", "Узнать про распределение задач");
        manager.add(task1);
        manager.add(task2);
        List<Task> history = manager.getHistory();
        assertEquals(2, history.size(), "В истории должно быть 2 задачи");
        assertEquals(task1, history.get(0), "Первая задача task1");
        assertEquals(task2, history.get(1), "Вторая задача task2");
    }

    @Test
    void historySizeLimitTest() {
        HistoryManager manager = new InMemoryHistoryManager();
        for (int i = 0; i < 11; i++) {
            Task task = new Task("Задача " + i, "Описание " + i);
            manager.add(task);
        }
        List<Task> history = manager.getHistory();
        assertEquals(10, history.size(), "В истории должно быть минимум 10 задач");
        assertFalse(history.contains(new Task("Task 0", "Description 0")), "First task should be evicted");
    }

    @Test
    void addNullTaskTest() {
        HistoryManager manager = new InMemoryHistoryManager();
        manager.add(null);
        assertTrue(manager.getHistory().isEmpty(), "Если задача null, то список остается пустым");
    }
}