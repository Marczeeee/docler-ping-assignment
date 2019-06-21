package hu.docler.ping.task;

import java.util.Properties;

import hu.docler.ping.util.ResultStore;

/**
 * {@link AbstractExternalCommandTask} implementation performing traceroute to a host.
 *
 */
public class TracertTask extends AbstractExternalCommandTask {
    /**
     * Ctor.
     *
     * @param properties
     *            application properties
     * @param host
     *            host to be used
     * @param resultStore
     *            {@link ResultStore} object for storing task run results
     */
    public TracertTask(
            final Properties properties,
            final String host,
            final ResultStore resultStore) {
        super(properties, host, resultStore);
    }

    @Override
    protected String getCommandPropertyName() {
        return "ping.tracert.command";
    }

    @Override
    public String getTaskDelayPropertyName() {
        return "ping.tracert.delay";
    }

    @Override
    protected boolean checkResult(final String result) {
        return true;
    }
}
