package com.example.amq.client;

import javax.jms.JMSException;

import com.example.amq.client.strategy.BrokerClientStrategy;
import com.example.amq.client.strategy.SimpleBrokerClientStrategy;

/**
 * Factory for instantiating new Broker instances 
 */
public class BrokerFactory {

    public static Broker newBrokerInstance(String uri) throws JMSException {
        return newBrokerInstance(new SimpleBrokerClientStrategy(), null, null,
                uri);
    }

    public static Broker newBrokerInstance(UriBuilder uri) throws JMSException {
        return newBrokerInstance(new SimpleBrokerClientStrategy(), null, null,
                uri);
    }

    public static Broker newBrokerInstance(String username, String password,
            String uri) throws JMSException {
        return newBrokerInstance(new SimpleBrokerClientStrategy(), username,
                password, uri);
    }

    public static Broker newBrokerInstance(String username, String password,
            UriBuilder uri) throws JMSException {
        return newBrokerInstance(new SimpleBrokerClientStrategy(), username,
                password, uri);
    }

    public static Broker newBrokerInstance(BrokerClientStrategy strategy,
            String username, String password, UriBuilder uri)
            throws JMSException {
        return newBrokerInstance(strategy, username, password,
                UriBuilder.compileUri(uri));
    }

    public static Broker newBrokerInstance(BrokerClientStrategy strategy,
            UriBuilder uri) throws JMSException {
        return newBrokerInstance(strategy, UriBuilder.compileUri(uri));
    }

    public static Broker newBrokerInstance(BrokerClientStrategy strategy,
            String uri) throws JMSException {
        return newBrokerInstance(strategy, null, null, uri);
    }

    public static Broker newBrokerInstance(BrokerClientStrategy strategy,
            String username, String password, String uri) throws JMSException {
        return new AMQBrokerImpl(strategy, username, password, uri);
    }

}
