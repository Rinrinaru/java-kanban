package tracker.controllers;

import tracker.exceptions.ManagerSaveException;
import tracker.model.Epic;
import tracker.model.Subtask;
import tracker.model.Task;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

    public FileBackedTaskManager(HistoryManager historyManager, File file) {
        super(historyManager);
        this.file = file;
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(new InMemoryHistoryManager(), file);

        try {
            String content = Files.readString(file.toPath());
            String[] lines = content.split("\n");

            if (lines.length > 1) {
                Map<Integer, Task> tasks = new HashMap<>();
                Map<Integer, Epic> epics = new HashMap<>();
                Map<Integer, Subtask> subtasks = new HashMap<>();
                int maxId = 0;

                for (int i = 1; i < lines.length; i++) {
                    Task task = manager.fromString(lines[i]);
                    if (task == null) continue;

                    if (task.getId() > maxId) {
                        maxId = task.getId();
                    }

                    switch (task.getType()) {
                        case TASK:
                            tasks.put(task.getId(), task);
                            break;
                        case EPIC:
                            epics.put(task.getId(), (Epic) task);
                            break;
                        case SUBTASK:
                            Subtask subtask = (Subtask) task;
                            subtasks.put(subtask.getId(), subtask);
                            Epic epic = epics.get(subtask.getEpicId());
                            if (epic != null) {
                                epic.getSubtaskIds().add(subtask.getId());
                            }
                            break;
                    }
                }

                manager.tasks.putAll(tasks);
                manager.epics.putAll(epics);
                manager.subtasks.putAll(subtasks);
                manager.nextId = maxId + 1;

                for (Epic epic : epics.values()) {
                    manager.updateEpicStatus(epic.getId());
                }
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка загрузки из файла", e);
        }

        return manager;
    }

    protected void save() {
        try {
            StringBuilder builder = new StringBuilder();
            builder.append("id,type,name,status,description,epic\n");

            getAllTasks().forEach(task -> builder.append(toString(task)).append("\n"));
            getAllEpics().forEach(epic -> builder.append(toString(epic)).append("\n"));
            getAllSubtasks().forEach(subtask -> builder.append(toString(subtask)).append("\n"));

            Files.writeString(file.toPath(), builder.toString());
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка сохранения в файл", e);
        }
    }

    private String toString(Task task) {
        return String.join(",", String.valueOf(task.getId()), task.getType().name(), task.getName(), task.getStatus().name(), task.getDescription(), task instanceof Subtask ? String.valueOf(((Subtask) task).getEpicId()) : "");
    }

    private Task fromString(String value) {
        String[] parts = value.split(",");
        if (parts.length < 5) return null;

        try {
            int id = Integer.parseInt(parts[0]);
            Task.Type type = Task.Type.valueOf(parts[1]);
            String name = parts[2];
            Task.Status status = Task.Status.valueOf(parts[3]);
            String description = parts[4];

            switch (type) {
                case TASK:
                    Task task = new Task(name, description);
                    task.setId(id);
                    task.setStatus(status);
                    return task;
                case EPIC:
                    Epic epic = new Epic(name, description);
                    epic.setId(id);
                    epic.setStatus(status);
                    return epic;
                case SUBTASK:
                    if (parts.length < 6) return null;
                    int epicId = Integer.parseInt(parts[5]);
                    Subtask subtask = new Subtask(name, description, epicId);
                    subtask.setId(id);
                    subtask.setStatus(status);
                    return subtask;
            }
        } catch (NumberFormatException e) {
            System.err.println("Ошибка: неверный числовой формат");
            return null;
        } catch (IllegalArgumentException e) {
            System.err.println("Ошибка: неверное значение enum");
            return null;
        } catch (Exception e) {
            System.err.println("Неизвестная ошибка при разборе строки" + e.getMessage());
            return null;
        }
        return null;
    }

    @Override
    public void createTask(Task task) {
        super.createTask(task);
        save();
    }

    @Override
    public void createEpic(Epic epic) {
        super.createEpic(epic);
        save();
    }

    @Override
    public void createSubtask(Subtask subtask) {
        super.createSubtask(subtask);
        save();
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void deleteTask(int id) {
        super.deleteTask(id);
        save();
    }

    @Override
    public void deleteEpic(int epicId) {
        super.deleteEpic(epicId);
        save();
    }

    @Override
    public void deleteSubtask(int subtaskId) {
        super.deleteSubtask(subtaskId);
        save();
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    @Override
    public void deleteAllSubtasks() {
        super.deleteAllSubtasks();
        save();
    }
}

