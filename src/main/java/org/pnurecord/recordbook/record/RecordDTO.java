package org.pnurecord.recordbook.record;

import lombok.Data;
import org.pnurecord.recordbook.category.Category;
import org.pnurecord.recordbook.reaction.Reaction;
import org.pnurecord.recordbook.user.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
    private List<Reaction> reactions = new ArrayList<>();

}
