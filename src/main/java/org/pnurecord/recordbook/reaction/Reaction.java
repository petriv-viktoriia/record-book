package org.pnurecord.recordbook.reaction;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Reaction {
    @Id
    private Long id;
    private Long recordId;
    private Long userId;
    private String content;
    private String type;
}
