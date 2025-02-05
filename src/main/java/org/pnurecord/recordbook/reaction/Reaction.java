package org.pnurecord.recordbook.reaction;

import jakarta.persistence.*;
import lombok.Data;
import org.pnurecord.recordbook.user.User;
import org.pnurecord.recordbook.record.Record;

@Data
@Entity
@Table(name = "reactions")
public class Reaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "record_id", referencedColumnName = "id")
    private Record record;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    private String content;
    private String type;
}
