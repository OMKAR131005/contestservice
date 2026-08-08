package com.devconnect.contest.problem;



import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "problems")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Problem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String statement;

    @Column(columnDefinition = "TEXT")
    private String constraints;

    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;

    private Integer points;

    @Column(name = "time_limit_ms")
    @Builder.Default
    private Integer timeLimitMs = 2000;

    @Column(name = "memory_limit_kb")
    @Builder.Default
    private Integer memoryLimitKb = 256000;

    @OneToMany(mappedBy = "problem", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TestCase> testCases = new ArrayList<>();
}