package tracker.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tracker.model.Task;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {

    private InMemoryHistoryManager manager;
    private Task task1;
    private Task task2;
    private Task task3;

    @BeforeEach
    void beforeEach() {
        manager = new InMemoryHistoryManager();

        task1 = new Task("Электроника", "Отчет по лабе 3");
        task1.setId(1);

        task2 = new Task("ЧМИ", "Узнать про распределение задач");
        task2.setId(2);

        task3 = new Task("Java", "Успеть доделать все до строгого дедлайна");
        task3.setId(3);
    }

    @Test
    void addAndGetHistoryTest() {
        manager.add(task1);
        manager.add(task2);

        List<Task> history = manager.getHistory();

        assertEquals(2, history.size(), "В истории должно быть 2 задачи");
        assertEquals(task1, history.get(0), "Первая задача должна быть task1");
        assertEquals(task2, history.get(1), "Вторая задача должна быть task2");
    }

    @Test
    void duplicateTaskShouldMoveToEnd() {
        manager.add(task1);
        manager.add(task2);
        manager.add(task1);

        List<Task> history = manager.getHistory();

        assertEquals(2, history.size(), "Дубликаты не должны создавать новые записи");
        assertEquals(task2, history.get(0), "Первая задача должна быть task2");
        assertEquals(task1, history.get(1), "Последняя задача должна быть task1");
    }

    @Test
    void removeTaskFromHistory() {
        manager.add(task1);
        manager.add(task2);
        manager.add(task3);

        manager.remove(task2.getId());

        List<Task> history = manager.getHistory();

        assertEquals(2, history.size(), "После удаления должно остаться 2 задачи");
        assertFalse(history.contains(task2), "История не должна содержать удаленную задачу");
        assertEquals(task1, history.get(0), "Первая задача должна быть task1");
        assertEquals(task3, history.get(1), "Вторая задача должна быть task3");
    }

    @Test
    void removeFromEmptyHistory() {
        manager.remove(1);

        assertTrue(manager.getHistory().isEmpty(), "Удаление из пустой истории не должно вызывать ошибок");
    }

    @Test
    void addNullTaskTest() {
        manager.add(null);

        assertTrue(manager.getHistory().isEmpty(), "Добавление null не должно изменять историю");
    }

    @Test
    void historyOrderAfterMultipleOperations() {
        manager.add(task1);
        manager.add(task2);
        manager.remove(task1.getId());
        manager.add(task3);
        manager.add(task1);

        List<Task> history = manager.getHistory();

        assertEquals(3, history.size(), "Неверное количество задач в истории");
        assertEquals(task2, history.get(0), "Первая задача должна быть task2");
        assertEquals(task3, history.get(1), "Вторая задача должна быть task3");
        assertEquals(task1, history.get(2), "Третья задача должна быть task1");
    }
}