package at.technikum.swen3backend.exception;

public class CollectionNotFoundException extends RuntimeException {
    public CollectionNotFoundException(long id) {

        super("Sammlung mit ID " + id + " wurde nicht gefunden.");
    }
}
