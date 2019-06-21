package hu.docler.ping.task;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import hu.docler.ping.task.TcpipPingTask.TcpPingResult;
import hu.docler.ping.util.ResultStore;

/**
 * {@link AbstractCommandTask} implementation performing TCP/IP based ping checks of a host. The
 * task uses HTTP requests to perform the ping check.
 *
 */
public class TcpipPingTask extends AbstractCommandTask<TcpPingResult> {
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
    public TcpipPingTask(
            final Properties properties,
            final String host,
            final ResultStore resultStore) {
        super(properties, host, resultStore);
    }

    @Override
    public String getTaskDelayPropertyName() {
        return "ping.tcpip.delay";
    }

    @Override
    protected TcpPingResult executeCheck() {
        final int timeout = loadHttpQueryTimeout();
        RequestConfig.Builder requestBuilder = RequestConfig.custom();
        requestBuilder = requestBuilder.setConnectTimeout(timeout);
        requestBuilder = requestBuilder.setConnectionRequestTimeout(timeout);
        requestBuilder = requestBuilder.setSocketTimeout(timeout);

        final HttpClientBuilder builder = HttpClientBuilder.create();
        builder.setDefaultRequestConfig(requestBuilder.build());
        final CloseableHttpClient httpClient = builder.build();
        final HttpGet httpGet = new HttpGet("http://" + getHost());
        getLogger().info("Executing HTTP GET request to host: {}", getHost());
        CloseableHttpResponse httpResponse;
        long elapsedTime;
        try {
            final long startTime = System.currentTimeMillis();
            httpResponse = httpClient.execute(httpGet);
            elapsedTime = System.currentTimeMillis() - startTime;
            getLogger().debug("HTTP request took {} milliseconds to complete", elapsedTime);
        } catch (final ClientProtocolException e) {
            getLogger().error("HTTP call failed with client protocol error", e);
            httpResponse = null;
            elapsedTime = -1;
        } catch (final IOException e) {
            getLogger().error("HTTP call failed with client I/O error", e);
            httpResponse = null;
            elapsedTime = -1;
        } finally {
            try {
                httpClient.close();
            } catch (final IOException e) {
                getLogger().warn("Failed to close http client properly", e);
            }
        }
        return new TcpPingResult(httpGet.getURI().toString(), elapsedTime,
                httpResponse != null ? httpResponse.getStatusLine().getStatusCode() : -1);
    }

    /**
     * Loads the timeout value for the HTTP query from the application properties. If the property
     * can't be found uses a default value of 60 seconds for the timeout.
     *
     * @return http query timeout value in milliseconds
     */
    private int loadHttpQueryTimeout() {
        int httpQueryTimeout;
        final String queryTimeoutValue = getProperties().getProperty("ping.tcpip.http.timeout");
        if (queryTimeoutValue != null && !queryTimeoutValue.isEmpty()) {
            httpQueryTimeout = Integer.parseInt(queryTimeoutValue);
        } else {
            httpQueryTimeout = Long.valueOf(TimeUnit.SECONDS.toMillis(60)).intValue();
        }
        return httpQueryTimeout;
    }

    @Override
    protected String convertResultToString(final TcpPingResult checkResult) {
        return checkResult.toString();
    }

    @Override
    protected boolean checkResult(final TcpPingResult result) {
        boolean isSuccessfulCheck = true;
        final String maxResponseTimeValue =
                getProperties().getProperty("ping.tcpip.reponsetime.max");
        if (maxResponseTimeValue != null && !maxResponseTimeValue.isEmpty()) {
            final long maxResponseTime = Long.parseLong(maxResponseTimeValue);
            isSuccessfulCheck = maxResponseTime <= result.responseTime;
        } else {
            isSuccessfulCheck = result.statusCode == 200;
        }
        return isSuccessfulCheck;
    }

    @Override
    protected String getCheckIdentifierName() {
        return "ping.tcpip.check";
    }

    /**
     * Holding mandatory data of the result of a tcp-based ping check.
     */
    static class TcpPingResult {
        /** URL called by the check. */
        private final String url;
        /** Response time of the call to the {@link #url}. */
        private final long responseTime;
        /** Status code of the {@link #url} query. */
        private final int statusCode;

        /**
         * Ctor.
         *
         * @param url
         *            url called
         * @param responseTime
         *            query response time
         * @param statusCode
         *            query status code
         */
        public TcpPingResult(final String url, final long responseTime, final int statusCode) {
            super();
            this.url = url;
            this.responseTime = responseTime;
            this.statusCode = statusCode;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append("URL: ");
            sb.append(url);
            sb.append("; Response Time (ms): ");
            sb.append(responseTime);
            sb.append("; Status Code: ");
            sb.append(statusCode);
            return sb.toString();
        }
    }
}
