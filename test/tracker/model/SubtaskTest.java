package tracker.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SubtaskTest {

    @Test
    void shouldCreateSubtaskWithEpicLink() {
        Subtask subtask = new Subtask("Купить молоко", "", 1);

        assertEquals("Купить молоко", subtask.getName());
        assertEquals(1, subtask.getEpicId());
        assertEquals(Task.Status.NEW, subtask.getStatus());
    }
}