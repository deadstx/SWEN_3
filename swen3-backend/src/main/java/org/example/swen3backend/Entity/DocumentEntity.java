package org.example.swen3backend.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "documents")
@Getter
@NoArgsConstructor
public class DocumentEntity {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false)
    private long fileSize;

    @Column(nullable = false)
    private Instant uploadedAt;

    @Column(nullable = false)
    private byte[] content;

    public DocumentEntity(String name, byte[] content) {
        this.name = name;
        this.content = content;
        this.fileSize = content.length;
        this.uploadedAt = Instant.now();
    }
}