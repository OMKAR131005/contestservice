package com.devconnect.contest.contest;



import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ContestResponseDTO {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private ContestType type;
    private ContestVisibility visibility;
    private Long createdBy;
    private List<ProblemSummary> problems;

    @Data
    @Builder
    public static class ProblemSummary {
        private Long id;
        private String title;
        private Integer points;
    }


    public static ContestResponseDTO from(Contest contest) {

        List<ProblemSummary> problems = contest.getProblems()
                .stream()
                .map(problem -> ProblemSummary.builder()
                        .id(problem.getId())
                        .title(problem.getTitle())
                        .points(problem.getPoints())
                        .build())
                .toList();

        return ContestResponseDTO.builder()
                .id(contest.getId())
                .title(contest.getTitle())
                .description(contest.getDescription())
                .startTime(contest.getStartTime())
                .endTime(contest.getEndTime())
                .type(contest.getType())
                .visibility(contest.getVisibility())
                .createdBy(contest.getCreatedBy())
                .problems(problems)
                .build();
    }
}
