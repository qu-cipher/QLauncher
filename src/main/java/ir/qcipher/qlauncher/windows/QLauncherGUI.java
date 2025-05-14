package ir.qcipher.qlauncher.windows;

import ir.qcipher.qlauncher.Main;
import ir.qcipher.qlauncher.dialogs.ErrorDialog;
import ir.qcipher.qlauncher.minecraft.Initializer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

public class QLauncherGUI extends Application {
    @Override
    public void start(Stage s) {
        Application.setUserAgentStylesheet(Objects.requireNonNull(Main.class.getResource("/themes/nord-dark.css")).toString());

        Stage splash = initializationWindow();
        splash.show();

        new Thread(() -> {
            try {
                new Initializer(Path.of("QLauncher"));

                Platform.runLater(() -> {
                    splash.close();
                    showApp(s);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    splash.close();
                    ErrorDialog.show(e);
                });
            }
        }).start();
    }

    private Stage initializationWindow() {
        Stage stage = new Stage(StageStyle.UNDECORATED);

        VBox layout = new VBox(15);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(30));

        ProgressIndicator spinner = new ProgressIndicator();
        Label loadingText = new Label("Initializing...");
        loadingText.setStyle("-fx-font-size: 20px; -fx-text-fill: #dddddd;");

        layout.setStyle("-fx-background-color: #2e3440;");
        layout.getChildren().addAll(spinner, loadingText);

        Scene scene = new Scene(layout, 300, 300);
        stage.setScene(scene);

        // center the splash
        Rectangle2D sb = Screen.getPrimary().getVisualBounds();
        stage.setX((sb.getWidth() - 300) / 2);
        stage.setY((sb.getHeight() - 300) / 2);
        stage.getIcons().add(getIconImage());

        return stage;
    }


    private void showApp(Stage s) {
        Parent root = null;
        try {
            root = FXMLLoader.load(Objects.requireNonNull(Main.class.getResource("/main.fxml")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Scene scene = new Scene(root);

        scene.getStylesheets().addAll(Objects.requireNonNull(Main.class.getResource("/styles/fonts.css")).toString(),
                Objects.requireNonNull(Main.class.getResource("/styles/main.css")).toString());

        s.setResizable(false);
        s.setOnCloseRequest(e -> s.close());
        s.setTitle("QLauncher");
        s.setScene(scene);
        s.getIcons().add(getIconImage());
        s.show();
    }

    public static Image getIconImage() {
        return (new Image(Objects.requireNonNull(Main.class.getResource("/img/logo.png")).toString()));
    }
}
