package ir.qcipher.qlauncher;

import ir.qcipher.qlauncher.extra.UserType;
import ir.qcipher.qlauncher.extra.VersionTypes;
import ir.qcipher.qlauncher.minecraft.Initializer;
import ir.qcipher.qlauncher.utils.RunCommandBuilder;
import ir.qcipher.qlauncher.utils.VersionUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class Launcher {
    public static void launch(
            String username,
            String javaPath,
            String version,
            String ram,
            String heap,
            VersionTypes type,
            Path launcherParent,
            boolean demo) throws IOException {
        String xuuid = UUID.randomUUID().toString();

        RunCommandBuilder runCommandBuilder = new RunCommandBuilder();

        Path path = VersionUtils.getVersionPath(launcherParent, version, type.name().toLowerCase());
        System.out.println(path.toAbsolutePath());

        List<String> cmd = runCommandBuilder.getRunCommand(
                javaPath,
                username,
                version,
                path,
                xuuid,
                "0",
                xuuid,
                "0",
                UserType.LEGACY,
                type,
                "{}",
                ram,
                heap,
                demo
        );

        System.out.println("\n"+ Arrays.toString(cmd.toArray()) +"\n");

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.directory(path.toFile());
        pb.redirectErrorStream(true);
        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        pb.start();
    }
}
