package org.example.swen3backend.messaging;

public record OcrMessage(long documentId, String fileName) {}