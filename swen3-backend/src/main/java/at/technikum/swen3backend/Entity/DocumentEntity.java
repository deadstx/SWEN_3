package at.technikum.swen3backend.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@DynamicUpdate
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

    // Legacy PDFs remain readable in the database; new uploads use objectKey.
    @Column(nullable = true)
    private byte[] content;

    @Column(length = 512)
    private String objectKey;

    @Column(columnDefinition = "text")
    private String summary;

    @Column(length = 32)
    private String summaryStatus;

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

    public DocumentEntity(String name, String objectKey, long fileSize, Instant uploadedAt) {
        this.name = name;
        this.objectKey = objectKey;
        this.summaryStatus = "PENDING";
        this.fileSize = fileSize;
        this.uploadedAt = uploadedAt;
    }
}
