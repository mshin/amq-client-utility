package com.example.amq.client;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.jms.JMSException;

/**
 * Helper class to build an ActiveMQ URI without having to know specifics about
 * ActiveMQ failover transport.
 */
public class UriBuilder {
    private List<URI> addressList;
    private boolean failover;
    private boolean randomize;
    private int maxReconnectAttempts;
    private int startupMaxReconnectAttempts;

    // Default values 
    private static final boolean DEFAULT_FAILOVER = false;
    private static final boolean DEFAULT_RANDOMIZE = false;
    private static final int DEFAULT_MAX_RECONNECT_ATTEMPTS = -1;
    private static final int DEFAULT_STARTUP_MAX_RECONNECT_ATTEMPTS = -1;

    
    /**
     * Returns a UriBuilder with default settings and the given addressList
     */
    public UriBuilder(List<URI> addressList) {
        setAddressList(addressList);
        setFailover(DEFAULT_FAILOVER);
        setRandomize(DEFAULT_RANDOMIZE);
        setMaxReconnectAttempts(DEFAULT_MAX_RECONNECT_ATTEMPTS);
        setStartupMaxReconnectAttempts(DEFAULT_STARTUP_MAX_RECONNECT_ATTEMPTS);
    }

    /**
     * Returns a String representation of the UriBuilder to be used as an ActiveMQ URI
     */
    public static String compileUri(UriBuilder builder) throws JMSException {
        StringBuilder sb = new StringBuilder();

        List<URI> addressList = builder.getAddressList();
        if (builder.isFailover()) {
            sb.append("failover:(");
        } else if (addressList.size() > 1) {
            // Multiple addresses are not allowed if failover isn't inabled
            throw new JMSException(
                    "Failed to compile URI: multiple addresses were specified without failover");
        }

        for (int i = 0; i < addressList.size(); i++) {
            URI address = addressList.get(i);
            if (i > 0) {
                sb.append(",");
            }
            sb.append(address.getScheme());
            sb.append("://");
            sb.append(address.getHost());
            sb.append(":");
            sb.append(address.getPort());
        }

        if (builder.isFailover()) {
            sb.append(")");
            sb.append("?randomize=" + builder.isRandomize());
            if (builder.getStartupMaxReconnectAttempts() >= 0) {
                sb.append("&startupMaxReconnectAttempts="
                        + builder.getStartupMaxReconnectAttempts());
            }
            if (builder.getMaxReconnectAttempts() >= 0) {
                sb.append("&maxReconnectAttempts="
                        + builder.getMaxReconnectAttempts());
            }
        }

        return sb.toString();
    }

    public List<URI> getAddressList() {
        return addressList;
    }

    public void setAddressList(List<URI> addressList) {
        this.addressList = (addressList == null ? new ArrayList<URI>()
                : addressList);
    }

    public void addAddress(URI address) {
        if (this.addressList == null) {
            setAddressList(new ArrayList<URI>());
        }
        this.addressList.add(address);
    }

    public boolean isFailover() {
        return failover;
    }

    public void setFailover(boolean failover) {
        this.failover = failover;
    }

    public boolean isRandomize() {
        return randomize;
    }

    public void setRandomize(boolean randomize) {
        this.randomize = randomize;
    }

    public int getMaxReconnectAttempts() {
        return maxReconnectAttempts;
    }

    public void setMaxReconnectAttempts(int maxReconnectAttempts) {
        this.maxReconnectAttempts = (maxReconnectAttempts < -1 ? -1
                : maxReconnectAttempts);
    }

    public int getStartupMaxReconnectAttempts() {
        return startupMaxReconnectAttempts;
    }

    public void setStartupMaxReconnectAttempts(int startupMaxReconnectAttempts) {
        this.startupMaxReconnectAttempts = (startupMaxReconnectAttempts < -1 ? -1
                : startupMaxReconnectAttempts);
    }
}
