package com.example.amq.exception;

import javax.jms.JMSException;

/**
 * Wrapper for JMSExceptions caused by methods not being implemented
 * @author mshin
 *
 */
public class MethodNotImplementedException extends JMSException {

    private static final long serialVersionUID = 8586581365418467135L;

    public MethodNotImplementedException(String reason) {
        super(reason);
        // TODO Auto-generated constructor stub
    }

    public MethodNotImplementedException(String reason, String errorCode) {
        super(reason, errorCode);
        // TODO Auto-generated constructor stub
    }

}
