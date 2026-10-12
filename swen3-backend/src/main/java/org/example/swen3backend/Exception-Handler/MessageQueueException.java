
package org.example.swen3backend.service;

public class MessageQueueException extends RuntimeException {
    public MessageQueueException(String message, Throwable cause) {
        super(message, cause);
    }
}