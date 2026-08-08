package com.devconnect.contest.judgesubmission;

import com.devconnect.contest.submission.Verdict;
import lombok.Builder;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@Service
public class JudgeService {

    public JudgeResult compileAndRun(String sourceCode, String stdin, int timeLimitMs) {
        Path tempDir = null;
        try {
            // 1. Create isolated temp directory
            tempDir = Files.createTempDirectory("judge_");

            // 2. Write source code to Main.java
            Path sourceFile = tempDir.resolve("Main.java");
            Files.writeString(sourceFile, sourceCode);

            // 3-7. Compile
            JudgeResult compileResult = compile(tempDir);
            if (compileResult != null) {
                return compileResult; // CE happened
            }

            // 8-14. Run
            return run(tempDir, stdin, timeLimitMs);

        } catch (IOException | InterruptedException e) {
            return JudgeResult.builder()
                    .verdict(Verdict.RE)
                    .stderr("Internal judge error: " + e.getMessage())
                    .build();
        } finally {
            if (tempDir != null) {
                deleteDirectory(tempDir);
            }
        }
    }

    private JudgeResult compile(Path tempDir) throws IOException, InterruptedException {
        ProcessBuilder compilePb = new ProcessBuilder("javac", "Main.java");
        compilePb.directory(tempDir.toFile());

        Process compileProcess = compilePb.start();

        boolean compiled = compileProcess.waitFor(5, TimeUnit.SECONDS);
        if (!compiled) {
            compileProcess.destroyForcibly();
            return JudgeResult.builder()
                    .verdict(Verdict.CE)
                    .stderr("Compilation timeout")
                    .build();
        }

        int compileExitCode = compileProcess.exitValue();
        if (compileExitCode != 0) {
            String errors = new String(compileProcess.getErrorStream().readAllBytes());
            return JudgeResult.builder()
                    .verdict(Verdict.CE)
                    .stderr(errors)
                    .build();
        }

        return null; // compile succeeded, no early return needed
    }

    private JudgeResult run(Path tempDir, String stdin, int timeLimitMs) throws IOException, InterruptedException {
        ProcessBuilder runPb = new ProcessBuilder("java", "-Xmx256m", "Main");
        runPb.directory(tempDir.toFile());

        Process runProcess = runPb.start();

        // Write stdin in a separate thread (avoids deadlock)
        Thread stdinWriter = new Thread(() -> {
            try {
                runProcess.getOutputStream().write(stdin.getBytes());
                runProcess.getOutputStream().close();
            } catch (IOException e) {
                // process already exited/closed — safe to ignore
            }
        });
        stdinWriter.start();

        // Read stdout in a separate thread (avoids buffer deadlock)
        ByteArrayOutputStream stdoutBuffer = new ByteArrayOutputStream();
        Thread stdoutReader = new Thread(() -> {
            try {
                runProcess.getInputStream().transferTo(stdoutBuffer);
            } catch (IOException e) {
                // ignore
            }
        });
        stdoutReader.start();

        // Read stderr in a separate thread (same reason — avoid buffer deadlock,
        // and this is what actually lets us capture the real exception message)
        ByteArrayOutputStream stderrBuffer = new ByteArrayOutputStream();
        Thread stderrReader = new Thread(() -> {
            try {
                runProcess.getErrorStream().transferTo(stderrBuffer);
            } catch (IOException e) {
                // ignore
            }
        });
        stderrReader.start();

        // Wait for process with timeout
        boolean finished = runProcess.waitFor(timeLimitMs, TimeUnit.MILLISECONDS);
        if (!finished) {
            runProcess.destroyForcibly();
            return JudgeResult.builder()
                    .verdict(Verdict.TLE)
                    .build();
        }

        stdinWriter.join();
        stdoutReader.join();
        stderrReader.join();

        int exitCode = runProcess.exitValue();
        String output = stdoutBuffer.toString();
        String errorOutput = stderrBuffer.toString();

        if (exitCode != 0) {
            // OOM crashes exit non-zero too — check stderr content to distinguish MLE from RE
            Verdict verdict = errorOutput.contains("OutOfMemoryError") ? Verdict.MLE : Verdict.RE;
            return JudgeResult.builder()
                    .verdict(verdict)
                    .stderr(errorOutput)
                    .build();
        }

        return JudgeResult.builder()
                .verdict(Verdict.AC) // comparison with expected output happens in SubmissionService
                .stdout(output)
                .build();
    }

    private void deleteDirectory(Path dir) {
        try (Stream<Path> paths = Files.walk(dir)) {
            paths.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            // ignore individual delete failures
                        }
                    });
        } catch (IOException e) {
            // ignore cleanup failure
        }
    }
}