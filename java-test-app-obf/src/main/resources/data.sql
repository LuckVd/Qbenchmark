-- Initialize H2 database with test data

CREATE TABLE IF NOT EXISTS users (
    id INT PRIMARY KEY IDENTITY,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    role VARCHAR(20) DEFAULT 'user'
);

-- Insert test data
INSERT INTO users (username, password, email, role) VALUES
('admin', 'admin123', 'admin@example.com', 'admin'),
('user1', 'password1', 'user1@example.com', 'user'),
('user2', 'password2', 'user2@example.com', 'user'),
('test', 'test123', 'test@example.com', 'user');
