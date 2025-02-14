package org.pnurecord.recordbook.reaction;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReactionDto {
    private Long id;
    private Long recordId;
    private Long userId;
    private boolean liked;
}
