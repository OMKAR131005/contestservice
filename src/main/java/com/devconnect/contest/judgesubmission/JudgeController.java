package com.devconnect.contest.judgesubmission;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/")
@RequiredArgsConstructor
public class JudgeController {
    private final JudgeService judgeService;
    @PostMapping("/judge-test")
    public JudgeResult testJudge(@RequestBody JudgeTestRequestDTO request) {
        return judgeService.compileAndRun(
                request.getCode(),
                request.getStdin(),
                request.getTimeLimitMs()
        );
    }
}
