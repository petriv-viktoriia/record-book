package org.pnurecord.recordbook.record;


import jakarta.persistence.*;
import lombok.Data;
import org.pnurecord.recordbook.reaction.Reaction;
import org.pnurecord.recordbook.category.Category;
import org.pnurecord.recordbook.user.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Entity
@Table(name = "records")
public class Record {
    @Id
    private Long id;
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author;

    private String description;
    private String file_path;
    private Date published_date;
    private RecordStatus status;

    @OneToMany(mappedBy = "record")
    private List<Reaction> reactions = new ArrayList<>();

}
