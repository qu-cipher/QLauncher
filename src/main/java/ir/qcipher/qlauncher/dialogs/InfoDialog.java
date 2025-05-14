package ir.qcipher.qlauncher.dialogs;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class InfoDialog {
    public static void show(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText("Info");
        alert.setContentText(message);
        alert.getButtonTypes().setAll(ButtonType.OK);
        alert.showAndWait();
    }
}
