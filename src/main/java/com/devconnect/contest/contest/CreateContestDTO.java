package com.devconnect.contest.contest;



import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor @AllArgsConstructor
@Builder
public class CreateContestDTO {
    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private ContestVisibility visibility;   // PUBLIC or PRIVATE — creator's choice
    private List<Long> problemIds;          // existing problems to attach to this contest
    private List<Long> invitedUserIds;      // only used when visibility = PRIVATE
}