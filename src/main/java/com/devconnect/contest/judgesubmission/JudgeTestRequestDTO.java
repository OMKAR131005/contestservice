package com.devconnect.contest.judgesubmission;

import lombok.Data;

@Data
public class JudgeTestRequestDTO {
    private String code;
    private String stdin;
    private int timeLimitMs = 5000; // default 5s if not provided
}