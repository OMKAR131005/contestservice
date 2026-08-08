package com.devconnect.contest.submission;



import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
public class SubmissionSummaryDTO {
    private Long submissionId;
    private Verdict verdict;
    private Integer score;
    private String language;
    private LocalDateTime submittedAt;
}