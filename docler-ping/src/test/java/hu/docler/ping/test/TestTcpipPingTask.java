package hu.docler.ping.test;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Assert;
import org.junit.Test;

import hu.docler.ping.task.TcpipPingTask;
import hu.docler.ping.test.util.TestDefaults;
import hu.docler.ping.util.MapResultStoreImpl;
import hu.docler.ping.util.ResultStore;

/**
 * Testing TCP ping task functionality.
 *
 */
public class TestTcpipPingTask {
    /** {@link ExecutorService} instance. */
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    /**
     * Creates a new {@link TcpipPingTask} task without a valid host value presented.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testTcpipPingTaskCreateWithoutHost() {
        new TcpipPingTask(new Properties(), null, new MapResultStoreImpl());
    }

    /**
     * Creates a new {@link TcpipPingTask} task without a valid {@link Properties} presented.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testTcpipPingTaskCreateWithoutProperties() {
        new TcpipPingTask(null, "some.dummy.host", new MapResultStoreImpl());
    }

    /**
     * Creates a new {@link TcpipPingTask} task without a valid {@link ResultStore} presented.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testTcpipPingTaskCreateWithoutResultStore() {
        new TcpipPingTask(new Properties(), "some.dummy.host", null);
    }

    /**
     * Tests execution of an {@link TcpipPingTask} with a valid, reachable host.
     *
     * @throws Exception
     *             if any execution error occurs during the test
     */
    @Test
    public void testTcpipPingTaskRun() throws Exception {
        final ResultStore resultStore = new MapResultStoreImpl();
        final TcpipPingTask tcpipPingTask =
                new TcpipPingTask(createProperties(), TestDefaults.defaultTestHost, resultStore);
        final Future<?> future = executorService.submit(tcpipPingTask);
        future.get();
        final Map<String, String> reportsForHost =
                resultStore.getReportsForHost(TestDefaults.defaultTestHost);
        Assert.assertNotNull(reportsForHost);
        Assert.assertFalse(reportsForHost.isEmpty());
        Assert.assertTrue(reportsForHost.containsKey("ping.tcpip.check"));
        final String icmpPingResult = reportsForHost.get("ping.tcpip.check");
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
        properties.put("ping.tcpip.delay", "5");
        properties.put("ping.tcpip.http.timeout", "10000");
        properties.put("ping.tcpip.reponsetime.max", "1000");
        properties.put("ping.report.url", "http://127.0.0.1/ping-report");
        return properties;
    }
}
