package com.devconnect.contest.contest;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contests")
@RequiredArgsConstructor
public class ContestController {

    private final ContestService contestService;

    // POST /api/contests — any logged-in user can create their own contest
    @PostMapping
    public Contest createContest(@AuthenticationPrincipal Long userId,
                                 @RequestBody CreateContestDTO dto) {
        return contestService.createContest(dto, userId);
    }

    // GET /api/contests — list contests visible to the current user
    @GetMapping
    public List<Contest> getVisibleContests(@AuthenticationPrincipal Long userId) {
        return contestService.getVisibleContests(userId);
    }

    // GET /api/contests/{id} — single contest, with private-contest access check
    @GetMapping("/{id}")
    public Contest getContestById(@AuthenticationPrincipal Long userId,
                                  @PathVariable Long id) {
        return contestService.getContestById(id, userId);
    }

    // GET /api/contests/invitations — private contests the current user has been invited to
    @GetMapping("/invitations")
    public List<Contest> getMyInvitations(@AuthenticationPrincipal Long userId) {
        return contestService.getMyInvitations(userId);
    }
}