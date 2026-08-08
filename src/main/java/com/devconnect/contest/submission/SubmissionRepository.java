package com.devconnect.contest.submission;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    List<Submission> findByUserIdAndProblemId(Long userId, Long problemId);
    List<Submission> findByContestId(Long contestId);
    List<Submission> findByUserIdAndProblem_IdOrderBySubmittedAtDesc(Long userId, Long problemId);
}