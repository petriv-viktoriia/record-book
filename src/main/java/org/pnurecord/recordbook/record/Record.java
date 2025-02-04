package org.pnurecord.recordbook.record;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Record {
    @Id
    private Long id;
    private String title;
    private Long categoryId;
    private String description;
    private String file_path;
    private Date published_date;
    private RecordStatus status;
}
