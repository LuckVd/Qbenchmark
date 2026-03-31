-- H2 Database Initialization Script for Vulnerability Testing
-- Run this in H2 Console at http://localhost:8080/h2-console/

-- Create users table for SQL injection tests
CREATE TABLE IF NOT EXISTS users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50),
    password VARCHAR(50),
    email VARCHAR(100)
);

-- Insert test data
INSERT INTO users (username, password, email) VALUES
    ('admin', 'password123', 'admin@example.com'),
    ('user1', 'pass123', 'user1@example.com'),
    ('test', 'test123', 'test@example.com');

-- Create file reference table for path traversal tests
CREATE TABLE IF NOT EXISTS files (
    id INT PRIMARY KEY AUTO_INCREMENT,
    filename VARCHAR(100),
    content VARCHAR(255)
);

INSERT INTO files (filename, content) VALUES
    ('test.txt', 'This is a test file'),
    ('readme.txt', 'Welcome to the application');
