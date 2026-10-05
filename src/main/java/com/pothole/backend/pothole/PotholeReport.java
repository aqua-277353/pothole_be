package com.pothole.backend.pothole;

import com.pothole.backend.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "pothole_reports")
public class PotholeReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private double lat;

    @Column(nullable = false)
    private double lng;

    @Column(nullable = false, length = 100)
    private String region;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PotholeSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PotholeSource source;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PotholeStatus status = PotholeStatus.PENDING;

    @Column(name = "confirm_count", nullable = false)
    private int confirmCount = 1;

    @Column(name = "detected_at")
    private Instant detectedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected PotholeReport() {
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public String getRegion() {
        return region;
    }

    public PotholeSeverity getSeverity() {
        return severity;
    }

    public PotholeSource getSource() {
        return source;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public PotholeStatus getStatus() {
        return status;
    }

    public int getConfirmCount() {
        return confirmCount;
    }

    public Instant getDetectedAt() {
        return detectedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
