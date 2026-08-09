package com.devconnect.contest.submission;

import com.devconnect.contest.contest.Contest;
import com.devconnect.contest.contest.ContestParticipantRepository;
import com.devconnect.contest.contest.ContestRepository;
import com.devconnect.contest.contest.ContestVisibility;
import com.devconnect.contest.judgesubmission.JudgeResult;
import com.devconnect.contest.judgesubmission.JudgeService;
import com.devconnect.contest.problem.Problem;
import com.devconnect.contest.problem.ProblemRepository;
import com.devconnect.contest.problem.TestCase;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class SubmissionService {

    private final ProblemRepository problemRepository;
    private final SubmissionRepository submissionRepository;
    private final JudgeService judgeService;
    private final ContestRepository contestRepository;
    private final ContestParticipantRepository contestParticipantRepository;

    public SubmissionResultDTO submissionResult(Submission submission, Long problemId, Long contestId) {

        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new IllegalArgumentException("problem not found"));

        if (contestId != null) {
            Contest contest = contestRepository.findById(contestId)
                    .orElseThrow(() -> new IllegalArgumentException("contest not found"));

            LocalDateTime now = LocalDateTime.now();
            if (now.isBefore(contest.getStartTime()) || now.isAfter(contest.getEndTime())) {
                throw new IllegalStateException("Contest is not active");
            }

            if (contest.getVisibility() == ContestVisibility.PRIVATE) {
                boolean isCreator = contest.getCreatedBy().equals(submission.getUserId());
                boolean isInvited = contestParticipantRepository
                        .existsByContestIdAndUserId(contestId, submission.getUserId());
                if (!isCreator && !isInvited) {
                    throw new AccessDeniedException("You don't have access to this contest");
                }
            }

            boolean problemInContest = contest.getProblems().stream()
                    .anyMatch(p -> p.getId().equals(problemId));
            if (!problemInContest) {
                throw new IllegalArgumentException("This problem is not part of the contest");
            }

            submission.setContest(contest);
        }

        submission.setProblem(problem); // required — Problem FK is nullable=false on Submission

        List<TestCase> testCases = problem.getTestCases();

        int testCaseNumber = 0;

        for (TestCase testCase : testCases) {
            testCaseNumber++;

            JudgeResult result = judgeService.compileAndRun(
                    submission.getCode(),
                    testCase.getInput(),
                    problem.getTimeLimitMs()
            );

            // CE / RE / TLE / MLE — crash or didn't even compile, stop immediately
            if (result.getVerdict() != Verdict.AC) {
                return buildFailureResult(submission, testCase, testCaseNumber, result.getVerdict(),
                        result.getStdout(), result.getStderr());
            }

            // Program ran successfully — now actually compare output
            String actual = normalize(result.getStdout());
            String expected = normalize(testCase.getExpectedOutput());

            if (!actual.equals(expected)) {
                return buildFailureResult(submission, testCase, testCaseNumber, Verdict.WA,
                        result.getStdout(), null);
            }
            // this test case passed — continue to next
        }

        // all test cases passed
        submission.setVerdict(Verdict.AC);
        submission.setScore(problem.getPoints());
        submissionRepository.save(submission);

        return SubmissionResultDTO.builder()
                .submissionId(submission.getId())
                .verdict(Verdict.AC)
                .score(problem.getPoints())
                .build();
    }

    private SubmissionResultDTO buildFailureResult(Submission submission, TestCase testCase,
                                                   int testCaseNumber, Verdict verdict,
                                                   String actualOutput, String message) {

        submission.setVerdict(verdict);
        submission.setScore(0);
        submissionRepository.save(submission);

        SubmissionResultDTO.SubmissionResultDTOBuilder builder = SubmissionResultDTO.builder()
                .submissionId(submission.getId())
                .verdict(verdict)
                .score(0)
                .failedTestCaseNumber(testCaseNumber)
                .message(message);

        // Only expose input/expected/actual for SAMPLE test cases — never leak hidden ones
        if (testCase.isSample()) {
            builder.failedTestCaseDetail(
                    SubmissionResultDTO.FailedTestCaseDetail.builder()
                            .input(testCase.getInput())
                            .expectedOutput(testCase.getExpectedOutput())
                            .actualOutput(actualOutput)
                            .build()
            );
        }

        return builder.build();
    }

    private String normalize(String s) {
        if (s == null) return "";
        return s.replace("\r\n", "\n").trim();
    }
}