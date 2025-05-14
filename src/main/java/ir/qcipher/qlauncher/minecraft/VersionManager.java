package ir.qcipher.qlauncher.minecraft;

import ir.qcipher.qlauncher.utils.FileDownloader;
import ir.qcipher.qlauncher.utils.QLogger;
import ir.qcipher.qlauncher.utils.VersionUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Logger;

public class VersionManager {
    private final Logger logger = new QLogger().getLogger();
    private Path launcherParent;
    private String versionId;
    private String type;

    public VersionManager(Path launcherParent, String versionId, String type) {
        this.launcherParent = launcherParent;
        this.versionId = versionId;
        this.type = type;
    }

    public void installVersionInfoJson() throws IOException {
        String downloadURL = VersionUtils.getVersionInfoJsonURL(launcherParent, versionId);
        logger.info("Getting version info json from: " + downloadURL);

        FileDownloader.downloadFile(downloadURL, VersionUtils.getVersionJsonFilePath(launcherParent, versionId, type).toString());
    }

    public void installVersionJar() throws IOException {
        String downloadURL = VersionUtils.getVersionJarURL(launcherParent, versionId, type);
        logger.info("Getting version jar from: " + downloadURL);

        FileDownloader.downloadFile(downloadURL, VersionUtils.getVersionJarFilePath(launcherParent, versionId, type).toString());
    }
}
