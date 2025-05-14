package ir.qcipher.qlauncher.extra.records;

import java.nio.file.Path;

public record LauncherParentPath(Path path) {
    public LauncherParentPath(Path path) {
        this.path = path;
    }
}
