package tracker.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Epic extends Task {

    private List<Integer> subtaskIds;

    public Epic(String name, String description) {
        super(name, description);
        this.subtaskIds = new ArrayList<>();

    }

    public List<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    public void setSubtaskIds(List<Integer> subtaskIds) {
        this.subtaskIds = subtaskIds;
    }
}
