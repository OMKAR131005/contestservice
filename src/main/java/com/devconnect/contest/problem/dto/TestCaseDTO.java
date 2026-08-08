package com.devconnect.contest.problem.dto;

import lombok.*;
@Data
@NoArgsConstructor @AllArgsConstructor
@Builder
public class TestCaseDTO {
    private String input;
    private String expectedOutput;
    private boolean isSample;
}
