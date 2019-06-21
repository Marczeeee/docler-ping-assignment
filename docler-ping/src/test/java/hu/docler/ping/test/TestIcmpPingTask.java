package hu.docler.ping.test;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Assert;
import org.junit.Test;

import hu.docler.ping.task.IcmpPingTask;
import hu.docler.ping.test.util.TestDefaults;
import hu.docler.ping.util.MapResultStoreImpl;
import hu.docler.ping.util.ResultStore;

/**
 * Testing ICMP ping task functionality.
 *
 */
public class TestIcmpPingTask {
    /** {@link ExecutorService} instance. */
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    /**
     * Creates a new {@link IcmpPingTask} task without a valid host value presented.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testIcmpPingTaskCreateWithoutHost() {
        new IcmpPingTask(new Properties(), null, new MapResultStoreImpl());
    }

    /**
     * Creates a new {@link IcmpPingTask} task without a valid {@link Properties} presented.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testIcmpPingTaskCreateWithoutProperties() {
        new IcmpPingTask(null, "some.dummy.host", new MapResultStoreImpl());
    }

    /**
     * Creates a new {@link IcmpPingTask} task without a valid {@link ResultStore} presented.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testIcmpPingTaskCreateWithoutResultStore() {
        new IcmpPingTask(new Properties(), "some.dummy.host", null);
    }

    /**
     * Tests execution of an {@link IcmpPingTask} with a valid, reachable host.
     *
     * @throws Exception
     *             if any execution error occurs during the test
     */
    @Test
    public void testIcmpPingTaskRun() throws Exception {
        final ResultStore resultStore = new MapResultStoreImpl();
        final IcmpPingTask icmpPingTask =
                new IcmpPingTask(createProperties(), TestDefaults.defaultTestHost, resultStore);
        final Future<?> future = executorService.submit(icmpPingTask);
        future.get();
        final Map<String, String> reportsForHost =
                resultStore.getReportsForHost(TestDefaults.defaultTestHost);
        Assert.assertNotNull(reportsForHost);
        Assert.assertFalse(reportsForHost.isEmpty());
        Assert.assertTrue(reportsForHost.containsKey("ping.icmp.command"));
        final String icmpPingResult = reportsForHost.get("ping.icmp.command");
        Assert.assertNotNull(icmpPingResult);
        Assert.assertFalse(icmpPingResult.isEmpty());
    }

    /**
     * Tests execution of an {@link IcmpPingTask} with an unreachable host.
     *
     * @throws Exception
     *             if any execution error occurs during the test
     */
    @Test
    public void testIcmpPingTaskRunNotReachableHost() throws Exception {
        final ResultStore resultStore = new MapResultStoreImpl();
        final String host = "192.168.0.11";
        final IcmpPingTask icmpPingTask = new IcmpPingTask(createProperties(), host, resultStore);
        final Future<?> future = executorService.submit(icmpPingTask);
        future.get();
        final Map<String, String> reportsForHost = resultStore.getReportsForHost(host);
        Assert.assertNotNull(reportsForHost);
        Assert.assertFalse(reportsForHost.isEmpty());
        Assert.assertTrue(reportsForHost.containsKey("ping.icmp.command"));
        final String icmpPingResult = reportsForHost.get("ping.icmp.command");
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
        properties.put("ping.icmp.delay", "5");
        properties.put("ping.icmp.command", "ping -n 5 $HOST");
        properties.put("ping.report.url", "http://127.0.0.1/ping-report");
        return properties;
    }
}
