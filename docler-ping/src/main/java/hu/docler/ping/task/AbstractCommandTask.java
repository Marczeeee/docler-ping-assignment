package hu.docler.ping.task;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.docler.ping.report.ReportSender;
import hu.docler.ping.util.ResultStore;

/**
 * {@link Runnable} implementation containing standard check execution functionality.
 *
 * @param <R>
 *            type of the result the check produces
 */
abstract class AbstractCommandTask<R extends Object> implements Runnable {
    /** {@link Logger} instance for the current implementation class. */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    /**
     * {@link Properties} object containing all the configuration options set for the application.
     */
    private final Properties properties;
    /** Host of the remote system to be checked by the task. */
    private final String host;
    /**
     * Result store used to store the output of the task's last run.
     */
    private final ResultStore resultStore;

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
    public AbstractCommandTask(
            final Properties properties,
            final String host,
            final ResultStore resultStore) {
        this.properties = properties;
        this.host = host;
        this.resultStore = resultStore;
        if (properties == null) {
            throw new IllegalArgumentException("Properties are mandatory, can't be null");
        }
        if (host == null) {
            throw new IllegalArgumentException("Host value is mandatory, can't be null");
        }
        if (resultStore == null) {
            throw new IllegalArgumentException("Result store is mandatory, can't be null");
        }
    }

    /**
     * @see Runnable#run()
     */
    public void run() {
        getLogger().info("Executing check type: {}", getCheckIdentifierName());
        final R checkResult = executeCheck();
        getLogger().debug("Converting check ({}) result ({}) to a string", getCheckIdentifierName(),
                checkResult);
        final String resultString = convertResultToString(checkResult);
        getLogger().info("Storing command ({}) result for host: {}", getCheckIdentifierName(),
                getHost());
        getResultStore().storeHostCheckResult(getHost(), getCheckIdentifierName(), resultString);
        final boolean postCheckResult = checkResult(checkResult);
        getLogger().info("Post check result was {} for check type: {}",
                postCheckResult ? "successful" : "failed", getCheckIdentifierName());

        if (!postCheckResult) {
            getLogger().warn("Sending report of failed check ({}) execution",
                    getCheckIdentifierName());
            new ReportSender(getHost(), getProperties(), getResultStore()).sendReport();
        }
    }

    /**
     * Executes implemented checking.
     *
     * @return result object of the check
     */
    protected abstract R executeCheck();

    /**
     * Converts the result of a check to a plain, storeable {@link String}.
     *
     * @param checkResult
     *            result of a check
     * @return String representation of the result
     */
    protected abstract String convertResultToString(R checkResult);

    /**
     * Returns the property name containing the run delay for the task used when scheduling the
     * task.
     *
     * @return property name of the run delay property
     */
    protected abstract String getTaskDelayPropertyName();

    /**
     * Returns an identifier name (like a property name of the command) for the task to be run.
     *
     * @return identifier name of the command property
     */
    protected abstract String getCheckIdentifierName();

    /**
     * Checks the result of a check execution and returns if the check execution was successful or
     * not based on the checks performed.
     *
     * @param result
     *            check result
     * @return <code>true</code> if the check execution was successful, <code>false</code> otherwise
     */
    protected abstract boolean checkResult(R result);

    protected String getHost() {
        return host;
    }

    protected Properties getProperties() {
        return properties;
    }

    protected ResultStore getResultStore() {
        return resultStore;
    }

    protected final Logger getLogger() {
        return logger;
    }
}
