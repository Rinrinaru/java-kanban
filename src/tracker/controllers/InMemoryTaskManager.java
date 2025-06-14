package tracker.controllers;

import tracker.exceptions.TimeConflictException;
import tracker.model.Epic;
import tracker.model.Subtask;
import tracker.model.Task;
import tracker.model.Task.Status;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {
    protected final Map<Integer, Task> tasks = new HashMap<>();
    protected final Map<Integer, Epic> epics = new HashMap<>();
    protected final Map<Integer, Subtask> subtasks = new HashMap<>();
    protected int nextId = 1;
    private final HistoryManager historyManager;
    private final Set<Task> prioritizedTasks = new TreeSet<>(Comparator.comparing(Task::getStartTime, Comparator.nullsLast(Comparator.naturalOrder())));

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
        tasks.keySet().forEach(historyManager::remove);
        tasks.keySet().forEach(prioritizedTasks::remove);
        tasks.clear();
    }

    @Override
    public void deleteAllEpics() {
        epics.values().stream().flatMap(epic -> epic.getSubtaskIds().stream()).forEach(id -> {
            historyManager.remove(id);
            prioritizedTasks.removeIf(task -> task.getId() == id);
        });
        epics.keySet().forEach(historyManager::remove);
        epics.clear();
        subtasks.clear();
    }

    @Override
    public void deleteAllSubtasks() {
        subtasks.keySet().forEach(id -> {
            historyManager.remove(id);
            prioritizedTasks.removeIf(task -> task.getId() == id);
        });
        subtasks.clear();
        epics.values().forEach(epic -> {
            epic.getSubtaskIds().clear();
            updateEpicStatus(epic.getId());
            updateEpicTimeFields(epic.getId());
        });
    }

    @Override
    public Task getTask(int id) {
        Task task = tasks.get(id);
        if (task != null) historyManager.add(task);
        return task;
    }

    @Override
    public Epic getEpic(int id) {
        Epic epic = epics.get(id);
        if (epic != null) historyManager.add(epic);
        return epic;
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) historyManager.add(subtask);
        return subtask;
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public void deleteTask(int id) {
        prioritizedTasks.removeIf(task -> task.getId() == id);
        tasks.remove(id);
        historyManager.remove(id);
    }

    @Override
    public void deleteSubtask(int subtaskId) {
        Subtask subtask = subtasks.remove(subtaskId);
        if (subtask != null) {
            prioritizedTasks.removeIf(task -> task.getId() == subtaskId);
            historyManager.remove(subtaskId);
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.removeSubtaskId(subtaskId);
                updateEpicStatus(epic.getId());
                updateEpicTimeFields(epic.getId());
            }
        }
    }

    @Override
    public void deleteEpic(int epicId) {
        Epic epic = epics.remove(epicId);
        if (epic != null) {
            epic.getSubtaskIds().forEach(id -> {
                subtasks.remove(id);
                prioritizedTasks.removeIf(task -> task.getId() == id);
                historyManager.remove(id);
            });
            historyManager.remove(epicId);
        }
    }

    @Override
    public void createTask(Task task) {
        if (task == null) return;
        validateTaskTime(task);
        task.setId(generateId());
        tasks.put(task.getId(), task);
        addToPrioritized(task);
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
        validateTaskTime(subtask);
        subtask.setId(generateId());
        subtasks.put(subtask.getId(), subtask);
        Epic epic = epics.get(subtask.getEpicId());
        epic.addSubtaskId(subtask.getId());
        updateEpicStatus(epic.getId());
        updateEpicTimeFields(epic.getId());
        addToPrioritized(subtask);
    }

    @Override
    public void updateTask(Task task) {
        if (task == null || !tasks.containsKey(task.getId())) return;
        validateTaskTime(task);
        prioritizedTasks.removeIf(t -> t.getId() == task.getId());
        tasks.put(task.getId(), task);
        addToPrioritized(task);
        historyManager.add(task);
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
        updateEpicStatus(updatedSubtask.getEpicId()); // Автоматическое обновление статуса
        updateEpicTimeFields(updatedSubtask.getEpicId());
    }

    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    public List<Subtask> getSubtasksByEpic(int epicId) {
        return epics.getOrDefault(epicId, new Epic("", "")).getSubtaskIds().stream().map(subtasks::get).filter(Objects::nonNull).collect(Collectors.toList());
    }

    protected void updateEpicStatus(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) return;

        List<Subtask> subtasksList = getSubtasksByEpic(epicId);
        if (subtasksList.isEmpty()) {
            epic.setStatus(Status.NEW);
            return;
        }

        boolean allNew = subtasksList.stream().allMatch(subtask -> subtask.getStatus() == Status.NEW);

        boolean allDone = subtasksList.stream().allMatch(subtask -> subtask.getStatus() == Status.DONE);

        boolean anyInProgress = subtasksList.stream().anyMatch(subtask -> subtask.getStatus() == Status.IN_PROGRESS);

        boolean hasNewAndDone = subtasksList.stream().anyMatch(subtask -> subtask.getStatus() == Status.NEW) && subtasksList.stream().anyMatch(subtask -> subtask.getStatus() == Status.DONE);

        if (allDone) {
            epic.setStatus(Status.DONE);
        } else if (allNew) {
            epic.setStatus(Status.NEW);
        } else if (anyInProgress || hasNewAndDone) {
            epic.setStatus(Status.IN_PROGRESS);
        }
    }

    private void updateEpicTimeFields(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic != null) {
            List<Subtask> subtasksList = getSubtasksByEpic(epicId);
            epic.updateTimeFields(subtasksList);
        }
    }

    private void addToPrioritized(Task task) {
        Optional.ofNullable(task.getStartTime()).ifPresent(time -> prioritizedTasks.add(task));
    }

    private void validateTaskTime(Task newTask) {
        Optional.ofNullable(newTask.getStartTime()).ifPresent(startTime -> {
            boolean hasOverlap = prioritizedTasks.stream().filter(task -> task.getId() != newTask.getId()).anyMatch(existingTask -> isTimeOverlap(newTask, existingTask));

            if (hasOverlap) {
                throw new TimeConflictException("Новая задача пересекается по времени с существующей");
            }
        });
    }

    private boolean isTimeOverlap(Task task1, Task task2) {
        LocalDateTime start1 = task1.getStartTime();
        LocalDateTime end1 = task1.getEndTime();
        LocalDateTime start2 = task2.getStartTime();
        LocalDateTime end2 = task2.getEndTime();

        return start1.isBefore(end2) && start2.isBefore(end1);
    }
}

