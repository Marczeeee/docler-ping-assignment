package hu.docler.ping;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.docler.ping.task.PingRunner;
import hu.docler.ping.util.CliArgsHandler;

/**
 * Entry point of the DoclerPing application.
 */
public final class DoclerPing {
    /**
     * {@link Logger} instance.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DoclerPing.class);
    /**
     * {@link ScheduledExecutorService} instance to run repeatedly all Ping and Traceroute tasks.
     */
    private static final ScheduledExecutorService EXECUTOR_SERVICE =
            Executors.newScheduledThreadPool(4);

    /**
     * Ctor.
     */
    private DoclerPing() {
        super();
    }

    /**
     * Application main function.
     *
     * @param args
     *            array of command line arguments
     */
    public static void main(final String[] args) {
        final CliArgsHandler cliArgsHandler = new CliArgsHandler(args);
        cliArgsHandler.parse();
        final String propertiesFilePath = cliArgsHandler.getArgumentValue("config");
        Properties properties;
        try {
            properties = DoclerPing.loadProperties(propertiesFilePath);
        } catch (final IOException e) {
            properties = null;
            LOGGER.error("Failed to load application properties", e);
            System.exit(1);
        }

        new PingRunner(properties, EXECUTOR_SERVICE);

        DoclerPing.keepAppAlive();
    }

    /**
     * Loads the content of the given properties file to a {@link Properties} object containing all
     * the properties set for the application.
     *
     * @param propertiesFilePath
     *            path to the properties file
     * @return the {@link Properties} object loaded with application properties
     * @throws IOException
     *             If the given path doesn't exists, or loading the properties file fails due to an
     *             I/O error.
     */
    private static Properties loadProperties(final String propertiesFilePath) throws IOException {
        final Properties props = new Properties();
        final FileInputStream fileInputStream = new FileInputStream(propertiesFilePath);
        try {
            props.load(fileInputStream);
        } finally {
            fileInputStream.close();
        }
        return props;
    }

    /**
     * Keeps the application alive and running till the user press Q button to exit.
     */
    private static void keepAppAlive() {
        System.out.println("Press Q then Enter to exit");
        final BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String msg = null;
        while (true) {
            try {
                msg = in.readLine();
            } catch (final IOException e) {
                LOGGER.error("Failed to read user input from system input channel", e);
            }

            if ("Q".equals(msg)) {
                LOGGER.warn("Received Q character on system input, quit now...");
                EXECUTOR_SERVICE.shutdownNow();
                break;
            }
        }
    }
}
