package com.devconnect.contest.judgesubmission;

import com.devconnect.contest.submission.Verdict;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JudgeResult {
    private Verdict verdict;
    private int timeMs;
    private String stdout;
    private String stderr;
}
