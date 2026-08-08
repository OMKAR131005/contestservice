package com.devconnect.contest.submission;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubmissionResultDTO {
    private Long submissionId;
    private Verdict verdict;
    private int score;

    private Integer failedTestCaseNumber; // null if AC or CE (CE fails before any test case runs)

    // Only populated when the failed test case is a SAMPLE test case.
    // Left null for hidden test cases — never leak hidden input/expected output.
    private FailedTestCaseDetail failedTestCaseDetail;

    private String message; // compile error / runtime error snippet shown to user

    @Data
    @Builder
    public static class FailedTestCaseDetail {
        private String input;
        private String expectedOutput;
        private String actualOutput;
    }
}