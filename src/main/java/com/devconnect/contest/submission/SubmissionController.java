package com.devconnect.contest.submission;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public SubmissionResultDTO submitCode(@AuthenticationPrincipal Long userId,
                                          @RequestBody SubmitCodeDTO dto) {

        Submission submission = Submission.builder()
                .userId(userId) // from JWT, never trust the request body for this
                .code(dto.getCode())
                .language(dto.getLanguage())
                .build();
        // contest is optional — set only if provided
        // (contestId -> Contest entity lookup can be added later once ContestService exists)

        return submissionService.submissionResult(submission, dto.getProblemId());
    }

    // GET /api/submissions?problemId=3  (userId comes from the logged-in user's token)
    @GetMapping
    public List<SubmissionSummaryDTO> getSubmissions(
            @AuthenticationPrincipal Long userId,
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