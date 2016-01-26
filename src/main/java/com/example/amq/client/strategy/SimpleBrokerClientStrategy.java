package com.example.amq.client.strategy;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.AbstractMap.SimpleEntry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQSession;
import org.apache.activemq.BlobMessage;

import com.example.amq.client.AMQBroker;
import com.example.amq.client.MessageHandler;
import com.example.amq.exception.AMQBrokerException;
import com.example.amq.exception.MethodNotImplementedException;

/**
 * Simple strategy for an AMQ client. Uses a single connection and session.
 * session.autoAcknowledge=true, connection.alwaysSessionAsync=false.
 * 
 * @author mshin, Ian McMahon
 *
 */
public class SimpleBrokerClientStrategy implements BrokerClientStrategy {

    /**
     * Default timeout for messages
     */
    private static final long DEFAULT_MESSAGE_TTL = 60000;

    private AMQBroker broker;

    /**
     * Message processing thread pool
     */
    private ExecutorService threadPool;

    @Override
    public void init(Object... objects) throws AMQBrokerException {

        broker.setCollections(new ArrayList<Entry<String, Connection>>(1),
                new ArrayList<Entry<String, Session>>(1),
                new ArrayList<Entry<String, MessageConsumer>>(),
                new ArrayList<Entry<String, MessageProducer>>(),
                new ArrayList<Entry<String, Destination>>());

        ActiveMQConnectionFactory connectionFactory = (ActiveMQConnectionFactory) objects[0];

        // We are using a single thread architecture for this strategy which
        // requires:
        // 1 Connection, 1 Session, session.autoAcknowledge=true,
        // alwaysSessionAsync=false.
        connectionFactory.setAlwaysSessionAsync(false);

        connectionFactory.setConnectionIDPrefix(connectionFactory.getUserName()
                + ".");

        threadPool = Executors.newCachedThreadPool();

        Connection connection;
        try {
            connection = connectionFactory.createConnection();
            broker.getConnections().add(
                    new SimpleEntry<String, Connection>("", connection));
        } catch (JMSException e) {
            throw new AMQBrokerException("Connection creation failed: "
                    + e.getMessage());
        }

        try {
            Session session = connection.createSession(false,
                    Session.AUTO_ACKNOWLEDGE);
            broker.getSessions().add(
                    new SimpleEntry<String, Session>("", session));
        } catch (JMSException e) {
            throw new AMQBrokerException("Session creation failed: "
                    + e.getMessage());
        }

        try {
            connection.start();
        } catch (JMSException e) {
            throw new AMQBrokerException("Starting session failed: "
                    + e.getMessage());
        }

    }

    /**
     * Always returns the single connection used with this strategy.
     */
    @Override
    public Connection getConnection() {
        Connection connection = null;
        Iterator<Entry<String, Connection>> iterator = broker.getConnections()
                .iterator();
        while (iterator.hasNext()) {
            Entry<String, Connection> entry = iterator.next();
            if (null != entry) {
                connection = entry.getValue();
            }
        }
        return connection;
    }

    /**
     * Not implemented with this implementation of AMQ Client Jar.
     */
    @Override
    public Connection getConnection(String username, String password)
            throws MethodNotImplementedException {
        throw new MethodNotImplementedException(
                "Cannot get connection with specific username and password using this implementation");
    }

    /**
     * Returns the single Session used with this strategy.
     */
    @Override
    public Session getSession() {
        Session session = null;
        Iterator<Entry<String, Session>> iterator = broker.getSessions()
                .iterator();
        while (iterator.hasNext()) {
            Entry<String, Session> entry = iterator.next();
            if (null != entry) {
                session = entry.getValue();
            }
        }
        return session;
    }

    /**
     * Returns a pooled Destination, or a new Destination if one is not found in
     * the pool.
     */
    @Override
    public Destination getDestination(String queueName)
            throws AMQBrokerException {
        Destination destination = null;
        Iterator<Entry<String, Destination>> iterator = broker
                .getDestinations().iterator();
        while (iterator.hasNext()) {
            Entry<String, Destination> entry = iterator.next();
            if (null != entry.getKey() && entry.getKey().equals(queueName)) {
                destination = entry.getValue();
            }
        }
        if (null == destination) {
            try {
                destination = broker.getSession().createQueue(queueName);
                Entry<String, Destination> entry = new SimpleEntry<String, Destination>(
                        queueName, destination);
                broker.getDestinations().add(entry);
            } catch (JMSException e) {
                throw new AMQBrokerException("Destination creation failed: "
                        + e.getMessage());
            }
        }

        return destination;
    }

    /**
     * Always returns a new MessageProducer.
     */
    @Override
    public MessageProducer getProducer(Destination destination)
            throws AMQBrokerException {
        try {
            Session session = broker.getSession();
            MessageProducer producer = session.createProducer(destination);
            producer.setTimeToLive(DEFAULT_MESSAGE_TTL);
            return producer;
        } catch (JMSException e) {
            throw new AMQBrokerException("Producer creation failed: "
                    + e.getMessage());
        }
    }

