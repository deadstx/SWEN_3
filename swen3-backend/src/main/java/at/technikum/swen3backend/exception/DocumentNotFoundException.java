package at.technikum.swen3backend.exception;

public class DocumentNotFoundException extends RuntimeException {

    public DocumentNotFoundException(long id) {


        super("Dokument mit ID " + id + " wurde nicht gefunden.");
    }
}
