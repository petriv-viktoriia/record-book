package org.pnurecord.recordbook.record;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RecordDto {
    private Long id;

    @NotBlank(message = "Title cannot be blank")
    private String title;
    private Long categoryId;
    private String categoryName;

    @NotBlank(message = "Description cannot be blank")
    private String description;
    private Long authorId;
    private String authorName;
    private LocalDate publishedDate;
    private RecordStatus status;
}
