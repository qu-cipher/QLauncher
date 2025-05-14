package ir.qcipher.qlauncher.windows;

import ir.qcipher.qlauncher.utils.TaskIndicator;
import javafx.concurrent.Task;

public class TaskManagerWindow {
    private final TaskIndicator indicator = new TaskIndicator();

    public void runTask(Task<Boolean> task) {
        indicator.showWithTask(task);
    }

    public void runNamedTask(String name, Runnable logic) {
        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                updateMessage(name);
                logic.run();
                return null;
            }
        };
        runTask(task);
    }
}
