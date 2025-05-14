package ir.qcipher.qlauncher.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The <code>VersionUtils</code> class
 * contains some useful tools related to version managing.
 */
public class VersionUtils {
    private static ObjectMapper mapper = new ObjectMapper();

    public static List<String> extractInstalledReleases(Path parent) {
        Path releasesPath = parent.resolve("instances").resolve("release");
        return getSubFolders(releasesPath);
    }

    public static List<String> extractInstalledSnapshots(Path parent) {
        Path snapshotsPath = parent.resolve("instances").resolve("snapshot");
        return getSubFolders(snapshotsPath);
    }

    public static List<String> extractInstalledAlphaBeta(Path parent) {
        Path othersPath = parent.resolve("instances").resolve("other");
        return getSubFolders(othersPath);
    }

    public static String getVersionInfoJsonURL(Path parent, String versionId) throws IOException {
        Path manifest = parent.resolve("config").resolve("v_manifest.json");
        if (!Files.exists(manifest)) return null;

        ArrayNode versions = (ArrayNode) mapper.readTree(manifest.toFile()).path("versions");
        for (JsonNode version : versions) {
            String id = version.path("id").asText();
            if (id.equals(versionId)) {
                return version.path("url").asText();
            }
        }
        return null;
    }

    public static String getVersionJarURL(Path parent, String versionId, String versionType) throws IOException {
        Path versionJsonFile = getVersionJsonFilePath(parent, versionId, versionType);
        JsonNode clientJsonNode = mapper.readTree(versionJsonFile.toFile()).path("downloads").path("client");

        return clientJsonNode.get("url").asText();
    }

    public static Path getVersionPath(Path launcherParent, String versionId, String versionType) {
        return launcherParent.resolve("instances").resolve(versionType.toLowerCase()).resolve(versionId);
    }

    public static Path getVersionJsonFilePath(Path launcherParent, String versionId, String versionType) {
        return getVersionPath(launcherParent, versionId, versionType).resolve(versionId+".json");
    }

    public static Path getVersionJarFilePath(Path launcherParent, String versionId, String versionType) {
        return getVersionPath(launcherParent, versionId, versionType).resolve(versionId + ".jar");
    }

    public static Path getAssetParent(Path launcherParent, String versionId, String versionType) {
        return getVersionPath(launcherParent, versionId, versionType).resolve("assets");
    }

    public static Path getVersionAssetIndexParent(Path launcherParent, String versionId, String versionType) {
        return getAssetParent(launcherParent, versionId, versionType).resolve("indexes");
    }

    public static Path getAssetObjectsPath(Path launcherParent, String versionId, String versionType) {
        return getAssetParent(launcherParent, versionId, versionType).resolve("objects");
    }

    public static String getAssetIndexId(Path parent, String versionId, String versionType) throws IOException {
        JsonNode versionInfoJson = mapper.readTree(getVersionJsonFilePath(parent, versionId, versionType).toFile());
        return versionInfoJson.path("assetIndex").path("id").asText();
    }

    public static String getAssetIndexJsonURL(Path parent, String versionId, String versionType) throws IOException {
        JsonNode versionInfoJson = mapper.readTree(getVersionJsonFilePath(parent, versionId, versionType).toFile());
        return versionInfoJson.path("assetIndex").path("url").asText();
    }

    public static Path getLibrariesParent(Path parent, String versionId, String versionType) {
        return getVersionPath(parent, versionId, versionType).resolve("libraries");
    };

    public static Path getNativesParent(Path parent, String versionId, String versionType) {
        return getVersionPath(parent, versionId, versionType).resolve("natives");
    };

    public static void removeVersion(Path launcherParent, String versionId, String versionType) throws IOException {
        Path vDir = getVersionPath(launcherParent, versionId, versionType);

        if (!Files.exists(vDir)) {
            return;
        }

        Files.walkFileTree(vDir, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private static List<String> getSubFolders(Path path) {
        if (!Files.exists(path)) return new ArrayList<>();

        try {
            return Files.list(path)
                    .filter(Files::isDirectory)
                    .map(p -> p.getFileName().toString())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }
}
