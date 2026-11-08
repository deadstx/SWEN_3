
package at.technikum.swen3backend.exception;

public class MessageQueueException extends RuntimeException {
    public MessageQueueException(String message, Throwable cause) {
        super(message, cause);
    }
}