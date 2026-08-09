package com.devconnect.contest.contest;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContestParticipantRepository extends JpaRepository<ContestParticipant, Long> {
    List<ContestParticipant> findByContestId(Long contestId);
    boolean existsByContestIdAndUserId(Long contestId, Long userId);
    List<ContestParticipant> findByUserId(Long userId);

}
