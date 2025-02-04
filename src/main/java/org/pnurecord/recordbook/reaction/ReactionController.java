package org.pnurecord.recordbook.reaction;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/reactions")
public class ReactionController {

    private final ReactionService reactionService;
}
