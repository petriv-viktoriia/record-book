package org.pnurecord.recordbook.category;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Data
public class CategoryDto {
    private Long id;

    @NotBlank(message = "Name cannot be blank")
    private String name;
}
