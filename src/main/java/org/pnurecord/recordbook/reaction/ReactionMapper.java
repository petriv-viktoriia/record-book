package org.pnurecord.recordbook.reaction;

import org.mapstruct.Mapper;
import org.pnurecord.recordbook.record.RecordDto;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReactionMapper {

    ReactionDto toReactionDto(Reaction reaction);

    Reaction toReaction(RecordDto recordDto);

    List<Reaction> toReactionList(List<RecordDto> recordDtos);

    List<ReactionDto> toReactionDtoList(List<Reaction> reactions);

}
