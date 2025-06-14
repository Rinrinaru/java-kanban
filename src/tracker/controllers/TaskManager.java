package tracker.controllers;

import tracker.model.Epic;
import tracker.model.Subtask;
import tracker.model.Task;

import java.util.List;

public interface TaskManager {
    // Получение списка всех задач
    List<Task> getAllTasks();

    List<Epic> getAllEpics();

    List<Subtask> getAllSubtasks();

    // Удаление всех задач
    void deleteAllTasks();

    void deleteAllEpics();

    void deleteAllSubtasks();

    // Получение задачи по ID
    Task getTask(int id);

    Epic getEpic(int id);

    Subtask getSubtask(int id);

    // Удаление по ID
    void deleteTask(int id);

    void deleteEpic(int id);

    void deleteSubtask(int id);

    // Создание задач
    void createTask(Task task);

    void createEpic(Epic epic);

    void createSubtask(Subtask subtask);

    // Обновление задач
    void updateTask(Task updatedTask);

    void updateEpic(Epic updatedEpic);

    void updateSubtask(Subtask updatedSubtask);

    List<Task> getHistory();

}