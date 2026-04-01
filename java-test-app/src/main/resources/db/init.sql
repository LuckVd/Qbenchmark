-- Mini Vulnerability Lab Database Init Script
-- 用于SQL注入漏洞测试的数据库初始化

CREATE DATABASE IF NOT EXISTS vulndb DEFAULT CHARACTER SET utf8mb4;
USE vulndb;

-- 用户表（用于SQL注入测试）
DROP TABLE IF EXISTS users;
CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    role VARCHAR(20) DEFAULT 'user'
);

-- 插入测试数据
INSERT INTO users (username, password, email, role) VALUES
('admin', 'admin123', 'admin@example.com', 'admin'),
('user1', 'password1', 'user1@example.com', 'user'),
('user2', 'password2', 'user2@example.com', 'user'),
('test', 'test123', 'test@example.com', 'user'),
('guest', 'guest123', 'guest@example.com', 'guest');

-- 显示插入的数据
SELECT * FROM users;
