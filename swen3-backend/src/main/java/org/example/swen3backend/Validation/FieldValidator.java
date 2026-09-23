package org.example.swen3backend.validation;

import org.example.swen3backend.service.InvalidDocumentException;
import org.springframework.stereotype.Component;

@Component
public class FieldValidator {

    public String validateAndStrip(String value, String fieldLabel) {
        if (value == null) {
            throw new InvalidDocumentException(fieldLabel + " darf nicht null sein.");
        }
        String stripped = value.strip();
        if (stripped.isBlank() || stripped.length() > 255) {
            throw new InvalidDocumentException(
                    fieldLabel + " muss zwischen 1 und 255 Zeichen enthalten."
            );
        }
        return stripped;
    }
}