package org.example.swen3backend.service;

public class CollectionNotFoundException extends RuntimeException {
    public CollectionNotFoundException(long id) {

        super("Sammlung mit ID " + id + " wurde nicht gefunden.");
    }
}
