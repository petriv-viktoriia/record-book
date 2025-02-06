package org.pnurecord.recordbook.record;

import lombok.Data;
import java.time.LocalDate;

@Data
public class RecordDto {
    private Long id;
    private String title;
    private Long categoryId;
    private String description;
    private String filename;
    private byte[] file;
    private Long authorId;
    private LocalDate published_date;
    private RecordStatus status;
}
