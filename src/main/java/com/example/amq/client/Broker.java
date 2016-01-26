package com.example.amq.client;

import javax.jms.BytesMessage;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;

import org.apache.activemq.Closeable;

/**
 * Simple interface for a Broker client
 */
public interface Broker extends Closeable {
    
    /**
     * Returns a queue with the given name
     */
    Destination getDestination(String queue) throws JMSException;

    /**
     * Returns a message producer on the given destination
     */
    MessageProducer getProducer(Destination destination) throws JMSException;

    /**
     * Returns a message consumer on the given destination
     */
    MessageConsumer getConsumer(Destination destination) throws JMSException;

    /**
     * Returns a message consumer on the given destination that delegates its onMessage to messageHandler
     */
    MessageConsumer getListeningConsumer(Destination destination,
            MessageHandler messageHandler) throws JMSException;

    /**
     * Creates an empty JMS BytesMessage
     */
    BytesMessage createBytesMessage() throws JMSException;

    /**
     * Creates an empty JMS ObjectMessage
     */
    ObjectMessage createObjectMessage() throws JMSException;

    /**
     * Creates an empty JMS TextMessage
     */
    TextMessage createTextMessage() throws JMSException;
}
