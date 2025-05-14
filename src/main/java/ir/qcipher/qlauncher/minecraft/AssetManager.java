package ir.qcipher.qlauncher.minecraft;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.qcipher.qlauncher.extra.Endpoints;
import ir.qcipher.qlauncher.utils.FileDownloader;
import ir.qcipher.qlauncher.utils.QLogger;
import ir.qcipher.qlauncher.utils.VersionUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class AssetManager {
    private final Logger logger = new QLogger().getLogger();
    private final ObjectMapper mapper = new ObjectMapper();

    private final Path parent;
    private final String version;
    private final String type;

    private Path assetIndexFile = null;
    private Consumer<String> progressCallback = null;

    public AssetManager(Path parent, String version, String type) {
        this.parent = parent;
        this.version = version;
        this.type = type;
    }

    public void setProgressCallback(Consumer<String> callback) {
        this.progressCallback = callback;
    }

    private void notifyProgress(String msg) {
        if (progressCallback != null) {
            progressCallback.accept(msg);
        }
    }

    public void downloadAssetIndexFile() throws IOException {
        Path assetIndexPath = VersionUtils.getVersionAssetIndexParent(parent, version, type);
        String assetIndexUrl = VersionUtils.getAssetIndexJsonURL(parent, version, type);
        String assetIndexId = VersionUtils.getAssetIndexId(parent, version, type);

        String downloadPath = assetIndexPath.resolve(assetIndexId + ".json").toString();
        logger.info(String.format("Getting asset index file from: (%s) to: (%s)", assetIndexUrl, downloadPath));

        assetIndexFile = FileDownloader.downloadFile(assetIndexUrl, downloadPath);
    }

    public void downloadAssetObjects() throws IOException {
        if (assetIndexFile == null) {
            logger.severe("Asset index file not correctly set. Re-download please.");
            return;
        }
        JsonNode objects = mapper.readTree(assetIndexFile.toFile()).path("objects");

        int total = objects.size();
        int current = 0;

        for (Iterator<Map.Entry<String, JsonNode>> it = objects.fields(); it.hasNext();) {
            Map.Entry<String, JsonNode> entry = it.next();

            String hash = entry.getValue().path("hash").asText();
            String sub = hash.substring(0, 2);
            Path assetObjectsPath = VersionUtils.getAssetObjectsPath(parent, version, type);
            Path assetPath = assetObjectsPath.resolve(sub).resolve(hash);

            notifyProgress(String.format("Downloading assets...(%s/%s)", current, total));
            logger.info(String.format("Getting asset object; hash: (%s); save path: (%s)", hash, assetPath));

            FileDownloader.downloadFile(String.format(Endpoints.ASSET_LIBRARY_FORMAT, sub, hash), assetPath.toString());

            current++;
        }
    }
}
