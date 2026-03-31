# Project Summary

Status: feature-development (所有计划目标 G01-G06 已完成)

## Purpose

Qbenchmark 是一个 Java 安全漏洞靶场项目，用于安全工具测试、漏洞复现和安全教学。目标是构建一个完整的 Java 安全漏洞测试基准平台，包含 50+ 个易受攻击的端点。

## Core Modules

- **VulnLabApp** (M01): Spring Boot 主应用，端口 8080
- **VulnerabilityControllers** (M02): 20 个控制器，50+ 漏洞端点
- **ValidationSuite** (M03): Shell 验证脚本，11 个 Payload 文件，辅助工具
- **Utils** (M04): CommandUtil, PathUtil, HttpUtil

## Tech Stack

- Java 1.8+
- Spring Boot: 2.7.18
- Maven
- MySQL 8.0 / H2
- Log4j: 2.14.1 (漏洞版本)
- Fastjson: 1.2.24 (漏洞版本)
- Shiro: 1.2.4 (漏洞版本)
- Velocity: 2.3 (已升级)
- FreeMarker: 2.3.31
- QLExpress: 3.3.4
- JWT: 0.9.1 (漏洞版本)

## Key Boundaries

- 控制器: `com.vulnlab.controller`
- 工具类: `com.vulnlab.util`
- 验证脚本: `validation/`
- Payload 文件: `validation/payloads/`

## 已实现漏洞 (15+ 种)

| 漏洞类型 | 控制器 | 端点数 | 状态 |
|---------|--------|--------|------|
| SQL Injection | SQLInjectionController | 3 | ✅ |
| SSRF | SSRFController | 2 | ✅ |
| Command Injection | CommandInjectionController | 5 | ✅ |
| XSS | XSSController | 2 | ✅ |
| Log4Shell | Log4jController | 1 | ✅ |
| Path Traversal | PathTraversalController | 2 | ✅ |
| 反序列化 | DeserializeController, FastjsonController, ShiroController | 4 | ✅ |
| XXE | XXEController | 3 | ✅ |
| 表达式注入 | SpelController, QLExpressController | 4 | ✅ |
| 模板注入 | VelocityController, FreeMarkerController | 4 | ✅ |
| URL 重定向 | UrlRedirectController | 4 | ✅ |
| 文件上传 | FileUploadController | 3 | ✅ |
| JWT 漏洞 | JwtController | 4 | ✅ |
| CORS/CSRF/Cookies | CorsCsrfController | 6 | ✅ |
| CRLF 注入 | CrlfInjectionController | 6 | ✅ |

## Maintenance Notes

- 2026-03-30: AI 工作流初始化完成
- 2026-03-30: G01 反序列化漏洞完成
- 2026-03-30: G02 XXE 漏洞完成
- 2026-03-31: G03 表达式注入完成
- 2026-03-31: G04 模板注入完成
- 2026-03-31: G05 Web 安全漏洞完成
- 2026-03-31: G06 验证测试完善完成 (本次会话)
- 2026-03-31: 所有计划目标已完成 ✅
