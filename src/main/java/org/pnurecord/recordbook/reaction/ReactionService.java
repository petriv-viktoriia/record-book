package org.pnurecord.recordbook.reaction;

import lombok.RequiredArgsConstructor;
import org.pnurecord.recordbook.exceptions.DuplicateValueException;
import org.pnurecord.recordbook.exceptions.NotFoundException;
import org.pnurecord.recordbook.record.RecordRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ReactionService {
    private final ReactionRepository reactionRepository;
    private final ReactionMapper reactionMapper;
    private final RecordRepository recordRepository;

    public ReactionDto addReaction(ReactionDto reactionDto) {
        boolean exists = reactionRepository.existsByRecordIdAndUserId(reactionDto.getRecordId(), reactionDto.getUserId());
        if (exists) {
            throw new DuplicateValueException("Reaction with recordId: %s and userId: %s already exists".formatted(reactionDto.getRecordId(), reactionDto.getUserId()));
        } else {
            Reaction reaction = reactionMapper.toReaction(reactionDto);
            return reactionMapper.toReactionDto(reactionRepository.save(reaction));
        }
    }

    public void deleteReaction(Long id) {
        Reaction reaction = reactionRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Reaction with id: %s not found".formatted(id))
        );

        reactionRepository.delete(reaction);
    }

    public ReactionDto updateReaction(Long id, ReactionUpdateDto reactionUpdateDto) {
        Reaction reaction = reactionRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Reaction with id: %s not found".formatted(id))
        );

        reaction.setLiked(reactionUpdateDto.isLiked());
        return reactionMapper.toReactionDto(reactionRepository.save(reaction));

    }

    public List<ReactionDto> getReactions() {
        List<Reaction> reactions = reactionRepository.findAll();
        return reactionMapper.toReactionDtoList(reactions);
    }

    public ReactionCountDto getReactionsCount(Long recordId) {

        if (recordRepository.existsById(recordId)) {
            int likes = reactionRepository.countByRecordIdAndLiked(recordId, true);
            int dislikes = reactionRepository.countByRecordIdAndLiked(recordId, false);

            return new ReactionCountDto(likes, dislikes);
        } else {
            throw new NotFoundException("Record with id: %s not found".formatted(recordId));
        }
    }

    public ReactionDto getReactionById(Long id) {
        Reaction reaction = reactionRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Reaction with id: %s not found".formatted(id))
        );
        return reactionMapper.toReactionDto(reaction);
    }
}
