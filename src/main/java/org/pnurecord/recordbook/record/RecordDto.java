package org.pnurecord.recordbook.record;

import lombok.Data;
import org.pnurecord.recordbook.category.Category;
import org.pnurecord.recordbook.user.User;

import java.util.Date;

@Data
public class RecordDto {
    private Long id;
    private String title;
    private Long category;
    private String description;
    private String file_path;
    private Long author;
    private Date published_date;
    private RecordStatus status;
}
