package com.devconnect.contest.contest;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContestRepository extends JpaRepository<Contest,Long> {


    @Query("SELECT c FROM Contest c WHERE c.type = com.devconnect.contest.contest.ContestType.SYSTEM " +
            "OR c.visibility = com.devconnect.contest.contest.ContestVisibility.PUBLIC " +
            "OR c.createdBy = :userId " +
            "OR c.id IN (SELECT p.contest.id FROM ContestParticipant p WHERE p.userId = :userId)")
    List<Contest> findVisibleContestsForUser(@Param("userId") Long userId);
}
