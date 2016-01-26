package com.example.amq.client.strategy;

import java.io.InputStream;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;

import org.apache.activemq.BlobMessage;
import org.apache.activemq.Closeable;

import com.example.amq.client.AMQBroker;
import com.example.amq.client.MessageHandler;

/**
 * Closable interface for implementing a strategy to work for an AMQ client
 * 
 * @author mshin
 *
 */
public interface BrokerClientStrategy extends Closeable {
    
    /**
     * Sets the AMQBroker for this strategy
     */
    void setBroker(AMQBroker broker);

    /**
     * Initializes the strategy with various objects
     */
    void init(Object... objects) throws JMSException;

    /**
     * Returns the JMS connection using the default username and password
     */
    Connection getConnection() throws JMSException;

    /**
     * Returns the JMS connection using the specified username and password
     */
    Connection getConnection(String username, String password)
            throws JMSException;
    
    /**
     * Returns the JMS session
     */
    Session getSession() throws JMSException;

    /**
     * Returns the JMS Destination with the specified name
     */
    Destination getDestination(String destinationName) throws JMSException;

    /**
     * Returns a JMS message producer on the specified destination
     */
    MessageProducer getProducer(Destination destination) throws JMSException;

    /**
     * Returns a JMS message consumer on the specified destination
     */
    MessageConsumer getConsumer(Destination destination) throws JMSException;

    /**
     * Returns a JMS message consumer on the specified destination with an asynchronous message handler
     */
    MessageConsumer getListeningConsumer(Destination destination,
            MessageHandler messageHandler) throws JMSException;

    /**
     * Returns an empty JMS BytesMessage
     */
    BytesMessage createBytesMessage() throws JMSException;

    /**
     * Returns an empty JMS ObjectMessage
     */
    ObjectMessage createObjectMessage() throws JMSException;

    /**
     * Returns an empty JMS TestMessage
     */
    TextMessage createTextMessage() throws JMSException;

    /**
     * Returns an empty JMS MapMessage
     */
    MapMessage createMapMessage() throws JMSException;

    /**
     * Returns an empty JMS StreamMessage
     */
    StreamMessage createStreamMessage() throws JMSException;

    /**
     * Returns an ActiveMQ BlobMessage with the specified inputStream
     */
    BlobMessage createBlobMessage(InputStream inputStream) throws JMSException;
}