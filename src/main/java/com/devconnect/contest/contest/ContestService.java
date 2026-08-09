package com.devconnect.contest.contest;

import com.devconnect.contest.problem.Problem;
import com.devconnect.contest.problem.ProblemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@RequiredArgsConstructor
@Service
public class ContestService {

    private final ContestRepository contestRepository;
    private final ProblemRepository problemRepository;
    private final ContestParticipantRepository contestParticipantRepository;

    @Scheduled(cron = "0 0 9 * * MON")
    public void generateWeeklyContest() {
        List<Problem> officialProblem = problemRepository.findByCreatedByIsNull();

        if (officialProblem.size() < 4) {
            return;
        }
        officialProblem = new ArrayList<>(officialProblem);
        Collections.shuffle(officialProblem);
        List<Problem> selected = officialProblem.subList(0, 4);

        Contest contest = Contest.builder()
                .title("Weekly Contest: " + LocalDateTime.now())
                .description("Auto Generated Weekly Contest")
                .type(ContestType.SYSTEM)
                .visibility(ContestVisibility.PUBLIC)
                .createdBy(0L)
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusHours(2))
                .problems(selected)
                .build();

        contestRepository.save(contest);
    }

    public Contest createContest(CreateContestDTO contest, Long createdBy) {
        Contest contest1 = Contest.builder()
                .createdBy(createdBy)
                .description(contest.getDescription())
                .startTime(contest.getStartTime())
                .endTime(contest.getEndTime())
                .visibility(contest.getVisibility())
                .type(ContestType.USER_CREATED)
                .title(contest.getTitle())
                .problems(problemRepository.findAllById(contest.getProblemIds()))
                .build();

        contestRepository.save(contest1);

        if (contest.getVisibility() == ContestVisibility.PRIVATE
                && contest.getInvitedUserIds() != null
                && !contest.getInvitedUserIds().isEmpty()) {
            for (Long userId : contest.getInvitedUserIds()) {
                contestParticipantRepository.save(
                        ContestParticipant.builder()
                                .userId(userId)
                                .contest(contest1)
                                .build());
            }
        }

        return contest1;
    }

    @Transactional
    public List<Contest> getMyInvitations(Long userId) {
        List<ContestParticipant> list = contestParticipantRepository.findByUserId(userId);
        return list.stream().map(ContestParticipant::getContest).toList();
    }

    @Transactional
    public List<Contest> getVisibleContests(Long userId) {
        return contestRepository.findVisibleContestsForUser(userId);
    }

    @Transactional
    public Contest getContestById(Long contestId, Long userId) {
        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new IllegalArgumentException("Contest not found"));

        if (contest.getType() == ContestType.SYSTEM
                || contest.getVisibility() == ContestVisibility.PUBLIC) {
            return contest;
        }

        if (contest.getCreatedBy().equals(userId)) {
            return contest;
        }

        boolean isInvited = contestParticipantRepository.existsByContestIdAndUserId(contestId, userId);
        if (isInvited) {
            return contest;
        }

        throw new AccessDeniedException("You don't have access to this contest");
    }
}