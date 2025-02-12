package org.pnurecord.recordbook.reaction;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ReactionCountDto {
    private int likes;
    private int dislikes;
}
