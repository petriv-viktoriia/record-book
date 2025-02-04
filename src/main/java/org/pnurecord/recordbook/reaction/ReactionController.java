package org.pnurecord.recordbook.reaction;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reactions")
@AllArgsConstructor
public class ReactionController {

    private final ReactionRepository reactionRepository;
}
