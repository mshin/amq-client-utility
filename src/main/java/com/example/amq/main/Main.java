package com.example.amq.main;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.TextMessage;

import com.example.amq.client.Broker;
import com.example.amq.client.BrokerFactory;
import com.example.amq.client.MessageHandler;

/**
 * Command line runnable AMQ client.
 * @author mshin, Ian McMahon
 *
 */
public class Main {
    // TODO implement parameter for interval sleep between
    // caughtMessage/sentMessage
    // TODO implement postSend hook.
    // TODO implement sending more than just text message (probably from file?)
    // This should probably be different from the encoder.

    // TODO implement parameters for messages sent, like ttl (timeToLive).

    // used by printMessage(Message message, String messagePrefix)
    private static AtomicInteger count = new AtomicInteger(0);
    private static volatile String decoderClassname = null;
    private static volatile String encoderClassname = null;

    public static void main(String[] args) throws JMSException {

        if (args.length < 3) {
            usage();
            System.exit(0);
        }

        boolean isConsumer = false;
        String corp = String.valueOf(args[0]).toLowerCase();
        if ("p".equals(corp) || "producer".equals(corp)) {
            isConsumer = false;
        } else if ("c".equals(corp) || "consumer".equals(corp)) {
            isConsumer = true;
        } else {
            exit(corp);
        }

        String uri = String.valueOf(args[1]);
        String destination = String.valueOf(args[2]);

        String username = null;
        String password = null;

        String message = null;

        for (int i = 3; i < args.length; i++) {
            String param = String.valueOf(args[i]);

            if (!param.startsWith("-")) {
                exit(param);
            }

            String pi1 = String.valueOf(param.charAt(1)).toLowerCase();
            switch (pi1) {
            case "u":
                if (null == username)
                    username = String.valueOf(param.substring(2));
                else
                    exit(param + ". Duplicate arg.");
                break;
            case "p":
                if (null == password)
                    password = String.valueOf(param.substring(2));
                else
                    exit(param + ". Duplicate arg.");
                break;
            case "m":
                if (null == message)
                    message = String.valueOf(param.substring(2));
                else
                    exit(param + ". Duplicate arg.");
                break;
            case "d":
                if (null == decoderClassname)
                    decoderClassname = String.valueOf(param.substring(2));
                else
                    exit(param + ". Duplicate arg.");
                break;
            case "e":
                if (null == encoderClassname)
                    encoderClassname = String.valueOf(param.substring(2));
                else
                    exit(param + ". Duplicate arg.");
                break;
            default:
                exit(param);
                break;
            }

        }

        // XOR operator (^)
        if (null == username ^ null == password) {
            System.err
                    .println("Must specify both username and password, or neither of them.");
            usage();
            System.exit(0);
        }

        final AtomicBoolean shutdown = new AtomicBoolean(false);
        final CountDownLatch latch = new CountDownLatch(1);

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

            @Override
            public void run() {
                shutdown.set(true);
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    System.err.println("Interrupted. " + e.getMessage());
                }
                System.out.println("Program Terminated.");
            }

        }));

        Broker broker = null;

        try {
            System.out.println("Attempting to create broker with uri: " + uri
                    + ", un:" + username + " pw:" + password);
            broker = BrokerFactory.newBrokerInstance(username, password, uri);

            if (isConsumer) {
                if (null != message) {
                    System.out.println();
                    System.out
                            .println("Creating async consumer on destination with uri: "
                                    + uri + "...");
                    System.out.println();

                    MessageHandler handler = new MainMessageHandler(message);
                    MessageConsumer consumer = broker.getListeningConsumer(
                            broker.getDestination(destination), handler);

                    while (!shutdown.get()) {

                        try {
                            Thread.sleep(1000L);
                        } catch (InterruptedException e) {
                            System.err
                                    .println("Interrupted. " + e.getMessage());
                        }
                    }

                } else {
                    System.out.println();
                    System.out
                            .println("Creating sync consumer on destination with uri: "
                                    + uri + "...");
                    System.out.println();

                    MessageConsumer consumer = broker.getConsumer(broker
                            .getDestination(destination));

                    while (!shutdown.get()) {

                        Message msg = consumer.receive(1000L);
                        printMessage(msg, null);

                        if (null != msg) {
                            try {
                                Thread.sleep(1000L);
                            } catch (InterruptedException e) {
                                System.err.println("Interrupted. "
                                        + e.getMessage());
                            }
                        }
                    }
                }

            } else {// is producer
                System.out.println();
                System.out
                        .println("Creating producer on destination with uri: "
                                + uri + "...");
                System.out.println();

                MessageProducer producer = broker.getProducer(broker
                        .getDestination(destination));

                // TODO do something with the messageEncoder

                int count = 0;
                while (!shutdown.get()) {
                    sendMessage(message, broker, producer);

                    System.out.println("Sent "
                            + (encoderClassname == null ? "text" : "encoded")
                            + " message " + count + " to " + uri + ".");
                    try {
                        Thread.sleep(1000L);
                    } catch (InterruptedException e) {
                        System.err.println("Interrupted. " + e.getMessage());
                    }

                    count++;
                }
            }

        } finally {
            if (null != broker)
                broker.close();
            System.out.println("Shutdown complete.");
            latch.countDown();
        }

    }

    public static void usage() {
        System.out
                .println("[producer|consumer (c/p)] [url] [destinationName] -u(username) -p(password) -m(message) -d(decoder pkg.class) -e(encoder pkg.class)");
        System.out.println("The first 3 arguments are mandatory.");
        System.out
                .println("If you want to read your message, implement the com.example.amq.main.MessageDecoder"
                        + " interface and place the impl in this directory.");
        System.out
                .println("If you want to encrypt your message, implement the com.example.amq.main.MessageEncoder"
                        + " interface and place the impl in this directory.");
    }

    public static void exit(String param) {
        System.err.println("ERROR with param: " + param);
        usage();
        System.exit(0);
    }

    private static class MainMessageHandler implements MessageHandler {

        private String messagePrefix;

        public MainMessageHandler(String messagePrefix) {
            this.messagePrefix = messagePrefix;
        }

        @Override
        public void handleMessage(Message message) {

            printMessage(message, messagePrefix);
        }

    }

    /**
     * Prints a message to the terminal, delagating to the decoder, if one is specified
     */
    public static void printMessage(Message message, String messagePrefix) {
        if (null == message)
            return;

        String id = "";
        String type = "";
        Long timestamp = null;
        Long expiration = null;

        String messageContent = null;

        try {
            id = message.getJMSMessageID();

        } catch (JMSException e) {
            System.err.println("Error while attempting to retrieve message "
                    + "id.");
            System.err.println(e.getMessage());
        }

        try {
            type = message.getJMSType();

        } catch (JMSException e) {
            System.err.println("Error while attempting to retrieve message "
                    + "type.");
            System.err.println(e.getMessage());
        }

        try {
            timestamp = message.getJMSTimestamp();

        } catch (JMSException e) {
            System.err.println("Error while attempting to retrieve message "
                    + "timestamp.");
            System.err.println(e.getMessage());
        }

        try {
            expiration = message.getJMSExpiration();

        } catch (JMSException e) {
            System.err.println("Error while attempting to retrieve message "
                    + "expiration.");
            System.err.println(e.getMessage());
        }

        if (null != decoderClassname) {
            Class<?> clazz = null;
            try {
                clazz = Class.forName(decoderClassname);
            } catch (ClassNotFoundException e) {
                System.err
                        .println("Error while attempting to retrieve decoder class "
                                + decoderClassname + ".");
                System.err.println(e.getMessage());
            }

            Object decoderObject = null;
            if (null != clazz) {
                try {
                    decoderObject = clazz.newInstance();
                } catch (InstantiationException | IllegalAccessException e) {
                    System.err.println("Error while attempting to reflect "
                            + decoderClassname + ".");
                    System.err.println(e.getMessage());
                }
            }

            MessageDecoder decoder = null;
            if (null != decoderObject) {
                try {
                    decoder = (MessageDecoder) decoderObject;
                } catch (ClassCastException e) {
                    System.err.println(decoderClassname
                            + " does not implement interface MessageDecoder.");
                    System.err.println(e.getMessage());
                }
            }

            if (null != decoder) {
                try {
                    messageContent = decoder.decode(message);
                } catch (JMSException e) {
                    System.err
                            .println("Error while attempting to decode message with decoder class "
                                    + decoderClassname + ".");
                    System.err.println(e.getMessage());
                }
            }
        }

        if (null != messagePrefix)
            System.out.print(messagePrefix);
        ;
        System.out.printf("#:%03d id:%s type:%s ts:%d exp:%d \n",
                count.intValue(), id, type, timestamp, expiration);
        if (null != messageContent) {
            System.out.println(messageContent);
        }

        count.incrementAndGet();
    }

    /**
     * Sends a message to a broker using a MessageProducer, delegating to the encoder if specified.
     */
    private static void sendMessage(String message, Broker broker,
            MessageProducer producer) throws JMSException {
        Message m = null;

        if (null != encoderClassname) {
            Class<?> clazz = null;
            try {
                clazz = Class.forName(encoderClassname);
            } catch (ClassNotFoundException e) {
                System.err
                        .println("Error while attempting to retrieve encoder class "
                                + encoderClassname + ".");
                System.err.println(e.getMessage());
            }

            Object encoderObject = null;
            if (null != clazz) {
                try {
                    encoderObject = clazz.newInstance();
                } catch (InstantiationException | IllegalAccessException e) {
                    System.err.println("Error while attempting to reflect "
                            + encoderClassname + ".");
                    System.err.println(e.getMessage());
                }
            }

            MessageEncoder encoder = null;
            if (null != encoderObject) {
                try {
                    encoder = (MessageEncoder) encoderObject;
                } catch (ClassCastException e) {
                    System.err.println(encoderClassname
                            + " does not implement interface MessageEncoder.");
                    System.err.println(e.getMessage());
                }
            }

            if (null != encoder) {
                try {
                    m = encoder.encode(message, broker);
                } catch (JMSException e) {
                    System.err
                            .println("Error while attempting to encode message with encoder class "
                                    + encoderClassname + ".");
                    System.err.println(e.getMessage());
                }
            }
        } else {
            m = broker.createTextMessage();
            ((TextMessage) m).setText(message);
        }
        producer.send(m);
    }
}
