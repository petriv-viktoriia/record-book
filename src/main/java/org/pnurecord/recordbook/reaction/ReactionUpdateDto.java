package org.pnurecord.recordbook.reaction;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReactionUpdateDto {
    private Long id;
    private boolean isLike;
}
