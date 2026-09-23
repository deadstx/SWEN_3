package org.example.swen3backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "documents")
@Getter
@NoArgsConstructor
public class DocumentEntity {

    @Id
    @GeneratedValue
    private Long id;

    @Setter
    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false)
    private long fileSize;

    @Column(nullable = false)
    private Instant uploadedAt;

    @Column(nullable = false)
    private byte[] content;

    @ElementCollection
    //Sammlung einfacher Werte (String), die zur DocumentEntity gehört
    @CollectionTable(name = "document_tags", joinColumns = @JoinColumn(name = "document_id"))
    @OrderColumn(name = "tag_order")
    @Column(name = "tag", nullable = false, length = 255)
    private List<String> tags = new ArrayList<>();

    public DocumentEntity(String name, byte[] content, long fileSize, Instant uploadedAt) {
        this.name = name;
        this.content = content;
        this.fileSize = fileSize;
        this.uploadedAt = uploadedAt;
    }
}