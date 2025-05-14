package ir.qcipher.qlauncher.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.qcipher.qlauncher.extra.UserType;
import ir.qcipher.qlauncher.extra.VersionTypes;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Logger;

public class RunCommandBuilder {
    private static final Logger logger = new QLogger().getLogger();
    private static final ObjectMapper mapper = new ObjectMapper();

    private final List<String> parsedArguments = new ArrayList<>();
    private final Map<String, String> argumentValues = new HashMap<>();
    private String mainClass;

    public List<String> getRunCommand(
            String javaPath,
            String username,
            String version,
            Path versionInstance,
            String uuid,
            String accessToken,
            String xuid,
            String clientId,
            UserType userType,
            VersionTypes versionType,
            String userProps,
            String memorySize,
            String heapSize,
            boolean isDemo
    ) throws IOException {
        ClassPathBuilder classPathBuilder = new ClassPathBuilder(versionInstance, version);

        String classPath = classPathBuilder.buildClasspath();

        List<String> command = new ArrayList<>();
        List<String> javaArgs = new ArrayList<>();
        List<String> arguments = getRunArguments(
                username,
                version,
                versionInstance,
                uuid,
                accessToken,
                xuid,
                clientId,
                userType,
                versionType,
                userProps,
                isDemo
        );

        if (mainClass == null || mainClass.isEmpty()) {
            throw new IllegalStateException("Main class is not set. Make sure parseArguments() is called.");
        }

        javaArgs.add(javaPath);
        javaArgs.add("-Djava.library.path=" + versionInstance.resolve("natives").toAbsolutePath().toString());
        javaArgs.add("-Xmx" + memorySize + "G");
        javaArgs.add("-Xms" + heapSize + "G");
        javaArgs.add("-cp");
        javaArgs.add(classPath);

        command.addAll(javaArgs);
        command.add(mainClass);
        command.addAll(arguments);
        return command;
    }

    private List<String> getRunArguments(
            String username,
            String version,
            Path versionInstance,
            String uuid,
            String accessToken,
            String xuid,
            String clientId,
            UserType userType,
            VersionTypes versionType,
            String userProps,
            boolean isDemo
    ) throws IOException {
        parseArguments(versionInstance, version);
        constructArgumentValues(username, version, versionInstance, uuid, accessToken, xuid, clientId, userType, versionType, userProps);

        return constructFinalArguments(isDemo);
    }

    private void parseArguments(Path versionInstance, String version) throws IOException {
        Path versionJsonFile = versionInstance.resolve(version + ".json");

        JsonNode rootNode = mapper.readTree(versionJsonFile.toFile());
        this.mainClass = rootNode.path("mainClass").asText(null);
        String legacyArguments = rootNode.path("minecraftArguments").asText(null);
        JsonNode modernArguments = rootNode.path("arguments").path("game");

        if (mainClass == null) {
            throw new IOException("Invalid version JSON: 'mainClass' is missing in " + versionJsonFile);
        }

        if (legacyArguments != null && modernArguments.isMissingNode()) {
            parsedArguments.addAll(Arrays.asList(legacyArguments.split(" ")));
        } else {
            for (JsonNode arg : modernArguments) {
                if (arg.isTextual()) {
                    parsedArguments.add(arg.asText());
                } else if (arg.isObject() && arg.has("value")) {
                    JsonNode valueNode = arg.get("value");
                    if (valueNode.isTextual()) {
                        parsedArguments.add(valueNode.asText());
                    } else if (valueNode.isArray()) {
                        for (JsonNode value : valueNode) {
                            parsedArguments.add(value.asText());
                        }
                    }
                }
            }
        }
    }

    private void constructArgumentValues(
            String username,
            String version,
            Path versionInstance,
            String uuid,
            String accessToken,
            String xuid,
            String clientId,
            UserType userType,
            VersionTypes versionType,
            String userProps
    ) throws IOException {
        String gameDir = versionInstance.toAbsolutePath().toString();
        String assetsDir = versionInstance.resolve("assets").toAbsolutePath().toString();
        String assetIndex = VersionUtils.getAssetIndexId(versionInstance.getParent().getParent().getParent(), version, versionType.name().toLowerCase());

        argumentValues.put("auth_player_name", username);
        argumentValues.put("version_name", version);
        argumentValues.put("game_directory", gameDir);

        argumentValues.put("assets_root", assetsDir);
        argumentValues.put("game_assets", assetsDir);

        argumentValues.put("assets_index_name", assetIndex);

        argumentValues.put("auth_session", uuid);
        argumentValues.put("auth_uuid", uuid);

        argumentValues.put("auth_access_token", accessToken);
        argumentValues.put("clientid", clientId);
        argumentValues.put("auth_xuid", xuid);
        argumentValues.put("user_type", userType.name().toLowerCase());
        argumentValues.put("version_type", versionType.name().toLowerCase());
        argumentValues.put("user_properties", userProps);
    }

    private List<String> constructFinalArguments(boolean isDemo) {
        List<String> finalArguments = new ArrayList<>();

        for (String arg : parsedArguments) {
            if (arg.startsWith("${") && arg.endsWith("}")) {
                String key = arg.substring(2, arg.length() - 1);
                String value = argumentValues.get(key);

                if (value == null) {
                    logger.warning("Missing argument value for key: " + key);
                    value = "";
                }

                finalArguments.add(value);
            } else {
                finalArguments.add(arg);
            }
        }

        if (isDemo) {
            finalArguments.add("--demo");
        }

        return finalArguments;
    }
}
