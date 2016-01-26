package com.example.amq.main;

import javax.jms.JMSException;
import javax.jms.Message;

/**
 * Decodes a JMS message into a String
 * @author mshin
 *
 */
public interface MessageDecoder {
    String decode(Message message) throws JMSException;
}
