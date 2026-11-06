package org.example.swen3ocrworker;

public record OcrMessage(long documentId, String fileName, String bucket, String objectKey) {}
