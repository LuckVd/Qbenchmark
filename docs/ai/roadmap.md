# 项目路线图

本文件是项目总体技术设计与长期进度的唯一事实来源。

使用原则：

- 总体技术方向、阶段规划、目标拆分、依赖关系统一写在这里。
- 当前正在执行的目标细节写在 `current-goal.md`，不要把步骤级执行过程堆到本文件。
- 路线图中的目标和子目标必须使用稳定编号，便于依赖跟踪和同步。
- 目标完成后的实现结果、测试结果、提交记录应回写到本文件表格中。

## 1. 项目概述

**项目名称：** Qbenchmark - Java 安全漏洞靶场

**技术目标：** 构建一个完整的 Java 安全漏洞测试基准平台，用于安全工具测试、漏洞复现和安全教学。

**当前阶段：** feature-development

**主文档说明：** 本文件负责记录总体设计、阶段目标和实现进度。

## 2. 总体技术架构

### 2.1 核心模块

| 模块ID | 模块名称 | 职责 | 关键接口/输入输出 | 备注 |
|---|---|---|---|---|
| M01 | VulnLabApp | Spring Boot 应用入口，负责启动和配置 | HTTP 端口 8080 | 主应用 |
| M02 | VulnerabilityControllers | 漏洞端点实现，各类型漏洞的易受攻击代码 | `/sqli/*`, `/ssrf/*`, `/cmd/*`, 等 | 核心漏洞模块 |
| M03 | ValidationSuite | 验证测试套件，验证漏洞可利用性 | Shell 脚本 + Payload | 验证模块 |
| M04 | Utils | 工具类，命令执行、路径处理、HTTP 请求 | CommandUtil, PathUtil, HttpUtil | 辅助模块 |

### 2.2 关键集成关系

- VulnLabApp 依赖 Spring Boot 2.7.18 框架
- VulnerabilityControllers 依赖 Utils 工具类
- ValidationSuite 独立运行，通过 HTTP 请求验证漏洞
- MySQL 8.0 / H2 用于 SQL 注入测试

### 2.3 技术栈

| 类别 | 技术 |
|------|------|
| 语言 | Java 1.8+ |
| 框架 | Spring Boot 2.7.18 |
| 构建工具 | Maven |
| 数据库 | MySQL 8.0, H2 (内存) |
| 关键依赖 | Log4j 2.14.1 (漏洞版本), HttpClient, Commons Lang3 |

## 3. 设计约束

- 优先复用现有模块，不要为单个目标创建孤立实现。
- 敏感信息不得硬编码在源码、配置或容器文件中。
- 当前目标实现应遵循 TDD，至少保证目标相关测试通过。
- 所有漏洞端点应同时提供漏洞版本和安全版本（用于对比）。
- 遵循 java-sec-code 的命名和结构约定，便于对比学习。

## 4. 阶段目标

### G01: 反序列化漏洞（高危）

添加 Java 反序列化相关漏洞，这是 Java 应用中最危险的高危漏洞类型。

### G02: XXE 漏洞（高危）

添加 XML 外部实体注入漏洞，支持多种 XML 解析器。

### G03: 表达式注入（中危）

添加 SpEL、QLExpress 表达式注入漏洞。

### G04: 模板注入（中危）

添加 Velocity、FreeMarker SSTI 漏洞。

### G05: 其他安全漏洞（低-中危）

添加 URL 重定向、文件上传、JWT、CORS、CSRF、CRLF 注入、Cookies 等漏洞。

## 5. 路线图进度表

