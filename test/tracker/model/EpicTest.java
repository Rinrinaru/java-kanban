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
        Epic epic = new Epic("Эпик", "Описание эпика");
        assertTrue(epic.getSubtaskIds().isEmpty(), "Новый эпик должен иметь пустой список подзадач");

        epic.addSubtaskId(1);
        assertEquals(1, epic.getSubtaskIds().size(), "Должна быть одна подзадача");
        assertEquals(1, epic.getSubtaskIds().get(0), "Неверный ID подзадачи");

        epic.setSubtaskIds(List.of(10, 20));
        assertEquals(2, epic.getSubtaskIds().size(), "Должно быть две подзадачи");
        assertTrue(epic.getSubtaskIds().containsAll(List.of(10, 20)), "Список должен содержать 10 и 20");

        List<Integer> subtaskIds = epic.getSubtaskIds();
        assertThrows(UnsupportedOperationException.class,
                () -> subtaskIds.add(30),
                "Нельзя модифицировать список подзадач напрямую"
        );
    }

}
