package ir.qcipher.qlauncher.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.logging.*;

public class QLogger {
    private static final Logger logger = Logger.getLogger(QLogger.class.getName());

    private static final String logFile = "QLauncher/logs/" +
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE = "\u001B[34m";

    private static ConsoleHandler consoleHandler;
    private static FileHandler fileHandler;

    static {
        setupHandlers();
    }

    private static void setupHandlers() {
        try {
            // Ensure directory exists
            File logDir = new File(logFile).getParentFile();
            if (logDir != null && !logDir.exists()) {
                Files.createDirectories(Paths.get(logDir.getPath()));
            }

            // Remove old handlers
            for (Handler handler : logger.getHandlers()) {
                logger.removeHandler(handler);
            }

            // Console Formatter
            Formatter consoleFormatter = new Formatter() {
                private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy:MM:dd");
                private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

                @Override
                public String format(LogRecord record) {
                    String className = record.getSourceClassName();
                    String date = dateFormat.format(new Date(record.getMillis()));
                    String time = timeFormat.format(new Date(record.getMillis()));
                    String level = mapLevel(record.getLevel());
                    String message = formatMessage(record);
                    String color = getColorForLevel(level);

                    return String.format("%s[%s]: [%s %s] [%s] %s%s%n",
                            color, className, date, time, level, message, RESET);
                }

                private String mapLevel(Level level) {
                    return getLevel(level);
                }

                private String getColorForLevel(String level) {
                    switch (level) {
                        case "ERROR": return RED;
                        case "WARNING": return YELLOW;
                        case "INFO": return BLUE;
                        case "DEBUG": return RED;
                        default: return RESET;
                    }
                }
            };

            // File Formatter
            Formatter logFileFormatter = new Formatter() {
                private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy:MM:dd");
                private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss:SS");

                @Override
                public String format(LogRecord record) {
                    String className = record.getSourceClassName();
                    String date = dateFormat.format(new Date(record.getMillis()));
                    String time = timeFormat.format(new Date(record.getMillis()));
                    String level = mapLevel(record.getLevel());
                    String message = formatMessage(record);

                    return getLogText(level, date, time, className, message);
                }

                private String mapLevel(Level level) {
                    return getLevel(level);
                }
            };

            // Set up console handler
            consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(Level.ALL);
            consoleHandler.setFormatter(consoleFormatter);
            logger.addHandler(consoleHandler);

            // Set up file handler
            fileHandler = new FileHandler(logFile + ".log", true);
            fileHandler.setLevel(Level.ALL);
            fileHandler.setFormatter(logFileFormatter);
            logger.addHandler(fileHandler);

            logger.setLevel(Level.ALL);
            logger.setUseParentHandlers(false);

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to set up logger", e);
        }
    }

    private static String getLogText(String level,
                                     String date,
                                     String time,
                                     String source,
                                     String msg) {
        String m = "MESSAGE: " + msg;

        StringBuffer buffer = new StringBuffer();
        buffer
                .append("[").append(level).append("]\n")
                .append("SOURCE: ").append(source).append("\n")
                .append(String.format("TIMESTAMP: %s %s", date, time)).append("\n")
                .append(m).append("\n");
        for (int i = 1; i <= m.length(); i++) { buffer.append("-"); }
        buffer.append("|\n");

        return buffer.toString();
    }

    private static String getLevel(Level level) {
        switch (level.getName()) {
            case "SEVERE": return "ERROR";
            case "WARNING": return "WARNING";
            case "INFO": return "INFO";
            case "FINE": return "DEBUG";
            default: return level.getName();
        }
    }

    public Logger getLogger() {
        return logger;
    }
}