| 目标ID | 子目标ID | 名称 | 描述 | 状态 | 前置依赖 | 风险/阻塞 | 验收结果 | 测试状态 | 实现时间 | Commit ID | 备注 |
|---|---|---|---|---|---|---|---|---|---|---|---|
| G00 |  | 项目基础骨架 | 初始化仓库结构、基础配置和 AI 工作流 | done |  |  | accepted | passed | 2026-03-30 |  | 已有 6 种基础漏洞 |
| G01 |  | 反序列化漏洞 | 添加 Jackson、Fastjson、Shiro 反序列化漏洞 | done | G00 |  | accepted | passed | 2026-03-30 |  | 高危 RCE |
| G01 | G01-S01 | Jackson 反序列化 | 实现 `/deserialize/jackson` 端点 | done | G00 |  | accepted | passed | 2026-03-30 |  | enableDefaultTyping |
| G01 | G01-S02 | Fastjson 反序列化 | 实现 `/fastjson/deserialize` 端点 | done | G00 | 需添加 fastjson 依赖 | accepted | passed | 2026-03-30 |  | autoType |
| G01 | G01-S03 | Shiro 反序列化 | 实现 `/shiro/deserialize` 端点 | done | G00 | 需添加 Shiro 依赖 | accepted | passed | 2026-03-30 |  | rememberMe |
| G01 | G01-S04 | Cookie 反序列化 | 实现 `/deserialize/rememberMe/vuln` 端点 | done | G01-S01 |  | accepted | passed | 2026-03-30 |  | ysoserial payload |
| G02 |  | XXE 漏洞 | 添加 XML 外部实体注入漏洞 | done | G00 |  | accepted | passed | 2026-03-30 |  | 文件读取/SSRF |
| G02 | G02-S01 | XMLReader XXE | 实现 `/xxe/xmlReader/vuln` 端点 | done | G00 |  | accepted | passed | 2026-03-30 |  | 多种解析器 |
| G02 | G02-S02 | SAXBuilder XXE | 实现 `/xxe/SAXBuilder/vuln` 端点 | done | G00 | 需 JDOM2 依赖 | accepted | passed | 2026-03-30 |  | DOM4J/JDOM2 |
| G02 | G02-S03 | DocumentBuilder XXE | 实现 `/xxe/DocumentBuilder/vuln` 端点 | done | G00 |  | accepted | passed | 2026-03-30 |  | 标准解析器 |
| G03 |  | 表达式注入 | 添加 SpEL、QLExpress 注入漏洞 | planned | G00 |  | pending | not_started |  |  | RCE |
| G03 | G03-S01 | SpEL 注入 | 实现 `/spel/vuln1` 和 `/spel/vuln2` | planned | G00 | Spring 自带 | pending | not_started |  |  | StandardEvaluationContext |
| G03 | G03-S02 | QLExpress 注入 | 实现 `/qlexpress/vuln` 端点 | planned | G00 | 需 QLExpress 依赖 | pending | not_started |  |  | 阿里表达式引擎 |
| G04 |  | 模板注入 | 添加 Velocity、FreeMarker SSTI | planned | G00 |  | pending | not_started |  |  | RCE |
| G04 | G04-S01 | Velocity SSTI | 实现 `/ssti/velocity` 端点 | planned | G00 | 需 Velocity 依赖 | pending | not_started |  |  | Velocity.evaluate |
| G04 | G04-S02 | FreeMarker SSTI | 实现 `/ssti/freemarker` 端点 | planned | G00 | 需 FreeMarker 依赖 | pending | not_started |  |  | 模板注入 |
| G05 |  | Web 安全漏洞 | 添加 URL 重定向、文件上传等 Web 漏洞 | planned | G00 |  | pending | not_started |  |  | 钓鱼/Shell |
| G05 | G05-S01 | URL 重定向 | 实现 `/urlRedirect/*` 三种方式 | planned | G00 |  | pending | not_started |  |  | redirect/setHeader/sendRedirect |
| G05 | G05-S02 | 文件上传 | 实现 `/file/upload` 和 `/file/upload/picture` | planned | G00 |  | pending | not_started |  |  | Webshell 上传 |
| G05 | G05-S03 | JWT 漏洞 | 实现 `/jwt/*` 签名伪造和算法降级 | planned | G00 | 需 JWT 依赖 | pending | not_started |  |  | JWT 安全 |
| G05 | G05-S04 | CORS/CSRF/Cookies | 实现 CORS、CSRF、Cookies 相关漏洞 | planned | G00 |  | pending | not_started |  |  | 浏览器安全 |
| G05 | G05-S05 | CRLF 注入 | 实现 `/crlf injection` 端点 | planned | G00 |  | pending | not_started |  |  | HTTP 响应拆分 |
| G06 |  | 验证测试完善 | 为所有新增漏洞添加验证脚本 | planned | G01,G02,G03,G04,G05 |  | pending | not_started |  |  | 测试覆盖率 |

## 6. 开放风险与阻塞

- 暂无长期风险记录。
