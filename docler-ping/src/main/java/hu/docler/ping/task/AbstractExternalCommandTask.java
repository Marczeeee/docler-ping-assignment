package hu.docler.ping.task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import hu.docler.ping.util.ResultStore;

/**
 * {@link AbstractCommandTask} extension containing external (operating system based) command
 * preparation and execution functionality.
 *
 */
abstract class AbstractExternalCommandTask extends AbstractCommandTask<String> {
    /**
     * Ctor.
     *
     * @param properties
     *            application {@link Properties} object
     * @param host
     *            host to be checked
     * @param resultStore
     *            result store to store task results
     */
    public AbstractExternalCommandTask(
            final Properties properties,
            final String host,
            final ResultStore resultStore) {
        super(properties, host, resultStore);
    }

    @Override
    protected String executeCheck() {
        getLogger().info("Running task to host: {}", getHost());
        final String command = createCommand();
        getLogger().info("Executing task command: {}", command);
        final String commandResult = callCommand(command);
        return commandResult;
    }

    @Override
    protected String convertResultToString(final String checkResult) {
        return checkResult;
    }

    /**
     * Returns the name of the property containing the command of the check to be executed.
     *
     * @return property name of the check command
     */
    protected abstract String getCommandPropertyName();

    @Override
    protected final String getCheckIdentifierName() {
        return getCommandPropertyName();
    }

    /**
     * Creates the command to be run on the underlying operating system. Loads the command property
     * value and puts the value of the {@link #host} property into it.
     *
     * @return the final command to be run
     */
    private String createCommand() {
        final String commandPropertyName = getCheckIdentifierName();
        final String command = getProperties().getProperty(commandPropertyName);
        return command.replace("$HOST", getHost());
    }

    /**
     * Calls an operating system command and returns the output it prints to the standard output.
     *
     * @param command
     *            command to be executed
     * @return output of the command
     */
    private String callCommand(final String command) {
        final StringBuilder sb = new StringBuilder();
        try {
            final Process process = Runtime.getRuntime().exec(command);

            final BufferedReader reader =
                    new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                getLogger().trace("Command ({}) execution output line: {}", command, line);
                sb.append(System.lineSeparator());
            }

            reader.close();
        } catch (final IOException e) {
            getLogger().error("Execution of command: {} failed", command);
        }
        return sb.toString();
    }
}
