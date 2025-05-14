package ir.qcipher.qlauncher;

import ir.qcipher.qlauncher.windows.QLauncherGUI;
import javafx.application.Application;

public class Main {
    public static void main(String[] args) {
        CrashReporter.setupCrashReporting();

        Application.launch(QLauncherGUI.class, args);
    }
}
