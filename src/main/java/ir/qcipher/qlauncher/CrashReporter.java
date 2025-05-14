package ir.qcipher.qlauncher;

import ir.qcipher.qlauncher.dialogs.ErrorDialog;
import javafx.application.Platform;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CrashReporter {
    public static void setupCrashReporting() {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            String crashReportPath = getCrashReportFilePath();
            Platform.runLater(() -> ErrorDialog.show(throwable));
            try {
                writeCrashReport(crashReportPath, throwable);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private static String getCrashReportFilePath() {
        String directoryPath = "QLauncher\\crash-reports";
        try {
            Files.createDirectories(Paths.get(directoryPath));
        } catch (IOException e) {
            e.printStackTrace();
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String timestamp = sdf.format(new Date());
        return directoryPath + File.separator + "crash_report_" + timestamp + ".txt";
    }

    private static void writeCrashReport(String crashReportPath, Throwable throwable) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(crashReportPath))) {
            throwable.printStackTrace(writer);

            writer.println("\n---- System Information ----");
            writer.println("Java Version: " + System.getProperty("java.version"));
            writer.println("OS: " + System.getProperty("os.name") + " " + System.getProperty("os.version"));
            writer.println("Architecture: " + System.getProperty("os.arch"));
            writer.println("User: " + System.getProperty("user.name"));
        }
    }
}
