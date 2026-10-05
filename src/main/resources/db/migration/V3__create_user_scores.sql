CREATE TABLE user_scores (
    user_id BIGINT PRIMARY KEY,
    total_points INTEGER NOT NULL DEFAULT 0,
    reports_count INTEGER NOT NULL DEFAULT 0,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_user_scores_user
        FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT chk_user_scores_points
        CHECK (total_points >= 0),
    CONSTRAINT chk_user_scores_reports
        CHECK (reports_count >= 0)
);
