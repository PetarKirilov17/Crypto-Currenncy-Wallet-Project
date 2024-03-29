package bg.uni.sofia.fmi.mjt.wallet.server.logger;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class ErrorLogger {
    private static final String RES_DIRECTORY = "res";
    private static final String FILE_PATH = "errors.log";
    private static Path loggerPath = Path.of(RES_DIRECTORY, FILE_PATH).toAbsolutePath();
    private static final String LOGGER_NAME = "ErrorLogger";

    private static Logger logger;

    public static void log(String message) {
        if (logger == null) {
            logger = createLogger();
        }

        logger.severe(message);
    }

    private static Logger createLogger() {
        Logger logger = Logger.getLogger(LOGGER_NAME);

        logger.setUseParentHandlers(false);
        logger.setLevel(Level.SEVERE);

        FileHandler fh;
        try {
            fh = new FileHandler(loggerPath.toString(), true);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not create logger", e);
        }

        logger.addHandler(fh);

        SimpleFormatter formatter = new SimpleFormatter();
        fh.setFormatter(formatter);

        return logger;
    }
}
