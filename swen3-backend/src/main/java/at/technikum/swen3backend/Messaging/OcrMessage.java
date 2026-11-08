package at.technikum.swen3backend.Messaging;

public record OcrMessage(long documentId, String fileName, String bucket, String objectKey) {}
