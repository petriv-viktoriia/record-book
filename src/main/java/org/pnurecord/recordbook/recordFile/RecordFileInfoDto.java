package org.pnurecord.recordbook.recordFile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class RecordFileInfoDto {
    private Long id;
    private String filename;
    private String type;
    private String fileUrl;
}