package com.devconnect.contest.contest;


import com.devconnect.contest.problem.Problem;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "contests")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Contest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @ManyToMany
    @JoinTable(
            name = "contest_problems",
            joinColumns = @JoinColumn(name = "contest_id"),
            inverseJoinColumns = @JoinColumn(name = "problem_id")
    )
    @Builder.Default
    private List<Problem> problems = new ArrayList<>();

    @Column(name = "created_by", nullable = false)
    private Long createdBy; // userId — no FK, user lives in a different DB

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ContestType type = ContestType.USER_CREATED;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ContestVisibility visibility = ContestVisibility.PUBLIC;
}