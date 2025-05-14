package ir.qcipher.qlauncher.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.qcipher.qlauncher.dialogs.ErrorDialog;
import ir.qcipher.qlauncher.dialogs.InfoDialog;
import ir.qcipher.qlauncher.extra.jsonOBJ.LauncherConfigJson;
import ir.qcipher.qlauncher.extra.jsonOBJ.User;
import ir.qcipher.qlauncher.minecraft.Initializer;
import ir.qcipher.qlauncher.utils.LauncherConfigUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

public class UserManagerController {
    private final ObjectMapper mapper = new ObjectMapper();
    private Path launcherPath;

    @FXML
    private ListView<String> userList;

    @FXML
    public Button add;

    @FXML
    public Button remove;

    @FXML
    public void initialize() {
        launcherPath = Initializer.getLauncherPathContext().path();
        buttonToggle();
        try {
            updateUsers();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @FXML
    public void addUser() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add User");
        dialog.setHeaderText("Add a New Username");
        dialog.setContentText("Enter username:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            String trimmed = name.trim();
            if (trimmed.isEmpty()) {
                InfoDialog.show("Invalid Input", "Username cannot be empty.");
                return;
            }

            try {
                Path configFile = launcherPath.resolve("launcher.json");
                LauncherConfigJson config = mapper.readValue(configFile.toFile(), LauncherConfigJson.class);

                boolean exists = config.getUsers().stream()
                        .anyMatch(user -> user.name.equalsIgnoreCase(trimmed));

                if (exists) {
                    InfoDialog.show("Duplicate", "This username already exists.");
                    return;
                }

                User user = new User();
                user.setName(trimmed);
                config.getUsers().add(user);
                mapper.writeValue(configFile.toFile(), config);
                refreshList();

            } catch (IOException e) {
                ErrorDialog.show(e);
            }
        });
    }
    @FXML
    public void removeUserFromList() throws IOException {
        String selection = userList.getSelectionModel().getSelectedItem();

        if (selection == null || selection.isBlank()) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Info");
        confirm.setHeaderText("Confirmation");
        confirm.setContentText("Are you sure you want to remove this username?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean removed = LauncherConfigUtils.removeUser(
                    launcherPath.resolve("launcher.json"), selection
            );
            if (removed) {
                refreshList();
            }
        }
    }

    private void buttonToggle() {
        userList.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    remove.setDisable(newValue == null);
                }
        );
    }

    private void updateUsers() throws IOException {
        userList.getItems().clear();
        Path configFile = launcherPath.resolve("launcher.json");
        LauncherConfigJson config = mapper.readValue(configFile.toFile(), LauncherConfigJson.class);

        for (User user : config.getUsers()) {
            userList.getItems().add(user.name);
        }
    }


    private void refreshList() throws IOException {
        remove.setDisable(true);
        updateUsers();
        userList.getSelectionModel().clearSelection();
        userList.refresh();
    }
}
