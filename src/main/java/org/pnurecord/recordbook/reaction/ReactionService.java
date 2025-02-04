package org.pnurecord.recordbook.reaction;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ReactionService {
    private final ReactionRepository reactionRepository;
}
