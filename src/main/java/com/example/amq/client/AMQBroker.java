package com.example.amq.client;

import java.io.InputStream;
import java.util.Collection;
import java.util.Map.Entry;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.BlobMessage;

/**
 * Interface for accessing ActiveMQ internals and messages 
 */
public interface AMQBroker extends AdvancedBroker {

    /**
     * Returns the URI used to connect to the broker
     */
    String getUri();

    /**
     * Returns the ActiveMQConnecitonFactory used to connect to the broker
     */
    ActiveMQConnectionFactory getConnectionFactory() throws JMSException;

    /**
     * Returns all connections to the broker
     */
    Collection<Entry<String, Connection>> getConnections();

    /**
     * Returns all session established with the broker
     */
    Collection<Entry<String, Session>> getSessions();

    /**
     * Returns all message consumers on the broker
     */
    Collection<Entry<String, MessageConsumer>> getMessageConsumers();

    /**
     * Returns all message producers on the broker
     */
    Collection<Entry<String, MessageProducer>> getMessageProducers();

    /**
     * Returns all destinations on the broker
     */
    Collection<Entry<String, Destination>> getDestinations();

    /**
     * Returns an active connection with the broker
     */
    Connection getConnection() throws JMSException;

    /**
     * Returns an active session with the broker
     */
    Session getSession() throws JMSException;

    /**
     * Returns a JMS queue with the specified name
     */
    Destination getDestination(String queue) throws JMSException;

    /**
     * Returns a Message producer on the specified destination 
     */
    MessageProducer getProducer(Destination destination) throws JMSException;

    /**
     * Returns a Message consumer on the specified destination 
     */
    MessageConsumer getConsumer(Destination destination) throws JMSException;

    /**
     * Returns an ActiveMQ BlobMessage witht he given inputStream
     */
    BlobMessage createBlobMessage(InputStream inputStream) throws JMSException;

    // void setConnections(Collection<Entry<String, Connection>> connections);
    // void setSessions(Collection<Entry<String, Session>> sessions);
    // void setMessageConsumers(Collection<Entry<String, MessageConsumer>>
    // messageConsumers);
    // void setMessageProducers(Collection<Entry<String, MessageProducer>>
    // messageProducers);
    // void setDestinations(Collection<Destination> destinations);

    /**
     * Single setter for all JMS collections 
     */
    void setCollections(Collection<Entry<String, Connection>> connections,
            Collection<Entry<String, Session>> sessions,
            Collection<Entry<String, MessageConsumer>> consumers,
            Collection<Entry<String, MessageProducer>> producers,
            Collection<Entry<String, Destination>> destinations);
}
