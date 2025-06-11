package tracker.controllers;

import tracker.model.Epic;
import tracker.model.Subtask;
import tracker.model.Task;

import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();
    private int nextId = 1;
    private final HistoryManager historyManager;

    public InMemoryTaskManager(HistoryManager historyManager) {
        this.historyManager = historyManager;
    }

    private int generateId() {
        return nextId++;
    }

    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public void deleteAllTasks() {
        tasks.clear();
    }

    @Override
    public void deleteAllEpics() {
        epics.clear();
        subtasks.clear();
    }

    @Override
    public void deleteAllSubtasks() {
        for (Epic epic : epics.values()) {
            epic.getSubtaskIds().clear();
            updateEpicStatus(epic.getId());
        }
        subtasks.clear();
    }

    @Override
    public Task getTask(int id) {
        Task task = tasks.get(id);
        historyManager.add(task);
        return task;
    }

    @Override
    public Epic getEpic(int id) {
        Epic epic = epics.get(id);
        historyManager.add(epic);
        return epic;
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask subtask = subtasks.get(id);
        historyManager.add(subtask);
        return subtask;
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public void deleteTask(int id) {
        tasks.remove(id);
    }

    @Override
    public void deleteSubtask(int subtaskId) {
        Subtask subtask = subtasks.remove(subtaskId);
        if (subtask != null) {
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.getSubtaskIds().remove((Integer) subtaskId);
                updateEpicStatus(epic.getId());
            }
            historyManager.remove(subtaskId);
        }
    }

    @Override
    public void deleteEpic(int epicId) {
        Epic epic = epics.remove(epicId);
        if (epic != null) {
            for (int subtaskId : epic.getSubtaskIds()) {
                subtasks.remove(subtaskId);
                historyManager.remove(subtaskId);
            }
            historyManager.remove(epicId);
        }
    }

    @Override
    public void createTask(Task task) {
        if (task == null) return;
        task.setId(generateId());
        tasks.put(task.getId(), task);
    }

    @Override
    public void createEpic(Epic epic) {
        if (epic == null) return;
        epic.setId(generateId());
        epics.put(epic.getId(), epic);
    }

    @Override
    public void createSubtask(Subtask subtask) {
        if (subtask == null) return;
        if (!epics.containsKey(subtask.getEpicId())) {
            throw new IllegalArgumentException("Epic with id " + subtask.getEpicId() + " not found");
        }
        subtask.setId(generateId());
        subtasks.put(subtask.getId(), subtask);
        Epic epic = epics.get(subtask.getEpicId());
        epic.getSubtaskIds().add(subtask.getId());
        updateEpicStatus(epic.getId());
    }

    @Override
    public void updateTask(Task task) {
        if (task == null) return;
        if (tasks.containsKey(task.getId())) {
            tasks.put(task.getId(), task);
            historyManager.add(task);
        }
    }

    @Override
    public void updateEpic(Epic updatedEpic) {
        if (updatedEpic == null || !epics.containsKey(updatedEpic.getId())) return;
        epics.put(updatedEpic.getId(), updatedEpic);
    }

    @Override
    public void updateSubtask(Subtask updatedSubtask) {
        if (updatedSubtask == null || !subtasks.containsKey(updatedSubtask.getId())) return;
        if (!epics.containsKey(updatedSubtask.getEpicId())) return;

        subtasks.put(updatedSubtask.getId(), updatedSubtask);
        updateEpicStatus(updatedSubtask.getEpicId());
    }

    public void updateEpicStatus(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) return;

        List<Integer> subtaskIds = epic.getSubtaskIds();
        if (subtaskIds.isEmpty()) {
            epic.setStatus(Task.Status.NEW);
            return;
        }

        boolean allDone = true;
        boolean anyInProgress = false;
        boolean partlyDone = false;

        for (int subtaskId : subtaskIds) {
            Subtask subtask = subtasks.get(subtaskId);
            if (subtask == null) continue;

            if (subtask.getStatus() != Task.Status.DONE) {
                allDone = false;
            }
            if (subtask.getStatus() == Task.Status.IN_PROGRESS) {
                anyInProgress = true;
                partlyDone = true;
            }
            if (subtask.getStatus() == Task.Status.DONE) {
                partlyDone = true;
            }
        }

        if (allDone) {
            epic.setStatus(Task.Status.DONE);
        } else if (anyInProgress || partlyDone) {
            epic.setStatus(Task.Status.IN_PROGRESS);
        } else {
            epic.setStatus(Task.Status.NEW);
        }
    }


}
