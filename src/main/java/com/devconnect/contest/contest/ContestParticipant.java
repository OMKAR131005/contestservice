package com.devconnect.contest.contest;



import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "contest_participants",
        uniqueConstraints = @UniqueConstraint(columnNames = {"contest_id", "user_id"}))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ContestParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "contest_id", nullable = false)
    private Contest contest;

    @Column(name = "user_id", nullable = false)
    private Long userId; // no FK — user lives in a different DB
}