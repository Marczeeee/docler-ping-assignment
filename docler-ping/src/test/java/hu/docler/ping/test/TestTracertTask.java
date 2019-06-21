package hu.docler.ping.test;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Assert;
import org.junit.Test;

import hu.docler.ping.task.TracertTask;
import hu.docler.ping.test.util.TestDefaults;
import hu.docler.ping.util.MapResultStoreImpl;
import hu.docler.ping.util.ResultStore;

/**
 * Testing trace route task check functionality.
 *
 */
public class TestTracertTask {
    /** {@link ExecutorService} instance. */
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    /**
     * Creates a new {@link TracertTask} task without a valid host value presented.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testTracertTaskCreateWithoutHost() {
        new TracertTask(new Properties(), null, new MapResultStoreImpl());
    }

    /**
     * Creates a new {@link TracertTask} task without a valid {@link Properties} presented.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testTracertTaskCreateWithoutProperties() {
        new TracertTask(null, "some.dummy.host", new MapResultStoreImpl());
    }

    /**
     * Creates a new {@link TracertTask} task without a valid {@link ResultStore} presented.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testTracertTaskCreateWithoutResultStore() {
        new TracertTask(new Properties(), "some.dummy.host", null);
    }

    /**
     * Tests execution of a {@link TracertTask} with a valid, reachable host.
     *
     * @throws Exception
     *             if any execution error occurs during the test
     */
    @Test
    public void testTracertTaskRun() throws Exception {
        final ResultStore resultStore = new MapResultStoreImpl();
        final TracertTask tracertTask =
                new TracertTask(createProperties(), TestDefaults.defaultTestHost, resultStore);
        final Future<?> future = executorService.submit(tracertTask);
        future.get();
        final Map<String, String> reportsForHost =
                resultStore.getReportsForHost(TestDefaults.defaultTestHost);
        Assert.assertNotNull(reportsForHost);
        Assert.assertFalse(reportsForHost.isEmpty());
        Assert.assertTrue(reportsForHost.containsKey("ping.tracert.command"));
        final String icmpPingResult = reportsForHost.get("ping.tracert.command");
        Assert.assertNotNull(icmpPingResult);
        Assert.assertFalse(icmpPingResult.isEmpty());
    }

    /**
     * Creates test {@link Properties} for test run.
     *
     * @return {@link Properties} for tasks
     */
    private Properties createProperties() {
        final Properties properties = new Properties();
        properties.put("ping.tracert.delay", "5");
        properties.put("ping.tracert.command", "tracert $HOST");
        properties.put("ping.report.url", "http://127.0.0.1/ping-report");
        return properties;
    }
}
