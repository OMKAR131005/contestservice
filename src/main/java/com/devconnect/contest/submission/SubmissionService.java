package com.devconnect.contest.submission;

import com.devconnect.contest.judgesubmission.JudgeResult;
import com.devconnect.contest.judgesubmission.JudgeService;
import com.devconnect.contest.problem.Problem;
import com.devconnect.contest.problem.ProblemRepository;
import com.devconnect.contest.problem.TestCase;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class SubmissionService {

    private final ProblemRepository problemRepository;
    private final SubmissionRepository submissionRepository;
    private final JudgeService judgeService;

    @Transactional
    public SubmissionResultDTO submissionResult(Submission submission, Long problemId) {

        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new IllegalArgumentException("problem not found"));

        List<TestCase> testCases = problem.getTestCases();
        submission.setProblem(problem);

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