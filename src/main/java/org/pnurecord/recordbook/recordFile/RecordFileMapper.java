package org.pnurecord.recordbook.recordFile;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RecordFileMapper {

    @Mapping(target = "record.id", source = "recordId")
    RecordFile toRecordFile(RecordFileDto recordFileDto);

    @Mapping(target = "recordId", source = "record.id")
    RecordFileDto toRecordFileDto(RecordFile recordFile);
}
