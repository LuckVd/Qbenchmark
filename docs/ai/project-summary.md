# Project Summary

Status: feature-development

## Purpose

Qbenchmark 是一个 Java 安全漏洞靶场项目，用于与 java-sec-code 进行对比基准测试。目标是构建一个完整的 Java 安全漏洞测试平台，包含常见高危漏洞的易受攻击代码示例。

## Core Modules

- **VulnLabApp** (M01): Spring Boot 主应用
- **VulnerabilityControllers** (M02): 漏洞端点实现
- **ValidationSuite** (M03): 验证测试套件
- **Utils** (M04): 命令执行、路径处理、HTTP 请求工具类

## Tech Stack

- Java 1.8+
- Spring Boot 2.7.18
- Maven
- MySQL 8.0 / H2
- Log4j 2.14.1 (漏洞版本)

## Key Boundaries

- 漏洞端点: `/sqli/*`, `/ssrf/*`, `/cmd/*`, `/xss/*`, `/log4j/*`, `/path/*`
- 工具类: `com.vulnlab.util`
- 控制器: `com.vulnlab.controller`
- 验证脚本: `validation/quick_validate.sh`

## 已实现漏洞 (6 种)

| 漏洞类型 | 控制器 | 端点 |
|---------|--------|------|
| SQL Injection | SQLInjectionController | `/sqli/jdbc/vuln` |
| SSRF | SSRFController | `/ssrf/urlconnection/vuln` |
| Command Injection | CommandInjectionController | `/cmd/runtime/vuln`, `/cmd/processbuilder/vuln` |
| XSS | XSSController | `/xss/reflect` |
| Log4Shell | Log4jController | `/log4j/vuln` |
| Path Traversal | PathTraversalController | `/path/absolute/vuln` |

## 待实现漏洞 (15+ 种)

- 反序列化: Jackson, Fastjson, Shiro, Cookie
- XXE: XMLReader, SAXBuilder, DocumentBuilder
- 表达式注入: SpEL, QLExpress
- 模板注入: Velocity, FreeMarker
- Web 安全: URL 重定向, 文件上传, JWT, CORS, CSRF, CRLF, Cookies

## Recent Maintenance Notes

- 2026-03-30: AI 工作流初始化完成
- 2026-03-30: 路线图已更新，规划新增 15+ 种漏洞类型
