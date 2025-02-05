package org.pnurecord.recordbook.record;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RecordMapper {

    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "authorId", source = "author.id")
    RecordDto toRecordDto(Record record);

    @Mapping(target = "category.id", source = "categoryId")
    @Mapping(target = "author.id", source = "authorId")
    Record toRecord(RecordDto recordDto);

    List<RecordDto> toRecordDtoList(List<Record> records);

    List<Record> toRecordList(List<RecordDto> recordDtos);

}
