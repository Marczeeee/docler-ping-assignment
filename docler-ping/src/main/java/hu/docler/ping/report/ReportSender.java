package hu.docler.ping.report;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.docler.ping.util.ResultStore;

/**
 * Sending an error report to a remote url with the details of the error of a host.
 *
 */
public class ReportSender {
    /** {@link Logger} instance. */
    private final Logger logger = LoggerFactory.getLogger(ReportSender.class);
    /** Host used in failed check. */
    private final String host;
    /** Application properties. */
    private final Properties properties;
    /** Store of check results. */
    private final ResultStore resultStore;

    /**
     * Ctor.
     *
     * @param host
     *            host checked
     * @param properties
     *            application properties
     * @param resultStore
     *            result store object
     */
    public ReportSender(
            final String host,
            final Properties properties,
            final ResultStore resultStore) {
        this.host = host;
        this.properties = properties;
        this.resultStore = resultStore;
    }

    /**
     * Send a report to a remote server configured in the application configuration using HTTP POST
     * command.
     */
    public void sendReport() {
        final ReportData reportData = createReportData();
        logger.debug("Created error report data ({}) for host: {}", reportData, host);
        final String reportUrl = properties.getProperty("ping.report.url");
        logger.debug("Using error report url: {}", reportUrl);
        final HttpPost httpPost = new HttpPost(reportUrl);
        httpPost.setEntity(new StringEntity(reportData.toString(), ContentType.APPLICATION_JSON));
        final CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            final CloseableHttpResponse response = httpClient.execute(httpPost);
            logger.warn("Reporting error result of host ({}) finished with status code: {}", host,
                    response.getStatusLine().getStatusCode());
        } catch (final ClientProtocolException e) {
            logger.error("Failed to post error report due to client protocol error", e);
        } catch (final IOException e) {
            logger.error("Failed to post error report due to I/O error", e);
        } finally {
            try {
                httpClient.close();
            } catch (final IOException e) {
                logger.error("Failed to close http client", e);
            }
        }
    }

    /**
     * Creating a report data object for previously set host.
     *
     * @return {@link ReportData} object created
     */
    private ReportData createReportData() {
        logger.info("Creating error report data for host: {}", host);
        final Map<String, String> reportsForHost = resultStore.getReportsForHost(host);
        final ReportData reportData = new ReportData(host, reportsForHost.get("ping.icmp.command"),
                reportsForHost.get("ping.tcpip.delay"), reportsForHost.get("ping.tracert.command"));
        return reportData;
    }

    /**
     * Class holding error report data. With the {@link #toString()} method converts the data set to
     * a standard JSON format.
     *
     */
    private static class ReportData {
        /** Host name. */
        private final String host;
        /** ICMP ping results. */
        private final String icmpPingResult;
        /** TCP ping results. */
        private final String tcpPingResult;
        /** Traceroute results. */
        private final String traceResult;

        /**
         * Ctor.
         *
         * @param host
         *            host name
         * @param icmpPingResult
         *            ICMP ping result value
         * @param tcpPingResult
         *            TCP ping result value
         * @param traceResult
         *            traceroute result value
         */
        public ReportData(
                final String host,
                final String icmpPingResult,
                final String tcpPingResult,
                final String traceResult) {
            super();
            this.host = host;
            this.icmpPingResult = icmpPingResult;
            this.tcpPingResult = tcpPingResult;
            this.traceResult = traceResult;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("{");
            sb.append("\"host\":\"");
            sb.append(host);
            sb.append("\", ");
            sb.append("\"icmp_ping\":\"");
            sb.append(icmpPingResult);
            sb.append("\", ");
            sb.append("\"tcp_ping\":\"");
            sb.append(tcpPingResult);
            sb.append("\", ");
            sb.append("\"trace\":\"");
            sb.append(traceResult);
            sb.append("\"}");
            return sb.toString();
        }
    }
}
