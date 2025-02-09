package org.pnurecord.recordbook.recordFile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class RecordFileDto {
    private Long id;
    private String filename;
    private String type;
    private Long recordId;
    private byte[] data;
}
