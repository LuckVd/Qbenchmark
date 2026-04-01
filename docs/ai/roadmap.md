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

**当前目标：** G23 语义信息移除 (已完成)

**验证覆盖率：** 23/23 = 100% ✅

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
| 关键依赖 | Log4j 2.14.1 (漏洞版本), HttpClient, Commons Lang3, Velocity 2.3, FreeMarker 2.3.31 |

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

### G05: Web 安全漏洞（低-中危）

添加 URL 重定向、文件上传、JWT、CORS、CSRF、CRLF 注入、Cookies 等漏洞。

### G06: 验证测试完善

修复和增强验证测试套件，添加 Docker 部署支持。

### G07: 环境验证靶场

创建一个独立、简化的 Java 靶场，用于安全扫描器的环境构建检测。只保留单个漏洞端点，但保持完整的 Java 项目框架结构。

### G08: 扩展反序列化漏洞（高危）

添加 XStream、SnakeYaml、XMLDecoder 反序列化漏洞，完善 Java 反序列化攻击面覆盖。

### G09: 脚本引擎注入（中危）

添加 Groovy 脚本引擎注入漏洞，扩展脚本/表达式注入类别。

### G10: 其他注入类漏洞（低-中危）

添加 XPath 注入、IP 伪造等其他注入类漏洞。

## 5. 路线图进度表

