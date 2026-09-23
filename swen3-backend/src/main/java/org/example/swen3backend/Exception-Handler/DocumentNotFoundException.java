package org.example.swen3backend.service;

public class DocumentNotFoundException extends RuntimeException {

    public DocumentNotFoundException(long id) {


        super("Dokument mit ID " + id + " wurde nicht gefunden.");
    }
}
