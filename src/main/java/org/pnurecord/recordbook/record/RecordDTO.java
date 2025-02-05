package org.pnurecord.recordbook.record;

import lombok.Data;
import org.pnurecord.recordbook.category.Category;
import org.pnurecord.recordbook.user.User;

import java.util.Date;

@Data
public class RecordDTO {
    private Long id;
    private String title;
    private Category category;
    private String description;
    private String file_path;
    private User author;
    private Date published_date;
    private RecordStatus status;
}
