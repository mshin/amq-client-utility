package com.example.amq.client;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Session;
import javax.jms.ConnectionFactory;
import javax.jms.StreamMessage;

/**
 * Advanced interface for accessing JMS internals or more complicated messages for a Broker
 */
public interface AdvancedBroker extends Broker {
   
    /**
     * Returns a JMS ConnectionFactory for this broker
     */
    ConnectionFactory getConnectionFactory() throws JMSException;

    /**
     * Returns a JMS Connection for this broker
     */
    Connection getConnection() throws JMSException;

    /**
     * Returns a JMS Connection with the specified username and password for this broker
     */
    Connection getConnection(String username, String password)
            throws JMSException;

    /**
     * Returns a JMS Session for this broker
     */
    Session getSession() throws JMSException;

    /**
     * Returns an empty JMS MapMessage
     */
    MapMessage createMapMessage() throws JMSException;

    /**
     * Returns an empty JMS StreamMessage
     */
    StreamMessage createStreamMessage() throws JMSException;
}
