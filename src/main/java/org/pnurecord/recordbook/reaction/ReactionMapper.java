package org.pnurecord.recordbook.reaction;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.pnurecord.recordbook.record.RecordDto;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReactionMapper {

    @Mapping(target = "recordId", source = "record.id")
    @Mapping(target = "userId", source = "user.id")
    ReactionDto toReactionDto(Reaction reaction);

    @Mapping(target = "record.id", source = "recordId")
    @Mapping(target = "user.id", source = "userId")
    Reaction toReaction(ReactionDto reactionDto);

    List<Reaction> toReactionList(List<ReactionDto> reactionDtos);

    List<ReactionDto> toReactionDtoList(List<Reaction> reactions);

}
