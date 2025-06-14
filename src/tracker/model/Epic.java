package tracker.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private List<Integer> subtaskIds;
    private LocalDateTime endTime;

    public Epic(String name, String description) {
        super(name, description);
        this.subtaskIds = new ArrayList<>();
        this.duration = Duration.ZERO;
        this.startTime = null;
        this.endTime = null;
    }

    public void setSubtaskIds(List<Integer> subtaskIds) {
        this.subtaskIds = new ArrayList<>(subtaskIds);
    }

    public List<Integer> getSubtaskIds() {
        return new ArrayList<>(subtaskIds);
    }

    public void addSubtaskId(int subtaskId) {
        subtaskIds.add(subtaskId);
    }

    public void removeSubtaskId(int subtaskId) {
        subtaskIds.remove((Integer) subtaskId);
    }

    @Override
    public Type getType() {
        return Type.EPIC;
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void updateTimeFields(List<Subtask> subtasksList) {
        if (subtasksList == null || subtasksList.isEmpty()) {
            this.startTime = null;
            this.duration = Duration.ZERO;
            this.endTime = null;
            return;
        }

        LocalDateTime earliestStart = null;
        LocalDateTime latestEnd = null;
        Duration totalDuration = Duration.ZERO;
        boolean hasValidSubtasks = false;

        for (Subtask subtask : subtasksList) {
            if (subtask == null || subtask.getStartTime() == null) {
                continue;
            }

            hasValidSubtasks = true;

            if (earliestStart == null || subtask.getStartTime().isBefore(earliestStart)) {
                earliestStart = subtask.getStartTime();
            }

            LocalDateTime subtaskEnd = subtask.getEndTime();
            if (subtaskEnd != null && (latestEnd == null || subtaskEnd.isAfter(latestEnd))) {
                latestEnd = subtaskEnd;
            }

            totalDuration = totalDuration.plus(subtask.getDuration());
        }

        if (hasValidSubtasks) {
            this.startTime = earliestStart;
            this.duration = totalDuration;
            this.endTime = latestEnd;
        } else {
            this.startTime = null;
            this.duration = Duration.ZERO;
            this.endTime = null;
        }
    }
}