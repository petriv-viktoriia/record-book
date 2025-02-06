package org.pnurecord.recordbook.record;

import jakarta.persistence.*;
import lombok.Data;
import org.pnurecord.recordbook.category.Category;
import org.pnurecord.recordbook.user.User;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "records")
public class Record {
    @Id
    private Long id;
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", referencedColumnName = "id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", referencedColumnName = "id")
    private User author;

    private String description;
    private String filename;
    private byte[] file;
    private LocalDate publishedDate;
    private RecordStatus status;

}
