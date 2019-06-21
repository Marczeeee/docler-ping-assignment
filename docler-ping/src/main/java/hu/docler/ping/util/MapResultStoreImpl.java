package hu.docler.ping.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stores the results of the checks of the hosts using {@link ConcurrentHashMap} objects. Stores the
 * last check's execution result for each check style for each host.
 */
public class MapResultStoreImpl implements ResultStore {
    /** {@link Logger} instance. */
    private final Logger logger = LoggerFactory.getLogger(MapResultStoreImpl.class);
    /**
     * {@link Map} storing check results based on host names. For every host name holds a
     * {@link Map} with the results for every type of checks.
     */
    private final ConcurrentHashMap<String, Map<String, String>> resultStore =
            new ConcurrentHashMap<String, Map<String, String>>(4);

    /**
     * Ctor.
     */
    public MapResultStoreImpl() {
        super();
    }

    /**
     * @see hu.docler.ping.util.ResultStore#storeHostCheckResult(java.lang.String, java.lang.String,
     *      java.lang.String)
     */
    public void storeHostCheckResult(
            final String host,
            final String checkType,
            final String result) {
        if (host == null || host.isEmpty()) {
            throw new IllegalArgumentException("Host is mandatory");
        }
        if (checkType == null || checkType.isEmpty()) {
            throw new IllegalArgumentException("CheckType is mandatory");
        }
        resultStore.putIfAbsent(host, new HashMap<String, String>(2));
        final Map<String, String> hostResultMap = resultStore.get(host);
        logger.debug("Storing result ({}) for check type ({}) and host ({})", result, checkType,
                host);
        hostResultMap.put(checkType, result);
    }

    /**
     * @see hu.docler.ping.util.ResultStore#getReportsForHost(java.lang.String)
     */
    public Map<String, String> getReportsForHost(final String host) {
        return resultStore.get(host);
    }
}
