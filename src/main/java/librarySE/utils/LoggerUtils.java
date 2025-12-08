package librarySE.utils;


import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;

/**
 * Utility class for simple file-based logging used across the library system.
 * <p>
 * This class provides a lightweight mechanism to record actions, errors,
 * or notifications into text files under the directory <b>library_data/logs</b>.
 * Each log entry automatically includes a timestamp and the calling class
 * and method name for easier debugging and traceability.
 * </p>
 *
 * <p>
 * All methods are static, making this class globally accessible without instantiation.
 * </p>
 *
 * @author Malak
 * 
 * 
 */
public final class LoggerUtils {

    /** Directory where log files are stored. */
    private static final Path LOG_DIR = Paths.get("library_data", "logs");

    // Static initializer ensures log directory exists 
    static {
        try {
            if (!Files.exists(LOG_DIR)) Files.createDirectories(LOG_DIR);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create log directory", e);
        }
    }

    /** Private constructor to prevent instantiation. */
    private LoggerUtils() {}

    /**
     * Writes a log message to a specific file inside {@code library_data/logs}.
     * <p>
     * Each message includes a timestamp and the caller's class and method name.
     * If the log file does not exist, it is automatically created.
     * </p>
     *
     * @param fileName the log file name (e.g. {@code "actions_log.txt"})
     * @param message  the message content to be logged
     */
    public static void log(String fileName, String message) {
        Path file = LOG_DIR.resolve(fileName);

        // Retrieve caller info for better traceability
        StackTraceElement caller = Thread.currentThread().getStackTrace()[2];
        String callerInfo = caller.getClassName().substring(
                caller.getClassName().lastIndexOf('.') + 1)
                + "." + caller.getMethodName();

        String line = String.format("[%s] [%s] %s%s",
                LocalDateTime.now(), callerInfo, message, System.lineSeparator());

        try {
            Files.writeString(file, line,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write log: " + fileName, e);
        }
    }
}
