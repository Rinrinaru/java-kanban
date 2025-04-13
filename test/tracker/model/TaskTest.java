package tracker.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    @Test
    void shouldCreateTaskWithInitialValues() {
        Task task = new Task("Электроника", "Отчет по лабе 3");

        assertEquals("Электроника", task.getName());
        assertEquals("Отчет по лабе 3", task.getDescription());
        assertEquals(Task.Status.NEW, task.getStatus());
        assertEquals(0, task.getId());
    }

    @Test
    void shouldAllowModificationOfFields() {
        Task task = new Task("ЧМИ", "Узнать про распределение задач");

        task.setName("ЧМИ 2.0");
        task.setDescription("Прийти и ухватить задачу");
        task.setStatus(Task.Status.IN_PROGRESS);
        task.setId(10);

        assertEquals("ЧМИ 2.0", task.getName());
        assertEquals("Прийти и ухватить задачу", task.getDescription());
        assertEquals(Task.Status.IN_PROGRESS, task.getStatus());
        assertEquals(10, task.getId());
    }

    @Test
    void shouldCorrectlyCompareTasks() {
        Task task1 = new Task("Электроника", "Отчет по лабе 3");
        Task task2 = new Task("ЧМИ", "Узнать про распределение задач");

        task1.setId(1);
        task2.setId(1);

        assertEquals(task1, task2);
        assertEquals(task1.hashCode(), task2.hashCode());
    }
}