    /**
     * Always returns a new MessageConsumer.
     */
    @Override
    public MessageConsumer getConsumer(Destination destination)
            throws AMQBrokerException {

        try {
            Session session = broker.getSession();
            return session.createConsumer(destination);
        } catch (JMSException e) {
            throw new AMQBrokerException("Consumer creation failed: "
                    + e.getMessage());
        }
    }

    /**
     * Always returns a new MessageConsumer. Adds a listener to the consumer
     * where: listener.onMessage() method calls handler.handleMessage().
     */
    @Override
    public MessageConsumer getListeningConsumer(Destination destination,
            final MessageHandler messageHandler) throws JMSException {

        MessageConsumer consumer = broker.getConsumer(destination);

        consumer.setMessageListener(new MessageListener() {

            @Override
            public void onMessage(Message message) {
                threadPool
                        .submit(new MessageProcessor(message, messageHandler));
            }

        });
        return consumer;
    }

    @Override
    public BytesMessage createBytesMessage() throws AMQBrokerException {
        try {
            Session session = broker.getSession();
            return session.createBytesMessage();
        } catch (JMSException e) {
            throw new AMQBrokerException("BytesMessage creation failed: "
                    + e.getMessage());
        }
    }

    @Override
    public ObjectMessage createObjectMessage() throws AMQBrokerException {
        try {
            Session session = broker.getSession();
            return session.createObjectMessage();
        } catch (JMSException e) {
            throw new AMQBrokerException("ObjectMessage creation failed: "
                    + e.getMessage());
        }
    }

    @Override
    public TextMessage createTextMessage() throws AMQBrokerException {
        try {
            Session session = broker.getSession();
            return session.createTextMessage();
        } catch (JMSException e) {
            throw new AMQBrokerException("TextMessage creation failed: "
                    + e.getMessage());
        }
    }

    @Override
    public MapMessage createMapMessage() throws AMQBrokerException {
        try {
            Session session = broker.getSession();
            return session.createMapMessage();
        } catch (JMSException e) {
            throw new AMQBrokerException("MapMessage createion failed: "
                    + e.getMessage());
        }
    }

    @Override
    public StreamMessage createStreamMessage() throws AMQBrokerException {
        try {
            Session session = broker.getSession();
            return session.createStreamMessage();
        } catch (JMSException e) {
            throw new AMQBrokerException("StreamMessage createion failed: "
                    + e.getMessage());
        }
    }

    @Override
    public BlobMessage createBlobMessage(InputStream inputStream)
            throws AMQBrokerException {
        try {
            Session session = broker.getSession();
            return ((ActiveMQSession) session).createBlobMessage(inputStream);
        } catch (JMSException e) {
            throw new AMQBrokerException("BlobMessage createion failed: "
                    + e.getMessage());
        }
    }

    @Override
    public void close() throws AMQBrokerException {

        Iterator<Entry<String, MessageProducer>> producerIterator = broker
                .getMessageProducers().iterator();
        while (producerIterator.hasNext()) {
            Entry<String, MessageProducer> entry = producerIterator.next();
            MessageProducer producer = null;
            if (null != entry){
                producer = entry.getValue();
            }
            if (null != producer) {
                try {
                    producer.close();
                } catch (JMSException e) {
                    throw new AMQBrokerException("Producer closing failed: "
                            + e.getMessage());
                }
            }
        }

        Iterator<Entry<String, MessageConsumer>> consumerIterator = broker
                .getMessageConsumers().iterator();
        while (consumerIterator.hasNext()) {
            Entry<String, MessageConsumer> entry = consumerIterator.next();
            MessageConsumer consumer = null;
            if (null != entry){
                consumer = entry.getValue();
            }
            if (null != consumer) {
                try {
                    consumer.close();
                } catch (JMSException e) {
                    throw new AMQBrokerException("Consumer closing failed: "
                            + e.getMessage());
                }
            }
        }

        try {
            broker.getSession().close();
        } catch (JMSException e) {
            throw new AMQBrokerException("Session closing failed: "
                    + e.getMessage());
        }

        try {
            broker.getConnection().close();
        } catch (JMSException e) {
            throw new AMQBrokerException("Connection closing failed: "
                    + e.getMessage());

        }

        threadPool.shutdown();

    }

    @Override
    public void setBroker(AMQBroker broker) {
        this.broker = broker;

    }

    /**
     * Runnable thread for dispatching consumer onMessage to the MessageHandler's handleMessage
     * @author mshin
     *
     */
    private class MessageProcessor implements Runnable {

        private Message message;
        private MessageHandler messageHandler;

        public MessageProcessor(Message message, MessageHandler messageHandler) {
            this.message = message;
            this.messageHandler = messageHandler;
        }

        @Override
        public void run() {
            messageHandler.handleMessage(message);
        }
    }
}