| 目标ID | 子目标ID | 名称 | 描述 | 状态 | 前置依赖 | 风险/阻塞 | 验收结果 | 测试状态 | 实现时间 | Commit ID | 备注 |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
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
| G03 |  | 表达式注入 | 添加 SpEL、QLExpress 注入漏洞 | done | G00 | 需添加 QLExpress 依赖 | accepted | passed | 2026-03-31 |  | RCE |
| G03 | G03-S01 | SpEL 注入 | 实现 `/spel/vuln1` 和 `/spel/vuln2` | done | G00 | Spring 自带 | accepted | passed | 2026-03-31 |  | StandardEvaluationContext |
| G03 | G03-S02 | QLExpress 注入 | 实现 `/qlexpress/vuln` 和 `/vuln2` 端点 | done | G00 | 需 QLExpress 依赖 | accepted | passed | 2026-03-31 |  | 阿里表达式引擎 |
| G04 |  | 模板注入 | 添加 Velocity、FreeMarker SSTI | done | G00 |  | accepted | passed | 2026-03-31 |  | RCE |
| G04 | G04-S01 | Velocity SSTI | 实现 `/ssti/velocity` 端点 | done | G00 | 需 Velocity 2.3 依赖 | accepted | passed | 2026-03-31 |  | Velocity.evaluate + POST |
| G04 | G04-S02 | FreeMarker SSTI | 实现 `/ssti/freemarker` 端点 | done | G00 | 需 FreeMarker 依赖 | accepted | passed | 2026-03-31 |  | 模板注入 + POST |
| G05 |  | Web 安全漏洞 | 添加 URL 重定向、文件上传等 Web 漏洞 | done | G00 | 需 JWT/FileUpload 依赖 | accepted | passed | 2026-03-31 | 5247a5c | 钓鱼/Shell |
| G05 | G05-S01 | URL 重定向 | 实现 `/urlRedirect/*` 三种方式 | done | G00 |  | accepted | passed | 2026-03-31 | 5247a5c | redirect/setHeader/sendRedirect |
| G05 | G05-S02 | 文件上传 | 实现 `/file/upload` 和 `/file/upload/picture` | done | G00 | 需 FileUpload 依赖 | accepted | passed | 2026-03-31 | 5247a5c || G05 | G05-S02 | 文件上传 | 实现 `/file/upload` 和 `/file/upload/picture` | done | G00 | 需 FileUpload 依赖 | accepted | passed | 2026-03-31 | 5247a5c | Webshell 上传 |
| G05 | G05-S03 | JWT 漏洞 | 实现 `/jwt/*` 签名伪造和算法降级 | done | G00 | 需 JWT 依赖 | accepted | passed | 2026-03-31 | 5247a5c || G05 | G05-S03 | JWT 漏洞 | 实现 `/jwt/*` 签名伪造和算法降级 | done | G00 | 需 JWT 依赖 | accepted | passed | 2026-03-31 | 5247a5c | JWT 安全 |
| G05 | G05-S04 | CORS/CSRF/Cookies | 实现 CORS、CSRF、Cookies 相关漏洞 | done | G00 |  | accepted | passed | 2026-03-31 | 5247a5c || G05 | G05-S04 | CORS/CSRF/Cookies | 实现 CORS、CSRF、Cookies 相关漏洞 | done | G00 |  | accepted | passed | 2026-03-31 | 5247a5c | 浏览器安全 |
| G05 | G05-S05 | CRLF 注入 | 实现 `/crlf/injection` 端点 | done | G00 |  | accepted | passed | 2026-03-31 | 5247a5c || G05 | G05-S05 | CRLF 注入 | 实现 `/crlf/injection` 端点 | done | G00 |  | accepted | passed | 2026-03-31 | 5247a5c | HTTP 响应拆分 |
| G06 |  | 验证测试完善 | 修复和增强验证测试套件 | done | G01,G02,G03,G04,G05 |  | accepted | passed | 2026-03-31 | cf2d8af | 100% 通过率 |
| G06 | G06-S01 | SSTI 端点修复 | 修复 Velocity/FreeMarker POST 端点，升级 Velocity 2.3 | done | G04 |  | accepted | passed | 2026-03-31 |  | 绕过 Spring 拦截 |
| G06 | G06-S02 | 命令注入修复 | 修复 Ping 端点使用 sh -c | done | G03 |  | accepted | passed | 2026-03-31 |  | 真正的 RCE |
| G06 | G06-S03 | 验证脚本增强 | 更新测试语法，添加 Docker 支持 | done |  | Docker 新增 | accepted | passed | 2026-03-31 |  | 完整验证工具 |
| G07 |  | 环境验证靶场 | 创建简化版 Java 靶场用于扫描器环境检测 | done |  |  | accepted | passed | 2026-03-31 |  | 6 个文件 |
| G07 | G07-S01 | 项目框架创建 | 创建完整的 Spring Boot 项目结构 | done |  |  | accepted | passed | 2026-03-31 |  | Maven/标准布局 |
| G07 | G07-S02 | 命令注入端点 | 实现 /ping 命令注入端点 | done | G07-S01 |  | accepted | passed | 2026-03-31 |  | 无外部依赖 |
| G07 | G07-S03 | 环境检测标记 | 添加 /info 版本标识端点 | done | G07-S01 |  | accepted | passed | 2026-03-31 |  | 扫描器识别 |
| G08 |  | 扩展反序列化漏洞 | 添加 XStream、SnakeYaml、XMLDecoder 反序列化 | done |  |  | accepted | passed | 2026-03-31 |  | 高危 RCE |
| G08 | G08-S01 | XStream 反序列化 | 实现 `/deserialize/xstream` 端点 | done |  |  | accepted | passed | 2026-03-31 |  | XStream 1.4.10 |
| G08 | G08-S02 | SnakeYaml 反序列化 | 实现 `/deserialize/yaml` 端点 | done |  |  | accepted | passed | 2026-03-31 |  | SnakeYaml 1.27 |
| G08 | G08-S03 | XMLDecoder 反序列化 | 实现 `/deserialize/xmldecoder` 端点 | done |  |  | accepted | passed | 2026-03-31 |  | Java 原生 |
| G09 |  | 脚本引擎注入 | 添加 Groovy 脚本引擎注入漏洞 | done |  |  | accepted | passed | 2026-03-31 |  | 代码执行 |
| G09 | G09-S01 | Groovy 注入 | 实现 `/cmd/groovy` 端点 | done |  |  | accepted | passed | 2026-03-31 |  | Groovy 2.5.6 |
| G10 |  | 其他注入类漏洞 | 添加 XPath 注入、IP 伪造等 | done |  |  | accepted | passed | 2026-03-31 |  | 补充覆盖 |
| G10 | G10-S01 | XPath 注入 | 实现 `/xpath/login` 端点 | done |  |  | accepted | passed | 2026-03-31 |  | JDK 原生 |
| G10 | G10-S02 | IP 伪造 | 实现 `/ip/spoof` 端点 | done |  |  | accepted | passed | 2026-03-31 |  | HTTP 头伪造 |
| G11 |  | JNDI 注入（高危） | 添加 JNDI RMI/LDAP 远程代码加载漏洞 | done | G01 |  | accepted | passed | 2026-03-31 |  | Log4Shell 核心攻击向量 |
| G11 | G11-S01 | JNDI RMI 注入 | 实现 `/jndi/rmi/vuln` 端点 | done | G11 |  | accepted | passed | 2026-03-31 |  | RMI 远程对象加载 |
| G11 | G11-S02 | JNDI LDAP 注入 | 实现 `/jndi/ldap/vuln` 端点 | done | G11 |  | accepted | passed | 2026-03-31 |  | LDAP 远程类加载 |
| G12 |  | HTTP Smuggling（高危） | 添加 HTTP 请求走私漏洞 | done |  |  | accepted | passed | 2026-03-31 |  | CL/TE 不匹配 |
| G12 | G12-S01 | CL.TE Smuggling | 实现 `/smuggling/clte` 端点 | done | G12 |  |  | accepted | passed | 2026-03-31 |  | Content-Length/Transfer-Encoding |
| G12 | G12-S02 | TE.CL Smuggling | 实现 `/smuggling/tecl` 端点 | done | G12 |  |  | accepted | passed | 2026-03-31 |  | Transfer-Encoding/Content-Length |
| G13 |  | IDOR（中危） | 添加越权访问漏洞 | done |  |  | accepted | passed | 2026-03-31 |  | API Security Top 10 |
| G13 | G13-S01 | IDOR 水平越权 | 实现 `/idor/user/profile` 端点 | done | G13 |  |  | accepted | passed | 2026-03-31 |  | 访问他人数据 |
| G13 | G13-S02 | IDOR 垂直越权 | 实现 `/idor/admin/config` 端点 | done | G13 |  |  | accepted | passed | 2026-03-31 |  | 普通用户越权 |
| G14 |  | DoS/DDoS（中危） | 添加资源耗尽攻击 | done |  |  | accepted | passed | 2026-03-31 |  | 拒绝服务 |
| G14 | G14-S01 | 正则 DoS | 实现 `/dos/regex` 端点 | done | G14 |  |  | accepted | passed | 2026-03-31 |  | ReDoS 恶意正则 |
| G14 | G14-S02 | 内存 DoS | 实现 `/dos/memory` 端点 | done | G14 |  |  | accepted | passed | 2026-03-31 |  | 大数组/死循环 |
| G15 |  | Logic Flaw（中危） | 添加业务逻辑漏洞 | done |  |  | accepted | passed | 2026-03-31 |  | 业务安全 |
| G15 | G15-S01 | 支付逻辑漏洞 | 实现 `/logic/payment` 端点 | done | G15 |  |  | accepted | passed | 2026-03-31 |  | 金额篡改 |
| G15 | G15-S02 | 验证码绕过 | 实现 `/logic/captcha` 端点 | done | G15 |  |  | accepted | passed | 2026-03-31 |  | 验证码复用/可预测 |
| G16 |  | RMI 漏洞（中危） | 添加 RMI Registry 反序列化 | done | G11 |  | accepted | passed | 2026-03-31 |  | RMI 服务端 |
| G16 | G16-S01 | RMI Registry 漏洞 | 实现 RMI 服务端和客户端 | done | G16 |  |  | accepted | passed | 2026-03-31 |  | 远程反序列化 |
| G17 |  | Login Bypass（中危） | 添加认证绕过漏洞 | done |  |  | accepted | passed | 2026-03-31 |  | 认证安全 |
| G17 | G17-S01 | SQL 登录绕过 | 实现 `/login/sqlbypass` 端点 | done | G17 |  |  | accepted | passed | 2026-03-31 |  | 万能密码 |
| G17 | G17-S02 | JWT 绕过 | 实现 `/login/jwtbypass` 端点 | done | G17 |  |  | accepted | passed | 2026-03-31 |  | 签名绕过 |
| G18 |  | Unauthorized Access（中危） | 添加未授权访问 | done |  |  | accepted | passed | 2026-03-31 |  | 访问控制 |
| G18 | G18-S01 | 路径遍历越权 | 实现 `/admin/dashboard` 等端点 | done | G18 |  |  | accepted | passed | 2026-03-31 |  | 未鉴权访问 |
| G19 |  | Password Reset（低危） | 添加密码重置漏洞 | done |  |  | accepted | passed | 2026-03-31 |  | 账户安全 |
| G19 | G19-S01 | Host 注入 | 实现 `/reset/token` 端点 | done | G19 |  |  | accepted | passed | 2026-03-31 |  | Token 泄露/可预测 |
| G20 |  | CSV Injection（低危） | 添加 CSV 注入漏洞 | done |  |  | accepted | passed | 2026-03-31 |  | 数据导出 |
| G20 | G20-S01 | CSV 公式注入 | 实现 `/export/csv` 端点 | done | G20 |  |  | accepted | passed | 2026-03-31 |  | Excel DDE 执行 |
| G21 |  | Blacklist Bypass（低危） | 添加黑名单绕过技巧 | done |  |  | accepted | passed | 2026-03-31 |  | 绕过技巧 |
| G21 | G21-S01 | 文件扩展名绕过 | 实现 `/bypass/extension` 端点 | done | G21 |  |  | accepted | passed | 2026-03-31 |  | 多重后缀 |
| G21 | G21-S02 | MIME 类型绕过 | 实现 `/bypass/mime` 端点 | done | G21 |  |  | accepted | passed | 2026-03-31 |  | Content-Type 伪造 |
| G22 |  | 混淆靶场建设 | 创建独立混淆靶场，多层混淆技术 | done |  |  | accepted | passed | 2026-04-01 |  | 源码级混淆 |
| G22 | G22-S01 | 项目骨架 | 创建 java-test-app-obf 目录结构、pom.xml、启动类 | done |  |  |  | passed | 2026-04-01 |  | 端口 8081 |
| G22 | G22-S02 | 混淆工具类 | ReflectionUtil、StringObfuscator、EncodingUtil | done |  |  |  | passed | 2026-04-01 |  | 反射/编码工具 |
| G22 | G22-S03 | 核心漏洞混淆 | SQLi、CMDi、反序列化、XXE、SpEL | done |  |  |  | passed | 2026-04-01 |  | 高危漏洞混淆 |
| G22 | G22-S04 | 其余漏洞混淆 | 模板、Web、高中低危漏洞 | done |  |  |  | passed | 2026-04-01 |  | 30+ 漏洞端点 |
| G22 | G22-S05 | ProGuard 集成 | ProGuard 配置 (源码级混淆已完成) | done |  |  |  | passed | 2026-04-01 |  | 混淆配置 |
| G22 | G22-S06 | 测试验证 | validate_obf.sh 验证脚本 | done |  |  |  | passed | 2026-04-01 |  | 7/7 通过 |
| G23 |  | 语义信息移除 | 移除两个靶场的漏洞语义信息，保证漏洞功能正常 | done | G22 |  | accepted | passed | 2026-04-01 |  | 类名/路径/注释混淆 |
| G23 | G23-S01 | 主靶场类名重命名 | 30 个控制器重命名 (SQLInjectionController → QueryController) | done | G23 |  |  | passed | 2026-04-01 |  | 移除语义类名 |
| G23 | G23-S02 | 主靶场路径重映射 | API 路径重映射 (/sqli/* → /api/v1/query/*) | done | G23 |  |  | passed | 2026-04-01 |  | 通用路径格式 |
| G23 | G23-S03 | 主靶场注释清理 | 移除 Javadoc 和中文漏洞注释 | done | G23 |  |  | passed | 2026-04-01 |  | 移除语义描述 |
| G23 | G23-S04 | 混淆靶场注释清理 | 移除 18 个文件的语义注释 | done | G23 |  |  | passed | 2026-04-01 |  | 移除漏洞描述 |
| G23 | G23-S05 | 验证脚本更新 | 更新 9 个验证脚本路径映射 | done | G23 |  |  | passed | 2026-04-01 |  | 同步新路径 |

## 6. 开放风险与阻塞

- 暂无长期风险记录。
