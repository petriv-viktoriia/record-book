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

    @NotBlank(message = "Description cannot be blank")
    private String description;

    private String filename;
    private byte[] file;
    private Long authorId;
    private LocalDate publishedDate;
    private RecordStatus status;
}
