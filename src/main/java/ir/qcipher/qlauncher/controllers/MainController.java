package ir.qcipher.qlauncher.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.qcipher.qlauncher.Launcher;
import ir.qcipher.qlauncher.Main;
import ir.qcipher.qlauncher.dialogs.ErrorDialog;
import ir.qcipher.qlauncher.extra.VersionTypes;
import ir.qcipher.qlauncher.extra.jsonOBJ.LauncherConfigJson;
import ir.qcipher.qlauncher.minecraft.Initializer;
import ir.qcipher.qlauncher.utils.VersionUtils;
import ir.qcipher.qlauncher.windows.QLauncherGUI;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainController {
    private final ObjectMapper mapper = new ObjectMapper();

    @FXML
    private ImageView launcherIcon;

    @FXML
    private Button playButton;

    @FXML
    private ComboBox<String> userSelector;

    @FXML
    private ComboBox<String> versionSelector;

    @FXML
    public void initialize() throws IOException {
        launcherIcon.setImage(new Image(Objects.requireNonNull(getClass().getResource("/img/QLauncher.png")).toString()));

        userSelector.setPlaceholder(new Label("Empty"));
        versionSelector.setPlaceholder(new Label("Empty"));

        Path p = Initializer.getLauncherPathContext().path();
        refreshUsers(p);
        refreshVersions(p);
    }

    public void refreshUsers(Path parent) throws IOException {
        Path configJson = parent.resolve("launcher.json");
        LauncherConfigJson config = mapper.readValue(configJson.toFile(), LauncherConfigJson.class);

        userSelector.getItems().setAll(
                config.users.stream().map(user -> user.name).toList()
        );
    }

    public void refreshVersions(Path parent) {
        List<String> releases = VersionUtils.extractInstalledReleases(parent);
        List<String> snapshots = VersionUtils.extractInstalledSnapshots(parent);

        List<String> finals = new ArrayList<>();
        if (!releases.isEmpty()) finals.addAll(releases);
        if (!snapshots.isEmpty()) finals.addAll(snapshots);

        versionSelector.getItems().setAll(finals);
    }


    @FXML
    public void handlePlayButton() throws IOException {
        String selectedVersion = versionSelector.getSelectionModel().getSelectedItem();
        String selectedUser = userSelector.getSelectionModel().getSelectedItem();

        if (selectedUser == null) {
            Platform.runLater(() -> ErrorDialog.show("Select a user", "Select a user to launch with."));
        } else if (selectedVersion == null) {
            Platform.runLater(() -> ErrorDialog.show("Select a version", "Select a version to launch."));
        } else {
            String javaPath = "C:\\Program Files\\Java\\jdk-1.8\\bin\\java.exe"; // todo

            Launcher.launch(
                    selectedUser,
                    javaPath,
                    selectedVersion,
                    "8",
                    "8",
                    VersionTypes.RELEASE, // todo
                    Initializer.getLauncherPathContext().path(),
                    false
            );
        }
    }

    @FXML
    public void openInstances() throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(Main.class.getResource("/instances.fxml")));
        Scene scene = new Scene(root);

        scene.getStylesheets().addAll(Objects.requireNonNull(Main.class.getResource("/styles/fonts.css")).toString(),
                Objects.requireNonNull(Main.class.getResource("/styles/main.css")).toString());

        Stage s = new Stage();
        s.setScene(scene);
        s.setTitle("Instances");
        s.setResizable(false);
        s.initModality(Modality.APPLICATION_MODAL);
        s.getIcons().add(QLauncherGUI.getIconImage());
        s.showAndWait();

        refreshVersions(Initializer.getLauncherPathContext().path());
    }

    @FXML
    public void openUserManager() throws IOException {
        Scene scene = new Scene(
                FXMLLoader.load(Objects.requireNonNull(Main.class.getResource("/users.fxml")))
        );
        scene.getStylesheets().addAll(Objects.requireNonNull(Main.class.getResource("/styles/fonts.css")).toString(),
                Objects.requireNonNull(getClass().getResource("/styles/main.css")).toString());

        Stage s = new Stage();
        s.setScene(scene);
        s.setTitle("Instances");
        s.setResizable(false);
        s.initModality(Modality.APPLICATION_MODAL);
        s.getIcons().add(QLauncherGUI.getIconImage());
        s.showAndWait();

        refreshUsers(Initializer.getLauncherPathContext().path());
    }
}
