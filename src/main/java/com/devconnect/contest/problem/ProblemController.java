package com.devconnect.contest.problem;




import com.devconnect.contest.problem.dto.CreateProblemDTO;
import com.devconnect.contest.problem.dto.ProblemDetailDTO;
import com.devconnect.contest.problem.dto.TestCaseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/problems")
@RequiredArgsConstructor
public class ProblemController {

    private final ProblemRepository problemRepository;

    // CREATE
    @PostMapping
    public ProblemDetailDTO createProblem(@RequestBody CreateProblemDTO dto) {
        Problem problem = Problem.builder()
                .title(dto.getTitle())
                .statement(dto.getStatement())
                .constraints(dto.getConstraints())
                .difficulty(dto.getDifficulty())
                .points(dto.getPoints())
                .timeLimitMs(dto.getTimeLimitMs() != null ? dto.getTimeLimitMs() : 2000)
                .memoryLimitKb(dto.getMemoryLimitKb() != null ? dto.getMemoryLimitKb() : 256000)
                .build();

        if (dto.getTestCases() != null) {
            List<TestCase> testCases = dto.getTestCases().stream()
                    .map(tcDto -> TestCase.builder()
                            .input(tcDto.getInput())
                            .expectedOutput(tcDto.getExpectedOutput())
                            .isSample(tcDto.isSample())
                            .problem(problem) // link back — required since TestCase owns the FK
                            .build())
                    .collect(Collectors.toList());
            problem.setTestCases(testCases);
        }

        Problem saved = problemRepository.save(problem);
        return toDetailDTO(saved);
    }

    // READ - single problem (only sample test cases exposed)
    @GetMapping("/{id}")
    public ProblemDetailDTO getProblem(@PathVariable Long id) {
        Problem problem = problemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Problem not found: " + id));
        return toDetailDTO(problem);
    }

    // READ - all problems
    @GetMapping
    public List<ProblemDetailDTO> getAllProblems() {
        return problemRepository.findAll().stream()
                .map(this::toDetailDTO)
                .collect(Collectors.toList());
    }

    // UPDATE
    @PutMapping("/{id}")
    public ProblemDetailDTO updateProblem(@PathVariable Long id, @RequestBody CreateProblemDTO dto) {
        Problem problem = problemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Problem not found: " + id));

        problem.setTitle(dto.getTitle());
        problem.setStatement(dto.getStatement());
        problem.setConstraints(dto.getConstraints());
        problem.setDifficulty(dto.getDifficulty());
        problem.setPoints(dto.getPoints());
        if (dto.getTimeLimitMs() != null) problem.setTimeLimitMs(dto.getTimeLimitMs());
        if (dto.getMemoryLimitKb() != null) problem.setMemoryLimitKb(dto.getMemoryLimitKb());

        // Replace test cases entirely if provided
        if (dto.getTestCases() != null) {
            problem.getTestCases().clear(); // orphanRemoval=true will delete old ones on save
            List<TestCase> newTestCases = dto.getTestCases().stream()
                    .map(tcDto -> TestCase.builder()
                            .input(tcDto.getInput())
                            .expectedOutput(tcDto.getExpectedOutput())
                            .isSample(tcDto.isSample())
                            .problem(problem)
                            .build())
                    .collect(Collectors.toList());
            problem.getTestCases().addAll(newTestCases);
        }

        Problem saved = problemRepository.save(problem);
        return toDetailDTO(saved);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProblem(@PathVariable Long id) {
        if (!problemRepository.existsById(id)) {
            throw new IllegalArgumentException("Problem not found: " + id);
        }
        problemRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // Helper: entity -> DTO, filtering out hidden test cases
    private ProblemDetailDTO toDetailDTO(Problem problem) {
        List<TestCaseDTO> sampleOnly = problem.getTestCases().stream()
                .filter(TestCase::isSample)
                .map(tc -> TestCaseDTO.builder()
                        .input(tc.getInput())
                        .expectedOutput(tc.getExpectedOutput())
                        .isSample(true)
                        .build())
                .collect(Collectors.toList());

        return ProblemDetailDTO.builder()
                .id(problem.getId())
                .title(problem.getTitle())
                .statement(problem.getStatement())
                .constraints(problem.getConstraints())
                .difficulty(problem.getDifficulty())
                .points(problem.getPoints())
                .timeLimitMs(problem.getTimeLimitMs())
                .memoryLimitKb(problem.getMemoryLimitKb())
                .sampleTestCases(sampleOnly)
                .build();
    }
}