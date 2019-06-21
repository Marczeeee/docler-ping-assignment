package hu.docler.ping.task;

import java.util.Properties;

import hu.docler.ping.util.ResultStore;

/**
 * {@link AbstractExternalCommandTask} implementation performing ICMP protocol based ping tests of a
 * host.
 *
 */
public class IcmpPingTask extends AbstractExternalCommandTask {

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
    public IcmpPingTask(
            final Properties properties,
            final String host,
            final ResultStore resultStore) {
        super(properties, host, resultStore);
    }

    @Override
    protected String getCommandPropertyName() {
        return "ping.icmp.command";
    }

    @Override
    public String getTaskDelayPropertyName() {
        return "ping.icmp.delay";
    }

    @Override
    protected boolean checkResult(final String result) {
        boolean wasSuccessful;
        if (result.toLowerCase().contains("unreachable") || result.toLowerCase().contains("timeout")
                || result.toLowerCase().contains("timed out")) {
            wasSuccessful = false;
        } else {
            final int sentIdx = result.indexOf("Sent = ");
            final String sentNrSubstring = result.substring(sentIdx + 7);
            final String sentNrString = sentNrSubstring.substring(0, sentNrSubstring.indexOf(','));
            final long sentNr = Long.parseLong(sentNrString);

            final int receivedIdx = result.indexOf("Received = ");
            final String receivedNrSubstring = result.substring(receivedIdx + 11);
            final String receivedNrString =
                    receivedNrSubstring.substring(0, receivedNrSubstring.indexOf(','));
            final long receivedNr = Long.parseLong(receivedNrString);

            final int lostIdx = result.indexOf("Lost = ");
            final String lostNrSubstring = result.substring(lostIdx + 7);
            final String lostNrString =
                    lostNrSubstring.substring(0, lostNrSubstring.indexOf('(') - 1);
            final long lostNr = Long.parseLong(lostNrString);

            final boolean sendReceivedEqual = sentNr == receivedNr;
            final boolean lostIsZero = lostNr == 0;
            wasSuccessful = sendReceivedEqual && lostIsZero;
        }
        return wasSuccessful;
    }
}
