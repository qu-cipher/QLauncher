package ir.qcipher.qlauncher.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import ir.qcipher.qlauncher.dialogs.ErrorDialog;
import ir.qcipher.qlauncher.dialogs.InfoDialog;
import ir.qcipher.qlauncher.minecraft.AssetManager;
import ir.qcipher.qlauncher.minecraft.Initializer;
import ir.qcipher.qlauncher.minecraft.LibraryManager;
import ir.qcipher.qlauncher.minecraft.VersionManager;
import ir.qcipher.qlauncher.utils.VersionUtils;
import ir.qcipher.qlauncher.utils.TaskIndicator;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class InstanceManagerController {
    // Tabs
    @FXML
    private TabPane tabPane;

    // ListViews
    @FXML
    private ListView<String> releases;

    @FXML
    private ListView<String> snapshots;

    @FXML
    private ListView<String> other;

    // Buttons
    @FXML
    private Button install;

    @FXML
    private Button fix;

    @FXML
    private Button remove;

    // installed
    private List<String> installedReleases;
    private List<String> installedSnapshots;
    private List<String> installedAlphaBeta;

    @FXML
    public void initialize() throws IOException {
        Path launcherPath = Initializer.getLauncherPathContext().path();
        updateLists(launcherPath);

        install.setOnAction((event) -> {
            String versionType = tabPane.getSelectionModel().getSelectedItem().getText();
            ListView<String> activeList = switch (versionType.toLowerCase()) {
                case "release" -> releases;
                case "snapshot" -> snapshots;
                default -> other;
            };
            Task<Boolean> downloadTask = getDownloadTask(activeList, launcherPath, versionType);

            TaskIndicator task = new TaskIndicator();
            task.showWithTask(downloadTask);
        });

        remove.setOnAction((event) -> {
            String versionType = tabPane.getSelectionModel().getSelectedItem().getText();
            ListView<String> activeList = switch (versionType.toLowerCase()) {
                case "release" -> releases;
                case "snapshot" -> snapshots;
                default -> other;
            };
            Task<Boolean> removeTask = getRemoveTask(activeList, launcherPath, versionType);

            TaskIndicator task = new TaskIndicator();
            task.showWithTask(removeTask);

            try {
                updateLists(launcherPath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        buttonToggle(releases, installedReleases);
        buttonToggle(snapshots, installedSnapshots);
        buttonToggle(other, installedAlphaBeta);
    }

    private Task<Boolean> getRemoveTask(ListView<String> activeList, Path launcherPath, String versionType) {
        String versionId = activeList.getSelectionModel().getSelectedItem();

        Task<Boolean> removeTask = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                updateMessage("Removing version " + versionId);
                VersionUtils.removeVersion(launcherPath, versionId, versionType);

                updateMessage("Done");
                Thread.sleep(1000);

                Platform.runLater(() -> {
                    try {
                        refreshLists(activeList, launcherPath);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
                return true;
            }
        };
        return removeTask;
    }

    private void refreshLists(ListView<String> activeList, Path launcherPath) throws IOException {
        install.setDisable(true);
        fix.setDisable(true);
        remove.setDisable(true);

        activeList.getSelectionModel().clearSelection();
        updateLists(launcherPath);
    }

    private Task<Boolean> getDownloadTask(ListView<String> activeList, Path launcherPath, String versionType) {
        String versionId = activeList.getSelectionModel().getSelectedItem();

        VersionManager vm = new VersionManager(launcherPath, versionId, versionType);
        AssetManager am = new AssetManager(launcherPath, versionId, versionType);
        LibraryManager lm = new LibraryManager(launcherPath, versionId, versionType);

        Task<Boolean> downloadTask = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                try {
                    updateMessage("Downloading %s.json...".formatted(versionId));
                    vm.installVersionInfoJson();

                    updateMessage("Downloading %s.jar...".formatted(versionId));
                    vm.installVersionJar();

                    updateMessage("Downloading libraries...");
                    lm.setProgressCallback(this::updateMessage);
                    lm.handleLibraries();
                    lm.extractNatives();

                    updateMessage("Downloading asset indexes...");
                    am.downloadAssetIndexFile();

                    updateMessage("Downloading assets...");
                    am.setProgressCallback(this::updateMessage);
                    am.downloadAssetObjects();

                    Platform.runLater(() -> {
                        InfoDialog.show("Success", "Version installed successfully!");

                        Window window = install.getScene().getWindow();
                        if (window instanceof Stage stage) {
                            stage.close();
                        }
                    });
                } catch (IOException e) {
                    Platform.runLater(() -> ErrorDialog.show(e));
                }
                return true;
            }
        };

        downloadTask.setOnFailed(ev -> {
            Throwable ex = downloadTask.getException();
            ErrorDialog.show(ex);
        });

        return downloadTask;
    }

    private void updateLists(Path parent) throws IOException {
        Path vManifest = parent.resolve("config").resolve("v_manifest.json");

        List<String> jsonReleases = new ArrayList<>();
        List<String> jsonSnapshots = new ArrayList<>();
        List<String> jsonOther = new ArrayList<>();

        installedReleases = VersionUtils.extractInstalledReleases(parent);
        installedSnapshots = VersionUtils.extractInstalledSnapshots(parent);
        installedAlphaBeta = VersionUtils.extractInstalledAlphaBeta(parent);

        ObjectMapper mapper = new ObjectMapper();
        ArrayNode allVersions = (ArrayNode) mapper.readTree(vManifest.toFile()).path("versions");

        for (JsonNode version : allVersions) {
            String versionId = version.path("id").asText();
            String versionType = version.path("type").asText();

            switch (versionType) {
                case "release": jsonReleases.add(versionId); break;
                case "snapshot": jsonSnapshots.add(versionId); break;
                default: jsonOther.add(versionId); break;
            }
        }

        releases.getItems().setAll(jsonReleases);
        snapshots.getItems().setAll(jsonSnapshots);
        other.getItems().setAll(jsonOther);
    }

    private void buttonToggle(ListView<String> listView, List<String> installed) {
        listView.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    if (newValue == null) return;
                    if (installed.contains(newValue)) {
                        install.setDisable(true);
                        fix.setDisable(true);
                        remove.setDisable(false);
                    } else {
                        install.setDisable(false);
                        fix.setDisable(true);
                        remove.setDisable(true);
                    }
                });
    }
}
