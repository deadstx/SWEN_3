package org.example.swen3backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "document_collections")
@Getter
@NoArgsConstructor
public class DocumentCollectionEntity {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @ManyToMany
    @JoinTable(name = "collection_documents",
            joinColumns = @JoinColumn(name = "collection_id"),
            inverseJoinColumns = @JoinColumn(name = "document_id"))
    @OrderBy("id ASC")
    private Set<DocumentEntity> documents = new LinkedHashSet<>();

    public DocumentCollectionEntity(String name) {
        this.name = name;
    }
}
