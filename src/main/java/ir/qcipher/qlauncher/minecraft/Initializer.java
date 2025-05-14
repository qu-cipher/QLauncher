package ir.qcipher.qlauncher.minecraft;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.qcipher.qlauncher.exceptions.WebNotReachable;
import ir.qcipher.qlauncher.extra.Endpoints;
import ir.qcipher.qlauncher.extra.jsonOBJ.Theme;
import ir.qcipher.qlauncher.extra.jsonOBJ.User;
import ir.qcipher.qlauncher.extra.records.LauncherParentPath;
import ir.qcipher.qlauncher.extra.jsonOBJ.LauncherConfigJson;
import ir.qcipher.qlauncher.extra.jsonOBJ.VersionManifest;
import ir.qcipher.qlauncher.utils.FileDownloader;

/**
 * The <code>Initializer</code> class
 * initializes the launcher,
 * by loading the parent path, loading configuration and more.
 */
public class Initializer {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static LauncherParentPath launcherParentPathRecord;

    private Path launcherParentPath;
    private Path launcherJson;

    public Initializer(Path launcherParentPath) throws IOException, WebNotReachable {
        this.launcherParentPath = launcherParentPath;
        this.launcherJson = launcherParentPath.resolve("launcher.json");

        // ---- initial checks
        boolean ifLauncherFolderExists = Files.exists(this.launcherParentPath);
        if (!ifLauncherFolderExists) initializeLauncherFolders(this.launcherParentPath);

        if (!isLauncherConfigValid(this.launcherParentPath)) createDefaultLauncherConfig(this.launcherParentPath);

        // ---- versions
        if (isWebReachable()) {
            if (!isVersionManifestJsonValid(this.launcherParentPath)) {
                initializeVersionManifest(this.launcherParentPath);
            }
        }

        // Records
        launcherParentPathRecord = new LauncherParentPath(this.launcherParentPath);
    }

    /**
     * Returns the path of launcher, set in runtime.
     * @return {@link LauncherParentPath}
     */
    public static LauncherParentPath getLauncherPathContext() {
        return launcherParentPathRecord;
    }

    /**
     * Creates important folders for launcher
     * @param parent launcher path
     * @throws IOException
     */
    private void initializeLauncherFolders(Path parent) throws IOException {
        String[] folders = new String[]{
                "config",
                "instances"
        };

        // make sure parent exists
        Files.createDirectories(parent);

        // create initial folders
        for (String folderName : folders) {
            Files.createDirectories(parent.resolve(folderName));
        }
    }

    /**
     * Creates <code>launcher.json</code> with default data at provided launcher parent
     * @param parent launcher path
     * @throws IOException
     */
    private void createDefaultLauncherConfig(Path parent) throws IOException {
        // we have already checked the existence of parent above,
        // so we don't need to do the same thing again.

        // 1. json file loading
        Path configPath = parent.resolve("launcher.json");
        File config = configPath.toFile();

        // 2. constructing data
        List<Theme> themes = new ArrayList<>();
        List<User> users = new ArrayList<>();

        Theme nordDarkTheme = new Theme();
        nordDarkTheme.setName("nord-dark");
        nordDarkTheme.setPath("%RESOURCES%/themes/nord-dark.css");
        themes.add(nordDarkTheme);

        LauncherConfigJson launcherConfigJson = new LauncherConfigJson(themes, users);

        // 3. writing data
        mapper.writerWithDefaultPrettyPrinter().writeValue(config, launcherConfigJson);
    }

    /**
     * Checks if the <code>launcher.json</code> file is valid or not
     * @param parent launcher path
     * @return
     */
    private boolean isLauncherConfigValid(Path parent) {
        File configFile = parent.resolve("launcher.json").toFile(); // config file

        if (!configFile.exists()
            || configFile.length() == 0) return false; // doesn't exist at all

        try {
            LauncherConfigJson config = mapper.readValue(configFile, LauncherConfigJson.class);

            return config.themes != null && !config.themes.isEmpty()
                    && config.users != null && !config.users.isEmpty(); // is valid ?
        } catch (IOException e) {
            return false;
        }
    }

    private boolean isWebReachable() {
        try {
            URL url = new URL(Endpoints.VERSION_MANIFEST_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(1500);
            connection.setReadTimeout(1500);
            int responseCode = connection.getResponseCode();
            return (200 <= responseCode && responseCode <= 399);
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Checks if the <code>v_manifest.json</code> file is valid or not
     * @param parent launcher path
     * @return
     */
    private boolean isVersionManifestJsonValid(Path parent) {
        File manifestJson = parent
                .resolve("config")
                .resolve("v_manifest.json").toFile();

        if (!manifestJson.exists() || manifestJson.length() == 0) return false; // doesn't exist at all

        ObjectMapper mapper = new ObjectMapper();
        try {
            VersionManifest manifest = mapper.readValue(manifestJson, VersionManifest.class);

            if (manifest.latestVersion == null
                    || manifest.versionsLists == null
                    || manifest.versionsLists.isEmpty()) {
                return false;
            }

            boolean latestValid = manifest.latestVersion.release != null && !manifest.latestVersion.release.isBlank()
                    && manifest.latestVersion.snapshot != null && !manifest.latestVersion.snapshot.isBlank();

            boolean versionsValid = manifest.versionsLists.stream().allMatch(v ->
                    v.id != null && !v.id.isBlank()
                            && v.type != null && !v.type.isBlank()
                            && v.url != null && !v.url.isBlank()
                            && v.time != null && !v.time.isBlank()
                            && v.releaseTime != null && !v.releaseTime.isBlank()
            );

            return latestValid && versionsValid;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Downloads the <code>v_manifest.json</code> file and saves it
     * @param parent launcher path
     * @throws IOException
     */
    private void initializeVersionManifest(Path parent) throws IOException {
        FileDownloader.downloadFile(
                Endpoints.VERSION_MANIFEST_URL,
                parent.resolve("config").resolve("v_manifest.json").toString()
        );
    }
}
