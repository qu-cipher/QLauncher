package ir.qcipher.qlauncher.utils;

import ir.qcipher.qlauncher.dialogs.ErrorDialog;
import javafx.application.Platform;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public class FileDownloader {
    private static final Logger logger = new QLogger().getLogger();
    private static final AtomicBoolean isDownloading =  new AtomicBoolean(false);

    public static Path downloadFile(String fileURL, String saveDir) throws IOException {
        URL url = new URL(fileURL);;
        Path finalPath = Path.of(saveDir);
        Path tempPath = Path.of(saveDir + ".downloading");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (isDownloading.get()) {
                logger.info("Download interrupted. Cleaning up...");
                try {
                    Files.deleteIfExists(tempPath);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }));

        if (!Files.exists(finalPath.getParent())) {
            logger.warning("Directories are not available, recreating...");
            Files.createDirectories(finalPath.getParent());
        }

        if (finalPath.toFile().exists()) {
            logger.warning("File already exists, it will be replaced with downloaded file!");
        }

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();;
        int responseCode = connection.getResponseCode();;

        if (responseCode == HttpURLConnection.HTTP_OK) {
            int contentLength = connection.getContentLength();
            logger.info(String.format("Downloading file to %s...", saveDir));
            isDownloading.set(true);
            logger.fine(String.format("Downloading file with size %s", formatSize(contentLength)));

            try (InputStream inputStream = connection.getInputStream();
                 RandomAccessFile raf = new RandomAccessFile(tempPath.toFile(), "rw")) {

                byte[] buffer = new byte[8192];
                int bytesRead;
                long totalBytesRead = 0;

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    raf.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;

//                    if (contentLength > 0) {
//                        int progress = (int) ((totalBytesRead * 100) / contentLength);
//                        System.out.print("\rProgress: " + progress + "%");
//                    }
                }

                logger.info(String.format("%s Download Finished!", saveDir));
            } finally {
                connection.disconnect();
                isDownloading.set(false);
            }

            Files.move(tempPath, finalPath, StandardCopyOption.REPLACE_EXISTING);
            logger.fine(String.format("File '%s' saved!", saveDir));
            return finalPath;
        } else {
            logger.severe(String.format("Server responded with code: %s", responseCode));
            connection.disconnect();
            return null;
        }
    }

    public static String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String unit = "KMGTPE".charAt(exp - 1) + "B";
        return String.format("%.2f %s", bytes / Math.pow(1024, exp), unit);
    }
}