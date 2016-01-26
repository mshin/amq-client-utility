package com.example.amq.exception;

import javax.jms.JMSException;

/**
 * Wrapper for general JMSExceptions caused by this client package
 * @author Ian McMahon
 *
 */
public class AMQBrokerException extends JMSException {

    private static final long serialVersionUID = -1656122886071405726L;

    public AMQBrokerException(String reason) {
        super(reason);
        // TODO Auto-generated constructor stub
    }

    public AMQBrokerException(String reason, String errorCode) {
        super(reason, errorCode);
        // TODO Auto-generated constructor stub
    }
}
