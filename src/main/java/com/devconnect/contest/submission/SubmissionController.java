package com.devconnect.contest.submission;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/submissions")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;
    private final SubmissionRepository submissionRepository;

    @PostMapping
    public SubmissionResultDTO submitCode(@RequestBody SubmitCodeDTO dto) {

        Submission submission = Submission.builder()
                .userId(dto.getUserId())
                .code(dto.getCode())
                .language(dto.getLanguage())
                .build();
        // contest is optional — set only if provided
        // (contestId -> Contest entity lookup can be added later once ContestService exists)

        return submissionService.submissionResult(submission, dto.getProblemId());
    }

    // GET /api/submissions?userId=1&problemId=3
    @GetMapping
    public List<SubmissionSummaryDTO> getSubmissions(
            @RequestParam Long userId,
            @RequestParam Long problemId) {

        return submissionRepository.findByUserIdAndProblem_IdOrderBySubmittedAtDesc(userId, problemId)
                .stream()
                .map(s -> SubmissionSummaryDTO.builder()
                        .submissionId(s.getId())
                        .verdict(s.getVerdict())
                        .score(s.getScore())
                        .language(s.getLanguage())
                        .submittedAt(s.getSubmittedAt())
                        .build())
                .collect(Collectors.toList());
    }
}