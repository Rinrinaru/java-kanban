import tracker.controllers.Managers;
import tracker.controllers.TaskManager;
import tracker.model.Epic;
import tracker.model.Subtask;
import tracker.model.Task;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        TaskManager manager = Managers.getDefault();

        Task task1 = new Task("Электроника", "Отчет по лабе 3");
        Task task2 = new Task("ЧМИ", "Узнать про распределение задач");
        manager.createTask(task1);
        manager.createTask(task2);

        Epic epic1 = new Epic("Убраться", "Освободить квартиру от хлама");
        manager.createEpic(epic1);
        Subtask subtask1 = new Subtask("Выкинуть мусор", "Не забыть про холодос", epic1.getId());
        Subtask subtask2 = new Subtask("Разложить одежду", "Даже ту, что типо не мятая лежит", epic1.getId());
        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);

        Epic epic2 = new Epic("Спринт 4", "Домучить");
        manager.createEpic(epic2);
        Subtask subtask3 = new Subtask("Сделать 4 проект", "Ответить на вопросы в конце и прикинуть структуру", epic2.getId());
        manager.createSubtask(subtask3);

        System.out.println("Задачи:");
        for (Task task : manager.getAllTasks()) {
            System.out.println(task.getName() + " [ID: " + task.getId() + "]");
        }

        System.out.println("Эпики:");
        for (Epic epic : manager.getAllEpics()) {
            System.out.println(epic.getName() + " [ID: " + epic.getId() + "]");
        }

        System.out.println("Подзадачи:");
        for (Subtask subtask : manager.getAllSubtasks()) {
            System.out.println(subtask.getName() + " [ID: " + subtask.getId() +
                    ", Эпик: " + subtask.getEpicId() + "]");
        }

        System.out.println("=== Начальные статусы ===");
        System.out.println("Задача 1: " + task1.getStatus());
        System.out.println("Задача 2: " + task2.getStatus());
        System.out.println("Подзадача 1: " + subtask1.getStatus());
        System.out.println("Подзадача 2: " + subtask2.getStatus());
        System.out.println("Подзадача 3: " + subtask3.getStatus());
        System.out.println("Эпик 1: " + epic1.getStatus());
        System.out.println("Эпик 2: " + epic2.getStatus());

        task1.setStatus(Task.Status.IN_PROGRESS);
        manager.updateTask(task1);

        subtask1.setStatus(Task.Status.DONE);
        subtask2.setStatus(Task.Status.IN_PROGRESS);
        manager.updateSubtask(subtask1);
        manager.updateSubtask(subtask2);

        subtask3.setStatus(Task.Status.DONE);
        manager.updateSubtask(subtask3);

        System.out.println("После изменения статусов");
        System.out.println("Задача 1: " + task1.getStatus());
        System.out.println("Задача 2: " + task2.getStatus());
        System.out.println("Подзадача 1: " + subtask1.getStatus());
        System.out.println("Подзадача 2: " + subtask2.getStatus());
        System.out.println("Подзадача 3: " + subtask3.getStatus());
        System.out.println("Эпик 1: " + epic1.getStatus() + " (должен быть IN_PROGRESS)");
        System.out.println("Эпик 2: " + epic2.getStatus() + " (должен быть DONE)");

        manager.getTask(task1.getId());
        manager.getEpic(epic1.getId());
        manager.getSubtask(subtask1.getId());
        manager.getTask(task2.getId());
        manager.getTask(task1.getId());


        List<Task> history = manager.getHistory();
        System.out.println("История просмотров:");
        for (Task task : history) {
            System.out.println(task.getName() + " (" + task.getClass().getSimpleName() + ")");
        }
    }
}