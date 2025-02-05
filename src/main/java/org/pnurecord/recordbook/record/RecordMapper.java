package org.pnurecord.recordbook.record;

import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RecordMapper {

    RecordDto toRecordDto(Record record);

    Record toRecord(RecordDto recordDto);

    List<RecordDto> toRecordDtoList(List<Record> records);

    List<Record> toRecordList(List<RecordDto> recordDtos);

}
