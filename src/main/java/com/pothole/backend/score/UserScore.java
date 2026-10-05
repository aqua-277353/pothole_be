package com.pothole.backend.score;

import com.pothole.backend.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "user_scores")
public class UserScore {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "total_points", nullable = false)
    private int totalPoints;

    @Column(name = "reports_count", nullable = false)
    private int reportsCount;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected UserScore() {
    }

    public UserScore(User user) {
        this.user = user;
        this.updatedAt = Instant.now();
    }

    public Long getUserId() {
        return userId;
    }

    public User getUser() {
        return user;
    }

    public int getTotalPoints() {
        return totalPoints;
    }

    public int getReportsCount() {
        return reportsCount;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
