package org.pnurecord.recordbook.record;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

import java.util.Date;

@Data
@Entity
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
