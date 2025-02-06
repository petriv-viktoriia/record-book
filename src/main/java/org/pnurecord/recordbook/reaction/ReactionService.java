package org.pnurecord.recordbook.reaction;

import lombok.RequiredArgsConstructor;
import org.pnurecord.recordbook.exceptions.DuplicateValueException;
import org.pnurecord.recordbook.exceptions.NotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ReactionService {
    private final ReactionRepository reactionRepository;
    private final ReactionMapper reactionMapper;

    public void addReaction(ReactionDto reactionDto) {
        boolean exists = reactionRepository.existsByRecordIdAndUserId(reactionDto.getRecordId(), reactionDto.getUserId());
        if (exists) {
            throw new DuplicateValueException("Reaction with recordId: %s and userId: %s already exists".formatted(reactionDto.getRecordId(), reactionDto.getUserId()));
        } else {
            Reaction reaction = reactionMapper.toReaction(reactionDto);
            reactionRepository.save(reaction);
        }
    }

    public void deleteReaction(Long id) {
        Reaction reaction = reactionRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Reaction with id: %s not found".formatted(id))
        );

        reactionRepository.delete(reaction);
    }

    public void updateReaction(Long id, ReactionUpdateDto reactionUpdateDto) {
        Reaction reaction = reactionRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Reaction with id: %s not found".formatted(id))
        );

        reaction.setLiked(reactionUpdateDto.isLiked());
        reactionRepository.save(reaction);

    }

    public List<ReactionDto> getReactions() {
        List<Reaction> reactions = reactionRepository.findAll();
        return reactionMapper.toReactionDtoList(reactions);
    }

    public ReactionCountDto getReactionsCount(Long recordId) {

        if (reactionRepository.existsById(recordId)) {
            int likes = reactionRepository.countByRecordIdAndLiked(recordId, true);
            int dislikes = reactionRepository.countByRecordIdAndLiked(recordId, false);

            return new ReactionCountDto(likes, dislikes);
        } else {
            throw new NotFoundException("Reaction with id: %s not found".formatted(recordId));
        }
    }

    public ReactionDto getReactionById(Long id) {
        Reaction reaction = reactionRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Reaction with id: %s not found".formatted(id))
        );
        return reactionMapper.toReactionDto(reaction);
    }
}
