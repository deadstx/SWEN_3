package at.technikum.swen3backend.Controller;

import lombok.extern.slf4j.Slf4j;
import at.technikum.swen3backend.exception.CollectionNotFoundException;
import at.technikum.swen3backend.exception.DocumentNotFoundException;
import at.technikum.swen3backend.exception.InvalidDocumentException;
import at.technikum.swen3backend.exception.MessageQueueException;
import at.technikum.swen3backend.storage.StorageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(StorageException.class)
    public ProblemDetail handleStorage(StorageException exception) {
        log.error("Document storage error", exception);
        return ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE,
                "Der Dokumentenspeicher ist derzeit nicht erreichbar.");
    }

    @ExceptionHandler({DocumentNotFoundException.class, CollectionNotFoundException.class})
    public ProblemDetail handleNotFound(RuntimeException exception) {
        log.warn("Resource not found: {}", exception.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(InvalidDocumentException.class)
    public ProblemDetail handleInvalidDocument(InvalidDocumentException exception) {
        log.warn("Invalid document: {}", exception.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(MessageQueueException.class)
    public ProblemDetail handleMessageQueue(MessageQueueException exception) {
        log.error("Message queue error", exception);
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Die Nachrichtenwarteschlange ist derzeit nicht erreichbar."
        );
    }
}
