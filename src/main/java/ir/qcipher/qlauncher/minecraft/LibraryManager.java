package ir.qcipher.qlauncher.minecraft;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.qcipher.qlauncher.extra.jsonOBJ.libraries.Artifact;
import ir.qcipher.qlauncher.extra.jsonOBJ.libraries.Library;
import ir.qcipher.qlauncher.extra.jsonOBJ.libraries.MinecraftLibraries;
import ir.qcipher.qlauncher.utils.FileDownloader;
import ir.qcipher.qlauncher.utils.OS;
import ir.qcipher.qlauncher.utils.VersionUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class LibraryManager {
    private final Path launcherParent;
    private final String versionId;
    private final String versionType;

    private final ObjectMapper mapper = new ObjectMapper();
    private Consumer<String> downloadProgress;

    public void setProgressCallback(Consumer<String> callback) {
        this.downloadProgress = callback;
    }

    private void notifyProgress(String msg) {
        if (downloadProgress != null) {
            downloadProgress.accept(msg);
        }
    }

    public LibraryManager(Path launcherParent, String versionId, String versionType) {
        this.launcherParent = launcherParent;
        this.versionId = versionId;
        this.versionType = versionType;
    }

    public void handleLibraries() throws IOException {
        MinecraftLibraries versionLibraries = mapper.readValue(
                VersionUtils.getVersionJsonFilePath(launcherParent, versionId, versionType).toFile(),
                MinecraftLibraries.class);

        int total = versionLibraries.libraries.size();
        int current = 0;

        for (Library library : versionLibraries.libraries) {
            if (library.downloads != null) {
                notifyProgress(String.format("Downloading libraries... (%s/%s)", current, total));

                Artifact artifact = library.downloads.artifact;
                if (artifact != null) {
                    String url = artifact.url;
                    Path destination = VersionUtils.getLibrariesParent(launcherParent, versionId, versionType)
                            .resolve(artifact.path);
                    FileDownloader.downloadFile(url, destination.toString());
                }

                if (library.downloads.classifiers != null) {
                    String osKey = OS.getOsName();
                    if (library.natives != null && library.natives.containsKey(osKey)) {
                        String classifierKey = library.natives.get(osKey);
                        Artifact nativeArtifact = library.downloads.classifiers.get(classifierKey);
                        if (nativeArtifact != null) {
                            String nativeUrl = nativeArtifact.url;
                            Path nativeDest = VersionUtils.getLibrariesParent(launcherParent, versionId, versionType)
                                    .resolve(nativeArtifact.path);
                            FileDownloader.downloadFile(nativeUrl, nativeDest.toString());
                        }
                    }
                }
            }

            current++;
        }
    }

    public void extractNatives() throws IOException {
        notifyProgress("Extracting native libraries...");

        MinecraftLibraries versionLibraries = mapper.readValue(
                VersionUtils.getVersionJsonFilePath(launcherParent, versionId, versionType).toFile(),
                MinecraftLibraries.class);

        String osKey = OS.getOsName();
        Path nativesDir = VersionUtils.getNativesParent(launcherParent, versionId, versionType);
        Files.createDirectories(nativesDir);

        int total = versionLibraries.libraries.size();
        int current = 0;

        for (Library library : versionLibraries.libraries) {
            notifyProgress(String.format("Extracting natives... (%s/%s)", current, total));

            if (library.downloads != null && library.downloads.classifiers != null &&
                    library.natives != null && library.natives.containsKey(osKey)) {

                String classifierKey = library.natives.get(osKey);
                Artifact nativeArtifact = library.downloads.classifiers.get(classifierKey);

                if (nativeArtifact != null) {
                    Path nativeJar = VersionUtils.getLibrariesParent(launcherParent, versionId, versionType)
                            .resolve(nativeArtifact.path);

                    try (ZipInputStream zipIn = new ZipInputStream(Files.newInputStream(nativeJar))) {
                        ZipEntry entry;
                        while ((entry = zipIn.getNextEntry()) != null) {
                            // Filter: skip directories or META-INF files
                            if (entry.isDirectory() || entry.getName().startsWith("META-INF")) continue;

                            Path outPath = nativesDir.resolve(entry.getName());
                            Files.createDirectories(outPath.getParent());

                            try (OutputStream out = Files.newOutputStream(outPath)) {
                                zipIn.transferTo(out);
                            }

                            zipIn.closeEntry();
                        }
                    }
                }
            }

            current++;
        }
    }
}
