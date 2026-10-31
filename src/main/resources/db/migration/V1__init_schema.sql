CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    bio TEXT NULL,
    location VARCHAR(255) NULL,
    profile_image_url VARCHAR(512) NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_users_email (email)
);

CREATE TABLE skills (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description TEXT NULL,
    category VARCHAR(30) NOT NULL,
    UNIQUE KEY uk_skills_name (name)
);

CREATE TABLE user_skills (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    skill_id BIGINT NOT NULL,
    skill_type VARCHAR(10) NOT NULL,
    proficiency_level VARCHAR(20) NOT NULL,
    years_experience INT NULL,
    CONSTRAINT fk_user_skills_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_user_skills_skill FOREIGN KEY (skill_id) REFERENCES skills(id),
    UNIQUE KEY uk_user_skills_user_skill_type (user_id, skill_id, skill_type),
    INDEX idx_user_skills_user_type (user_id, skill_type),
    INDEX idx_user_skills_skill (skill_id)
);

CREATE TABLE user_interests (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    interest_name VARCHAR(100) NOT NULL,
    CONSTRAINT fk_user_interests_user FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_user_interests_user (user_id)
);

CREATE TABLE connections (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    requester_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    message VARCHAR(500) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_connections_requester FOREIGN KEY (requester_id) REFERENCES users(id),
    CONSTRAINT fk_connections_receiver FOREIGN KEY (receiver_id) REFERENCES users(id),
    INDEX idx_connections_requester_status (requester_id, status),
    INDEX idx_connections_receiver_status (receiver_id, status)
);

CREATE TABLE recommendations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    recommended_user_id BIGINT NOT NULL,
    score DECIMAL(5,4) NOT NULL,
    explanation TEXT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'NEW',
    source_version VARCHAR(50) NULL,
    generated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_recommendations_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_recommendations_recommended FOREIGN KEY (recommended_user_id) REFERENCES users(id),
    INDEX idx_recommendations_user_generated (user_id, generated_at)
);

CREATE TABLE conversations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE conversation_participants (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    conversation_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    joined_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_read_at DATETIME NULL,
    UNIQUE KEY uk_conversation_user (conversation_id, user_id),
    CONSTRAINT fk_cp_conversation FOREIGN KEY (conversation_id) REFERENCES conversations(id),
    CONSTRAINT fk_cp_user FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_cp_user (user_id)
);

CREATE TABLE messages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    conversation_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    message_type VARCHAR(20) NOT NULL DEFAULT 'TEXT',
    sent_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    edited_at DATETIME NULL,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_messages_conversation FOREIGN KEY (conversation_id) REFERENCES conversations(id),
    CONSTRAINT fk_messages_sender FOREIGN KEY (sender_id) REFERENCES users(id),
    INDEX idx_messages_conversation_time (conversation_id, sent_at)
);

CREATE TABLE recommendation_feedback (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    recommendation_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    rating INT NOT NULL,
    feedback_text TEXT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_feedback_recommendation FOREIGN KEY (recommendation_id) REFERENCES recommendations(id),
    CONSTRAINT fk_feedback_user FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_feedback_recommendation (recommendation_id)
);
