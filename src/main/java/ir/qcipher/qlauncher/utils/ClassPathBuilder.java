package ir.qcipher.qlauncher.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ClassPathBuilder {
    private final Path versionInstance;
    private final String version;
    private static final String OS_SEPARATOR = System.getProperty("os.name").toLowerCase().contains("win") ? ";" : ":";

    public ClassPathBuilder(Path versionInstance, String version) {
        this.versionInstance = versionInstance;
        this.version = version;
    }

    public String buildClasspath() throws IOException {
        List<Path> classpathEntries = new ArrayList<>();
        Path librariesPath = versionInstance.resolve("libraries");

        if (Files.exists(librariesPath)) {
            try (Stream<Path> libraryFiles = Files.walk(librariesPath)) {
                classpathEntries.addAll(
                        libraryFiles
                                .filter(Files::isRegularFile)
                                .filter(p -> p.toString().endsWith(".jar"))
                                .collect(Collectors.toList())
                );
            }
        }

        Path mainJar = versionInstance.resolve(version + ".jar");
        if (Files.exists(mainJar)) {
            classpathEntries.add(mainJar);
        } else {
            System.err.println("⚠ Warning: Main Minecraft JAR not found at " + mainJar);
        }

        return classpathEntries.stream()
                .map(path -> path.toAbsolutePath().toString())
                .collect(Collectors.joining(OS_SEPARATOR));
    }
}
