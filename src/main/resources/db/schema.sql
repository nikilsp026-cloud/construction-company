-- =========================================================
-- Construction Company Management System - MySQL Schema
-- Reference only: Hibernate auto-generates this via ddl-auto=update
-- Run manually only if you want a clean start (truncate or recreate)
-- =========================================================

CREATE DATABASE IF NOT EXISTS construction_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE construction_db;

-- ===================================
-- ROLES
-- ===================================
CREATE TABLE IF NOT EXISTS roles (
    id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- ===================================
-- USERS
-- ===================================
CREATE TABLE IF NOT EXISTS users (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(100) NOT NULL UNIQUE,
    email         VARCHAR(150) NOT NULL UNIQUE,
    password      VARCHAR(255) NOT NULL,
    full_name     VARCHAR(200),
    phone         VARCHAR(20),
    profile_photo VARCHAR(500),
    enabled       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    DATETIME,
    updated_at    DATETIME
);

-- ===================================
-- USER_ROLES (Junction Table)
-- ===================================
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_ur_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_ur_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
);

-- ===================================
-- SERVICES (Construction Services)
-- ===================================
CREATE TABLE IF NOT EXISTS services (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    title         VARCHAR(200) NOT NULL,
    description   TEXT,
    icon          VARCHAR(100),
    image         VARCHAR(500),
    status        ENUM ('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    display_order INT                                  DEFAULT 0,
    created_at    DATETIME,
    updated_at    DATETIME
);

-- ===================================
-- PROJECTS
-- ===================================
CREATE TABLE IF NOT EXISTS projects (
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    name                  VARCHAR(200) NOT NULL,
    description           TEXT,
    client_name           VARCHAR(200),
    location              VARCHAR(300),
    start_date            DATE,
    completion_date       DATE,
    status                ENUM ('ONGOING','COMPLETED','UPCOMING','ON_HOLD') NOT NULL DEFAULT 'ONGOING',
    budget                DECIMAL(15, 2),
    thumbnail             VARCHAR(500),
    category              VARCHAR(100),
    featured              BOOLEAN                                                    DEFAULT FALSE,
    completion_percentage INT                                                        DEFAULT 0,
    created_at            DATETIME,
    updated_at            DATETIME
);

-- ===================================
-- PROJECT_IMAGES
-- ===================================
CREATE TABLE IF NOT EXISTS project_images (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT       NOT NULL,
    image_path VARCHAR(500),
    alt_text   VARCHAR(200),
    created_at DATETIME,
    CONSTRAINT fk_pi_project FOREIGN KEY (project_id) REFERENCES projects (id) ON DELETE CASCADE
);

-- ===================================
-- GALLERY
-- ===================================
CREATE TABLE IF NOT EXISTS gallery (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    title       VARCHAR(200),
    description TEXT,
    image_path  VARCHAR(500) NOT NULL,
    category    VARCHAR(100),
    created_at  DATETIME
);

-- ===================================
-- BLOGS
-- ===================================
CREATE TABLE IF NOT EXISTS blogs (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    title             VARCHAR(300) NOT NULL,
    slug              VARCHAR(300) NOT NULL UNIQUE,
    short_description TEXT,
    content           LONGTEXT,
    thumbnail         VARCHAR(500),
    tags              VARCHAR(500),
    seo_title         VARCHAR(200),
    seo_description   TEXT,
    published_date    DATETIME,
    author_id         BIGINT,
    status            ENUM ('DRAFT','PUBLISHED','ARCHIVED') NOT NULL DEFAULT 'DRAFT',
    created_at        DATETIME,
    updated_at        DATETIME,
    CONSTRAINT fk_blog_author FOREIGN KEY (author_id) REFERENCES users (id) ON DELETE SET NULL
);

-- ===================================
-- TESTIMONIALS
-- ===================================
CREATE TABLE IF NOT EXISTS testimonials (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_name VARCHAR(200) NOT NULL,
    photo         VARCHAR(500),
    company       VARCHAR(200),
    rating        INT                                NOT NULL DEFAULT 5,
    comment       TEXT,
    status        ENUM ('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_at    DATETIME
);

-- ===================================
-- TEAM_MEMBERS
-- ===================================
CREATE TABLE IF NOT EXISTS team_members (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(200) NOT NULL,
    designation   VARCHAR(200),
    photo         VARCHAR(500),
    facebook      VARCHAR(300),
    linkedin      VARCHAR(300),
    instagram     VARCHAR(300),
    description   TEXT,
    display_order INT          DEFAULT 0,
    active        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    DATETIME
);

-- ===================================
-- CONTACT_MESSAGES
-- ===================================
CREATE TABLE IF NOT EXISTS contact_messages (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(200) NOT NULL,
    phone      VARCHAR(20),
    email      VARCHAR(150),
    subject    VARCHAR(300),
    message    TEXT,
    is_read    BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME
);

-- ===================================
-- QUOTE_REQUESTS
-- ===================================
CREATE TABLE IF NOT EXISTS quote_requests (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    name              VARCHAR(200) NOT NULL,
    phone             VARCHAR(20),
    email             VARCHAR(150),
    location          VARCHAR(300),
    budget            VARCHAR(100),
    construction_type VARCHAR(200),
    area              VARCHAR(100),
    message           TEXT,
    pdf_path          VARCHAR(500),
    status            ENUM ('PENDING','REVIEWED','APPROVED','REJECTED') NOT NULL DEFAULT 'PENDING',
    created_at        DATETIME
);

-- ===================================
-- WEBSITE_SETTINGS
-- ===================================
CREATE TABLE IF NOT EXISTS website_settings (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    setting_key   VARCHAR(100) NOT NULL UNIQUE,
    setting_value TEXT,
    setting_type  VARCHAR(50)
);

-- ===================================
-- INDEXES (performance)
-- ===================================
CREATE INDEX IF NOT EXISTS idx_projects_status   ON projects (status);
CREATE INDEX IF NOT EXISTS idx_projects_featured ON projects (featured);
CREATE INDEX IF NOT EXISTS idx_projects_category ON projects (category);
CREATE INDEX IF NOT EXISTS idx_blogs_slug        ON blogs (slug);
CREATE INDEX IF NOT EXISTS idx_blogs_status      ON blogs (status);
CREATE INDEX IF NOT EXISTS idx_gallery_category  ON gallery (category);
CREATE INDEX IF NOT EXISTS idx_messages_read     ON contact_messages (is_read);
CREATE INDEX IF NOT EXISTS idx_quotes_status     ON quote_requests (status);

-- ===================================
-- SEED DATA (roles only — app seeds the rest)
-- ===================================
INSERT IGNORE INTO roles (name) VALUES ('ROLE_ADMIN');
INSERT IGNORE INTO roles (name) VALUES ('ROLE_EMPLOYEE');
