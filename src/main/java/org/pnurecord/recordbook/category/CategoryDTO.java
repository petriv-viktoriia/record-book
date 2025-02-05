package org.pnurecord.recordbook.category;

import lombok.Data;
import org.pnurecord.recordbook.record.Record;

import java.util.ArrayList;
import java.util.List;

@Data
public class CategoryDTO {
    private Long id;
    private String name;
    private List<Record> records = new ArrayList<>();
}
