package hu.docler.ping.task;

import java.util.Properties;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.docler.ping.util.MapResultStoreImpl;
import hu.docler.ping.util.ResultStore;

/**
 * Dealing with running all the checker tasks scheduled and configured. Creates an starts all the
 * mandatory tasks for all of the hosts defined in the application configuration file.
 *
 */
public final class PingRunner {
    /** {@link Logger} instance. */
    private static final Logger LOGGER = LoggerFactory.getLogger(PingRunner.class);
    /** Default {@link TimeUnit} for the delay values used when scheduling checker tasks. */
    private static final TimeUnit TASK_DELAY_TIME_UNIT = TimeUnit.SECONDS;
    /**
     * {@link Properties} containing all the application properties set in an external configuration
     * file.
     */
    private final Properties properties;

    /**
     * Ctor.
     *
     * @param properties
     *            application properties object
     * @param executorService
     *            {@link ScheduledExecutorService} used to run all the checker tasks with a fixed
     *            (and configured) delay.
     */
    public PingRunner(final Properties properties, final ScheduledExecutorService executorService) {
        this.properties = properties;

        final ResultStore resultStore = new MapResultStoreImpl();

        final String[] hosts = loadHosts();
        for (final String host : hosts) {
            final IcmpPingTask icmpPingTask = new IcmpPingTask(properties, host, resultStore);
            executorService.scheduleAtFixedRate(icmpPingTask, 0, loadDelayValue("ping.icmp.delay"),
                    TASK_DELAY_TIME_UNIT);
            final TcpipPingTask tcpipPingTask = new TcpipPingTask(properties, host, resultStore);
            executorService.scheduleAtFixedRate(tcpipPingTask, 0,
                    loadDelayValue("ping.tcpip.delay"), TASK_DELAY_TIME_UNIT);
            final TracertTask tracertTask = new TracertTask(properties, host, resultStore);
            executorService.scheduleAtFixedRate(tracertTask, 0,
                    loadDelayValue("ping.tracert.delay"), TASK_DELAY_TIME_UNIT);
        }
    }

    /**
     * Loads all hosts from the application configuration. Splits the given {@link String} value and
     * returns the hosts as an array of strings where each host is an individual {@link String}.
     *
     * @return array of hosts
     *
     * @throws IllegalArgumentException
     *             If the value of the property contains no host values.
     */
    private String[] loadHosts() {
        final String hostString = properties.getProperty("ping.hosts");
        if (hostString == null || hostString.isEmpty()) {
            LOGGER.error("Missing configuration of ping.hosts property");
            throw new IllegalArgumentException("Missing configuration of ping.hosts property");
        }
        return hostString.split(",");
    }

    /**
     * Loads a delay value from the application configuration.
     *
     * @param delayPropertyName
     *            name of the property containing a delay value
     * @return numerical delay value
     *
     * @throws IllegalArgumentException
     *             If the value of the property contains no delay value.
     */
    private long loadDelayValue(final String delayPropertyName) {
        final String delayValue = properties.getProperty(delayPropertyName);
        if (delayValue == null || delayValue.isEmpty()) {
            LOGGER.error("Missing configuration of {} property", delayPropertyName);
            throw new IllegalArgumentException(
                    "Missing configuration of " + delayPropertyName + " property");
        }
        return Long.parseLong(delayValue);
    }
}
