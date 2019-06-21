package hu.docler.ping.test;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Assert;
import org.junit.Test;

import hu.docler.ping.util.MapResultStoreImpl;
import hu.docler.ping.util.ResultStore;

/**
 * Testing result store functionality.
 *
 */
public class TestMapResultStoreImpl {
    /**
     * Tests putting a new, valid result to a {@link ResultStore}.
     */
    @Test
    public void testPutResult() {
        final ResultStore resultStore = new MapResultStoreImpl();
        final String host = "127.0.0.1";
        final String identifierName = "some.id.value";
        final String resultValue = "Result value to be stored";
        resultStore.storeHostCheckResult(host, identifierName, resultValue);

        final Map<String, String> reportsForHost = resultStore.getReportsForHost(host);
        Assert.assertNotNull(reportsForHost);
        Assert.assertFalse(reportsForHost.isEmpty());
        final String resultValueForId = reportsForHost.get(identifierName);
        Assert.assertNotNull(resultValueForId);
        Assert.assertEquals(resultValue, resultValueForId);
    }

    /**
     * Tests putting an invalid result (host missing) to a {@link ResultStore}.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testPutResultWithInvalidHost() {
        final ResultStore resultStore = new MapResultStoreImpl();
        final String host = null;
        final String checkType = "some.id.value";
        final String resultValue = "Result value to be stored";
        resultStore.storeHostCheckResult(host, checkType, resultValue);
    }

    /**
     * Tests putting an invalid result (check type missing) to a {@link ResultStore}.
     */
    @Test
    public void testPutResultWithInvalidCheckType() {
        final ResultStore resultStore = new MapResultStoreImpl();
        final String host = "127.0.0.1";
        final String checkType = "null";
        final String resultValue = "Result value to be stored";
        resultStore.storeHostCheckResult(host, checkType, resultValue);
    }

    /**
     * Tests putting a lot of valid results to a {@link ResultStore} concurrently. Fails if
     * {@link ConcurrentModificationException} or any other error occurs.
     *
     * @throws Exception
     *             if an error occurs due to concurrent modification of the {@link ResultStore}
     */
    @Test
    public void testParallelPutResult() throws Exception {
        final ResultStore resultStore = new MapResultStoreImpl();
        final ExecutorService threadPool = Executors.newCachedThreadPool();
        final Random random = new Random();

        final String[] hosts = new String[] {"host1", "host2", "host3", "host4", "host5"};
        final String[] checkTypes = new String[] {"check1", "check2", "check3"};

        final List<Future<?>> futures = new ArrayList<Future<?>>(1000);
        for (int i = 0; i < 1000; i++) {
            futures.add(threadPool.submit(new SimpleTask(hosts[random.nextInt(hosts.length)],
                    checkTypes[random.nextInt(checkTypes.length)], resultStore)));
        }

        for (final Future<?> future : futures) {
            future.get();
        }

        for (final String host : hosts) {
            final Map<String, String> reportsForHost = resultStore.getReportsForHost(host);
            Assert.assertNotNull(reportsForHost);
            Assert.assertFalse(reportsForHost.isEmpty());
            Assert.assertEquals(checkTypes.length, reportsForHost.size());
        }

    }

    /**
     * Simple task implementation to be used to update {@link ResultStore} with a new result value.
     *
     */
    private class SimpleTask implements Runnable {
        /** Host value. */
        private final String host;
        /** Check typ evalue. */
        private final String checkType;
        /** {@link ResultStore} value. */
        private final ResultStore resultStore;

        /**
         * Ctor.
         * 
         * @param host
         *            host value
         * @param checkType
         *            check type value
         * @param resultStore
         *            {@link ResultStore} object
         */
        public SimpleTask(
                final String host,
                final String checkType,
                final ResultStore resultStore) {
            super();
            this.host = host;
            this.resultStore = resultStore;
            this.checkType = checkType;
        }

        /**
         * @see Runnable#run()
         */
        public void run() {
            resultStore.storeHostCheckResult(host, checkType,
                    Long.toString(System.currentTimeMillis()));
        }
    }
}
