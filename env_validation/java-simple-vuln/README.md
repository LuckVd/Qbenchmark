# Java Simple Vulnerability Lab

## 环境验证靶场

这是一个简化的 Java 安全漏洞靶场，用于安全扫描器的环境构建检测。

## 特点

- **最小化**: 只包含单个命令注入漏洞
- **完整框架**: 标准 Maven/Spring Boot 项目结构
- **无外部依赖**: 不需要数据库等额外容器
- **可检测**: 提供明确的版本标识

## 快速启动

### Maven 方式

```bash
mvn spring-boot:run
```

### Docker 方式

```bash
docker build -t java-simple-vuln .
docker run -p 8080:8080 java-simple-vuln
```

## 端点说明

| 端点 | 方法 | 说明 |
|------|------|------|
| `/ping?ip=xxx` | GET | 命令注入漏洞端点 |
| `/info` | GET | 环境信息 |

## 漏洞测试

### 命令注入 Payload

```bash
# 执行 whoami
curl "http://localhost:8080/ping?ip=127.0.0.1;whoami"

# 读取 /etc/passwd
curl "http://localhost:8080/ping?ip=127.0.0.1|cat%20/etc/passwd"

# 列出文件
curl "http://localhost:8080/ping?ip=127.0.0.1&&ls%20-la"
```

## 项目结构

```
java-simple-vuln/
├── pom.xml
├── Dockerfile
├── README.md
└── src/main/
    ├── java/com/envtest/
    │   ├── SimpleApplication.java
    │   └── controller/
    │       └── CmdInjectionController.java
    └── resources/
        └── application.yml
```

## 技术栈

- Java 1.8
- Spring Boot 2.7.18
- Maven

## 用于扫描器检测

本项目旨在为安全扫描器提供一个标准的 Java 项目环境，扫描器应能：

1. 识别为 Maven 项目
2. 识别为 Spring Boot 应用
3. 识别 Java 版本
4. 检测到命令注入漏洞
5. 构建并运行 Docker 镜像
