package ir.qcipher.qlauncher.dialogs;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;

public class ErrorDialog {
    public static void show(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText("An error occurred");
        alert.setContentText(message);
        alert.getButtonTypes().setAll(ButtonType.OK);
        alert.showAndWait();
    }

    public static void show(Throwable throwable) {
        show("Error", throwable.getMessage() != null ? throwable.getMessage() : throwable.toString());
    }
}
