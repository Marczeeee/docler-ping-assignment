package hu.docler.ping.util;

import java.util.Map;

/**
 * Storing results of host checks.
 *
 */
public interface ResultStore {

    /**
     * Stores a result of a check for a host.
     *
     * @param host
     *            host name value
     * @param checkType
     *            type name of the check
     * @param result
     *            check command output
     * @throws IllegalArgumentException
     *             If the host or the check type value is <code>null</code> or an empty
     *             {@link String}.
     */
    void storeHostCheckResult(String host, String checkType, String result);

    /**
     * Returns the {@link Map} of results for a host. The result {@link Map} contains results based
     * on check identifier keys.
     *
     * @param host
     *            host name value
     * @return {@link Map} of results, or <code>null</code> if no data is available for the host
     */
    Map<String, String> getReportsForHost(String host);

}
