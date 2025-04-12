package tracker.controllers;

public class Managers {

    private Managers() {
    }

    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }
}