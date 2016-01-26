package com.example.amq.client;

import javax.jms.Message;

/**
 * Interface used for asynchronous JMS message handling 
 */
public interface MessageHandler {
    /**
     * Process the message
     */
    void handleMessage(Message message);
}
