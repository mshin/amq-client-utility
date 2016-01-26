package com.example.amq.client;

import java.io.InputStream;
import java.util.Collection;
import java.util.Map.Entry;

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

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.BlobMessage;
import org.apache.activemq.Closeable;

import com.example.amq.client.strategy.BrokerClientStrategy;

/**
 * Implementation of the 3 Broker interfaces that delegates to a BrokerClientStrategy
 */
public class AMQBrokerImpl implements AMQBroker, AdvancedBroker, Broker,
        Closeable {

    private BrokerClientStrategy strategy;
    private ActiveMQConnectionFactory connectionFactory;
    private Collection<Entry<String, Connection>> connections;
    private Collection<Entry<String, Session>> sessions;
    private Collection<Entry<String, MessageConsumer>> consumers;
    private Collection<Entry<String, MessageProducer>> producers;
    private Collection<Entry<String, Destination>> destinations;

    // TODO need to implement CLI and test

    /*
     * package level visibility
     */
    AMQBrokerImpl(BrokerClientStrategy strategy, String username,
            String password, String brokerUri) throws JMSException {
        this.strategy = strategy;
        strategy.setBroker(this);

        this.connectionFactory = this.createConnectionFactory(username,
                password, brokerUri);
        this.strategy.init(connectionFactory);
    }

    /**
     * Creates a connectionFactory using the specified username, password, and URL
     */
    private ActiveMQConnectionFactory createConnectionFactory(String username,
            String password, String brokerUrl) {
        ActiveMQConnectionFactory amqConnectionFactory = null;
        if (null == username || null == password) {
            amqConnectionFactory = new ActiveMQConnectionFactory(brokerUrl);
        } else {
            amqConnectionFactory = new ActiveMQConnectionFactory(username,
                    password, brokerUrl);
        }
        return amqConnectionFactory;
    }

    @Override
    public Destination getDestination(String queue) throws JMSException {
        return strategy.getDestination(queue);
    }

    @Override
    public MessageProducer getProducer(Destination destination)
            throws JMSException {
        return strategy.getProducer(destination);
    }

    @Override
    public MessageConsumer getConsumer(Destination destination)
            throws JMSException {
        return strategy.getConsumer(destination);
    }

    @Override
    public MessageConsumer getListeningConsumer(Destination destination,
            MessageHandler messageHandler) throws JMSException {
        return strategy.getListeningConsumer(destination, messageHandler);
    }

    @Override
    public Connection getConnection() throws JMSException {
        return strategy.getConnection();
    }

    @Override
    public Connection getConnection(String username, String password)
            throws JMSException {
        return strategy.getConnection();
    }

    @Override
    public Session getSession() throws JMSException {
        return strategy.getSession();
    }

    @Override
    public BytesMessage createBytesMessage() throws JMSException {
        return strategy.createBytesMessage();
    }

    @Override
    public ObjectMessage createObjectMessage() throws JMSException {
        return strategy.createObjectMessage();
    }

    @Override
    public TextMessage createTextMessage() throws JMSException {
        return strategy.createTextMessage();
    }

    @Override
    public MapMessage createMapMessage() throws JMSException {
        return strategy.createMapMessage();
    }

    @Override
    public StreamMessage createStreamMessage() throws JMSException {
        return strategy.createStreamMessage();
    }

    @Override
    public BlobMessage createBlobMessage(InputStream inputStream)
            throws JMSException {
        return strategy.createBlobMessage(inputStream);
    }

    @Override
    public String getUri() {
        return connectionFactory.getBrokerURL();
    }

    @Override
    public ActiveMQConnectionFactory getConnectionFactory() {
        return this.connectionFactory;
    }

    @Override
    public Collection<Entry<String, Connection>> getConnections() {
        return this.connections;
    }

    @Override
    public Collection<Entry<String, Session>> getSessions() {
        return this.sessions;
    }

    @Override
    public Collection<Entry<String, MessageConsumer>> getMessageConsumers() {
        return this.consumers;
    }

    @Override
    public Collection<Entry<String, MessageProducer>> getMessageProducers() {
        return this.producers;
    }

    @Override
    public Collection<Entry<String, Destination>> getDestinations() {
        return this.destinations;
    }

    @Override
    public void close() throws JMSException {
        strategy.close();
        // TODO

    }

    @Override
    public void setCollections(
            Collection<Entry<String, Connection>> connections,
            Collection<Entry<String, Session>> sessions,
            Collection<Entry<String, MessageConsumer>> consumers,
            Collection<Entry<String, MessageProducer>> producers,
            Collection<Entry<String, Destination>> destinations) {
        this.connections = connections;
        this.sessions = sessions;
        this.consumers = consumers;
        this.producers = producers;
        this.destinations = destinations;
    }

}
