package hu.docler.ping.util;

import java.util.Arrays;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loading and managing application command line parameters.
 */
public class CliArgsHandler {
    /**
     * {@link Logger} instance.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(CliArgsHandler.class);
    /**
     * Array of command line arguments.
     */
    private final String[] args;
    /**
     * Storing and managing the command line options.
     */
    private final Options options = new Options();
    /**
     * Contains the whole command line read.
     */
    private CommandLine cmd;
    /**
     * Name of the application to be printed on help page.
     */
    private final String appName = "DoclerPing";

    /**
     * Ctor.
     *
     * @param args
     *            array of application command line arguments
     */
    public CliArgsHandler(final String[] args) {
        super();

        if (args != null) {
            this.args = Arrays.copyOf(args, args.length);
        } else {
            this.args = new String[0];
        }

        LOGGER.debug("Loading command line arguments: {}", Arrays.toString(args));

        final OptionGroup helpOptionsGroup = new OptionGroup();
        helpOptionsGroup.addOption(new Option("h", "help", false, "Show help"));
        options.addOptionGroup(helpOptionsGroup);

        final OptionGroup defaultOptionsGroup = new OptionGroup();
        defaultOptionsGroup.addOption(
                new Option("c", "config", true, "Path of the application configuration file"));
        options.addOptionGroup(defaultOptionsGroup);
    }

    /**
     * Reads all the command line arguments given to the application and parses them. Checks the
     * parsed arguments against the options set in {@link #options}.
     *
     */
    public final void parse() {
        final CommandLineParser parser = new DefaultParser();

        try {
            LOGGER.debug("Loading and processing command line arguments...");
            cmd = parser.parse(options, args, false);

            if (cmd.hasOption("h")) {
                printHelp();
            }

            if (!cmd.hasOption("c")) {
                System.out.println("Missing configuration file parameter!");
                System.exit(1);
            }
        } catch (final ParseException e) {
            LOGGER.error("Failed to read command line arguments: {}", e.getMessage());
            printHelp();
            System.exit(1);
        }
    }

    /**
     * Prints a helper text for the application command line arguments to the standard output and
     * exists the application. The helper text contains information for all the available arguments
     * of the application.
     */
    public final void printHelp() {
        final HelpFormatter formater = new HelpFormatter();
        formater.printHelp(appName, "", options,
                "Using -Dlogback.configurationFile=/path/to/your/logback.xml you can set your own logging configuration");
        System.exit(0);
    }

    /**
     * Returns the value set for a command line argument. If the argument doesn't exist returns with
     * <code>null</code>.
     *
     * @param argument
     *            command line argument identifier
     * @return value set for the argument, or <code>null</code> if no value available or the
     *         argument is missing
     */
    public String getArgumentValue(final String argument) {
        if (cmd.hasOption(argument)) {
            return cmd.getOptionValue(argument);
        } else {
            return null;
        }
    }
}
