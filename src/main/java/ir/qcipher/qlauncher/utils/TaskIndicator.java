package ir.qcipher.qlauncher.utils;

import ir.qcipher.qlauncher.extra.Delta;
import ir.qcipher.qlauncher.windows.QLauncherGUI;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class TaskIndicator {
    private final Stage stage;
    private final Label loadingText;

    public TaskIndicator() {
        stage = new Stage(StageStyle.UNDECORATED);

        VBox layout = new VBox(15);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(30));

        ProgressIndicator spinner = new ProgressIndicator();
        loadingText = new Label();
        loadingText.setStyle("-fx-font-size: 15px; -fx-text-fill: #dddddd;");

        layout.setStyle("-fx-background-color: #2e3440;");
        layout.getChildren().addAll(spinner, loadingText);

        // make it draggable
        Delta dragDelta = new Delta();
        layout.setOnMousePressed(event -> {
            dragDelta.x = event.getSceneX();
            dragDelta.y = event.getSceneY();
        });
        layout.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - dragDelta.x);
            stage.setY(event.getScreenY() - dragDelta.y);
        });

        stage.setScene(new Scene(layout, 300, 300));

        // center the splash
        Rectangle2D sb = Screen.getPrimary().getVisualBounds();
        stage.setX((sb.getWidth() - 300) / 2);
        stage.setY((sb.getHeight() - 300) / 2);
        stage.getIcons().add(QLauncherGUI.getIconImage());
        stage.initModality(Modality.APPLICATION_MODAL);
    }

    public void showWithTask(Task<Boolean> task) {
        loadingText.textProperty().bind(task.messageProperty());

        task.setOnSucceeded(e -> close());
        task.setOnFailed(e -> {
            close();
            task.getException().printStackTrace(); // or show an error dialog
        });

        stage.show();
        new Thread(task).start();
    }

    public void close() {
        Platform.runLater(stage::close);
    }
}
