package com.example.amq.main;

import javax.jms.JMSException;
import javax.jms.Message;

import com.example.amq.client.Broker;

/**
 * Encodes a String to a JMS message. Needs access to the broker for
 * instantiating empty Messages.
 * 
 * @author Ian McMahon
 *
 */
public interface MessageEncoder {
    Message encode(String message, Broker broker) throws JMSException;
}
