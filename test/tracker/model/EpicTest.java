package tracker.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {

    @Test
    void shouldCorrectlyInitializeEpic() {
        Epic epic = new Epic("Пятый модуль", "Сделать до 13.04");

        assertEquals("Пятый модуль", epic.getName());
        assertEquals("Сделать до 13.04", epic.getDescription());
        assertEquals(Task.Status.NEW, epic.getStatus());

        assertTrue(epic.getSubtaskIds().isEmpty());
    }

    @Test
    void shouldManageSubtaskIds() {
        Epic epic = new Epic("Эпик", "");

        epic.getSubtaskIds().add(1);
        assertEquals(1, epic.getSubtaskIds().get(0));

        epic.setSubtaskIds(List.of(10, 20));
        assertEquals(2, epic.getSubtaskIds().size());
    }

}
