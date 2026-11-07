package org.example.swen3backend.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Service
public class DocumentStorage {
    private final S3Client client;
    private final String bucket;

    public DocumentStorage(S3Client client, @Value("${storage.bucket}") String bucket) {
        this.client = client;
        this.bucket = bucket;
    }

    public String getBucket() {
        return bucket;
    }

    public void upload(String objectKey, byte[] content) {
        try {
            ensureBucket();
            client.putObject(request -> request.bucket(bucket).key(objectKey)
                    .contentType("application/pdf"), RequestBody.fromBytes(content));
        } catch (SdkException exception) {
            throw new StorageException("Could not store PDF in RustFS", exception);
        }
    }

    public byte[] download(String objectKey) {
        try {
            return client.getObjectAsBytes(request -> request.bucket(bucket).key(objectKey)).asByteArray();
        } catch (SdkException exception) {
            throw new StorageException("Could not download PDF from RustFS", exception);
        }
    }

    public void delete(String objectKey) {
        try {
            client.deleteObject(request -> request.bucket(bucket).key(objectKey));
        } catch (SdkException exception) {
            throw new StorageException("Could not delete PDF from RustFS", exception);
        }
    }

    private void ensureBucket() {
        try {
            client.headBucket(request -> request.bucket(bucket));
        } catch (S3Exception exception) {
            if (exception.statusCode() != 404) {
                throw exception;
            }
            try {
                client.createBucket(request -> request.bucket(bucket));
            } catch (S3Exception creationException) {
                // Another upload may have created our bucket in the meantime.
                if (!"BucketAlreadyOwnedByYou".equals(creationException.awsErrorDetails().errorCode())) {
                    throw creationException;
                }
            }
        }
    }
}
