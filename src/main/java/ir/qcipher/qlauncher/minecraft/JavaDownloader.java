package ir.qcipher.qlauncher.minecraft;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.qcipher.qlauncher.extra.Endpoints;
import ir.qcipher.qlauncher.utils.FileDownloader;

import java.io.IOException;
import java.nio.file.Path;

public class JavaDownloader{
    private final ObjectMapper mapper = new ObjectMapper();

    public void downloadJre(Path parent, String majorVersion) throws IOException {
        Path manifest = loadJavaVersionsManifest(parent, majorVersion);

        // todo : download java zip
        // todo : then extract it
        // todo : then save it's path in launcher.json
        // todo : then use it when launching minecraft
        // todo : so we will need a verifier class to
        // todo : verify the items like libraries, assets, jars, natives, java versions, etc
    }

    private Path loadJavaVersionsManifest(Path parent, String version) throws IOException {
        String downloadUrl = Endpoints.JAVA_DOWNLOADS.formatted(version);
        Path savePath = parent.resolve("jre").resolve(version);

        return FileDownloader.downloadFile(downloadUrl, savePath.toString());
    }
}
