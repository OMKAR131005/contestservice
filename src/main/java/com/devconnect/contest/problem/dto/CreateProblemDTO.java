package com.devconnect.contest.problem.dto;


import com.devconnect.contest.problem.Difficulty;
import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor @AllArgsConstructor
@Builder
public class CreateProblemDTO {
    private String title;
    private String statement;
    private String constraints;
    private Difficulty difficulty;
    private Integer points;
    private Integer timeLimitMs;
    private Integer memoryLimitKb;
    private List<TestCaseDTO> testCases;
}