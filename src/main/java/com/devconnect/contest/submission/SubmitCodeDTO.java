package com.devconnect.contest.submission;


import lombok.*;

@Data
@NoArgsConstructor @AllArgsConstructor
@Builder
public class SubmitCodeDTO {
    private Long userId;
    private Long problemId;
    private Long contestId; // optional — null if practice submission, not part of a contest
    private String code;
    private String language; // e.g. "java" — kept for future multi-language support
}