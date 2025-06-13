package tracker.model;

public class Subtask extends Task {
    private int epicID;

    public Subtask(String name, String description, int epicID) {
        super(name, description);
        this.epicID = epicID;
    }

    public int getEpicId() {
        return epicID;
    }

    @Override
    public Type getType() {
        return Type.SUBTASK;
    }
}
