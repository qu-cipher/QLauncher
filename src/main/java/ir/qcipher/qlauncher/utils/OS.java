package ir.qcipher.qlauncher.utils;

public class OS {
    public static String getOsName() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) return "windows";
        if (os.contains("mac")) return "osx";
        if (os.contains("nux")) return "linux";
        return "";
    }
}
