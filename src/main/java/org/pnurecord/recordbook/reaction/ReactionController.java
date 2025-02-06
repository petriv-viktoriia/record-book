package org.pnurecord.recordbook.reaction;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/reactions")
public class ReactionController {
    private final ReactionService reactionService;

    @GetMapping
    public List<ReactionDto> getReactions() {
        return reactionService.getReactions();
    }

    @PostMapping
    public void createReaction(ReactionDto reaction) {
        reactionService.addReaction(reaction);
    }

    @DeleteMapping("/{id}")
    public void deleteReaction(@PathVariable Long id) {
        reactionService.deleteReaction(id);
    }

    @PutMapping("/{id}")
    public void updateReaction(@PathVariable Long id, ReactionUpdateDto reactionUpdateDto) {
        reactionService.updateReaction(id, reactionUpdateDto);
    }

    @GetMapping("/count")
    public ReactionCountDto getReactionCount(@RequestParam("record-id") Long recordId) {
        return reactionService.getReactionsCount(recordId);
    }

    @GetMapping("/{id}")
    public ReactionDto getReactionById(@PathVariable Long id) {
        return reactionService.getReactionById(id);
    }

}
