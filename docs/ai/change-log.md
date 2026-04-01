# Change Log

## 2026-04-01 (G22 混淆靶场建设)

- Goal ID: G22 (混淆靶场建设)
- Summary: 创建独立混淆靶场，使用多层混合混淆技术对 30+ 个漏洞端点进行混淆
- Impact: `java-vuln-obf/`, `validation/validate_obf.sh`
- Changes:
  - **java-vuln-obf/**: 新建独立混淆靶场项目
    - 端口 8081，与原靶场并行运行
    - 三层架构：Facade (REST API) → Handler (间接调用) → Executor (漏洞代码)
  - **混淆工具类**: ReflectionUtil, StringObfuscator, EncodingUtil, PathMapping
  - **混淆技术**:
    - 路径伪装：`/sqli/jdbc/vuln` → `/api/v1/query/user`
    - 字符串分割：SQL 语句分段构建
    - 反射调用：Runtime, ProcessBuilder 通过反射执行
    - Base64 编码：配置参数编码
    - 三层架构：Facade → Handler → Executor 隔离
  - **混淆端点**: 30+ 个控制器，覆盖所有漏洞类型
  - **validate_obf.sh**: 新增混淆靶场验证脚本
- Tests: 7/7 通过 (100%)
  - SQLi Path Obfuscation: ✅
  - Command Exec Reflection: ✅
  - SpEL Expression: ✅
  - SpEL RCE: ✅ (T(java.lang.System).getProperty 返回 root)
  - Jackson Parse: ✅
  - XML Parse: ✅
  - Lab Info: ✅
- 验证脚本覆盖率: **22/22 = 100%** (含 G22)
- Commit: pending

## 2026-03-31 (验证脚本完善)

- Goal ID: 验证脚本全覆盖
- Summary: 补充缺失的 Groovy 脚本注入验证脚本，实现 100% 覆盖
- Impact: `validation/validate_groovy.sh`, `docs/ai/`
- Changes:
  - **validate_groovy.sh**: 新增验证脚本，12 个测试用例
    - 基础算术/字符串操作
    - 命令执行 (whoami, ls, id, pwd)
    - 多命令执行 (复合命令)
    - 环境变量/系统属性读取
    - 文件读取 (/etc/passwd)
    - 反向 Shell payload
    - 安全版本防护测试
  - **current-goal.state.yaml**: 更新验证覆盖率状态
- Tests: 12/12 通过 (100%)
  - Groovy 基础表达式: ✅
  - Groovy 命令执行: ✅
  - Groovy 文件读取: ✅
  - Groovy 安全版本: ✅
- 验证脚本覆盖率: **21/21 = 100%**
- Commit: pending

## 2026-03-31 (本次会话 - G10 其他注入类漏洞)

- Goal ID: G10 (其他注入类漏洞)
- Summary: 添加 XPath 注入、IP 伪造两种漏洞
- Impact: `java-vuln-lab/src/main/java/com/vulnlab/controller/`
- Changes:
  - **XPathController.java**: 新增控制器，3 个端点
    - `/xpath/login` - XPath 注入漏洞（绕过认证）
    - `/xpath/login/safe` - XPath 安全版本（对比）
    - `/xpath/info` - 漏洞说明
  - **IPForgeryController.java**: 新增控制器，4 个端点
    - `/ip/spoof` - IP 伪造漏洞（绕过 IP 限制）
    - `/ip/safe` - 安全版本（使用 RemoteAddr）
    - `/ip/headers` - 显示所有 IP 相关头
    - `/ip/info` - 漏洞说明
- Tests: 全部通过
  - Maven 构建: ✅
  - 应用启动: ✅
  - XPath 注入: ✅ `admin' or '1'='1` 绕过认证
  - IP 伪造: ✅ X-Forwarded-For: 127.0.0.1 绕过限制
- Commit: 6420461
- Deploy: pushed to origin/master

## 2026-03-31 (本次会话 - G09 脚本引擎注入)

- Goal ID: G09 (脚本引擎注入)
- Summary: 添加 Groovy 脚本引擎注入漏洞
- Impact: `java-vuln-lab/pom.xml`, `java-vuln-lab/src/main/java/com/vulnlab/controller/CommandInjectionController.java`
- Changes:
  - **pom.xml**: 添加 Groovy 2.5.6 依赖
  - **CommandInjectionController.java**: 扩展控制器，添加 2 个端点
    - `/cmd/groovy` - Groovy 脚本引擎注入漏洞
    - `/cmd/groovy/safe` - Groovy 安全版本（对比）
- Tests: 全部通过
  - Maven 构建: ✅
  - 应用启动: ✅
  - Groovy 数学运算: ✅ Math.abs(-5) 返回 5
  - Groovy 字符串操作: ✅ "hello".toUpperCase() 返回 HELLO
  - Groovy 命令执行: ✅ "whoami".execute().text 返回 root
- Commit: 3742f57
- Deploy: pushed to origin/master

## 2026-03-31 (本次会话 - G08 扩展反序列化漏洞)

- Goal ID: G08 (扩展反序列化漏洞)
- Summary: 添加 XStream、SnakeYaml、XMLDecoder 三种反序列化漏洞
- Impact: `java-vuln-lab/pom.xml`, `java-vuln-lab/src/main/java/com/vulnlab/controller/`
- Changes:
  - **pom.xml**: 添加 XStream 1.4.10 和 SnakeYaml 1.27 依赖
  - **ExtendDeserializeController.java**: 新增控制器，6 个端点
    - `/deserialize/xstream` - XStream 反序列化漏洞
    - `/deserialize/xstream/safe` - XStream 安全版本（对比）
    - `/deserialize/yaml` - SnakeYaml 反序列化漏洞
    - `/deserialize/yaml/safe` - SnakeYaml 安全版本（对比）
    - `/deserialize/xmldecoder` - XMLDecoder 反序列化漏洞
    - `/deserialize/extend/info` - 依赖版本信息
- Tests: 全部通过
  - Maven 构建: ✅
  - 应用启动: ✅
  - XStream 端点: ✅ HashMap 反序列化成功
  - SnakeYaml 端点: ✅ LinkedHashMap 反序列化成功
  - XMLDecoder 端点: ✅ String 反序列化成功
- Commit: a09de39
- Deploy: pushed to origin/master

## 2026-03-31 (本次会话 - G07 环境验证靶场)

- Goal ID: G07 (环境验证靶场)
- Summary: 创建独立简化的 Java 靶场，用于扫描器环境检测
- Impact: `env_validation/java-simple-vuln/`
- Changes:
  - **pom.xml**: Maven 项目配置，最小依赖
  - **SimpleApplication.java**: Spring Boot 入口类
  - **CmdInjectionController.java**: 命令注入漏洞控制器 (/ping, /info)
  - **application.yml**: 应用配置（端口 8081）
  - **Dockerfile**: 容器镜像定义
  - **README.md**: 使用说明
- Tests: 全部通过
  - Maven 构建: ✅
  - 应用启动: ✅
  - /info 端点: ✅
  - /ping 命令注入: ✅ `;whoami` 返回 root
- Commit: fe42241
- Deploy: pushed to origin/master

## 2026-03-31 (本次会话 - G06 验证测试完善)

- Goal ID: G06 (验证测试完善)
- Summary: 修复 SSTI 端点、命令注入 Ping 端点，增强验证脚本，添加 Docker 支持
- Impact: `java-vuln-lab/pom.xml`, `java-vuln-lab/src/main/java/com/vulnlab/controller/`, `validation/`
- Changes:
  - **VelocityController.java**: 重写，升级 Velocity 到 2.3，添加 POST 端点绕过 Spring 拦截
  - **FreeMarkerController.java**: 添加 POST 端点，支持复杂模板语法
  - **CommandInjectionController.java**: 修复 Ping 端点使用 sh -c 实现真正的命令注入
  - **pom.xml**: 升级 velocity-engine-core 到 2.3
  - **validation/quick_validate.sh**: 更新 SSTI 测试语法和 Ping 测试条件
  - **validation/local_validate.sh**: 新增完整验证脚本
  - **validation/scripts/generate_payloads.py**: 新增 Payload 生成器工具
  - **validation/scripts/jndi_server.py**: 新增 JNDI 服务器用于 Log4Shell RCE 测试
  - **validation/HOWTO_VALIDATE.md**: 新增验证使用说明
  - **docker-compose.yml**: 新增 Docker 编排配置
  - **java-vuln-lab/Dockerfile**: 新增应用容器镜像
- Tests: 33/33 测试通过 (100%)
  - Velocity POST: ✅ #set($x=100)$x
  - FreeMarker POST: ✅ ${"hello"?upper_case}
  - Ping 命令注入: ✅ |whoami 返回 root
- Commit: cf2d8af
- Deploy: pushed to origin/master

## 2026-03-31 (之前会话 - G05 实现)

- Goal ID: 项目扫描 (ai-scan)
- Summary: 刷新项目摘要，所有计划目标已完成
- Impact: `docs/ai/project-summary.md`, `docs/ai/project-tree.md`, `docs/ai/current-goal.md`
- Changes:
  - 更新项目摘要：15+ 种漏洞类型全部完成
  - 更新项目结构树：17 个控制器，50+ 端点
  - 更新当前目标状态：无活跃目标
- 项目状态: 所有计划目标 (G01-G05) 已完成 ✅

- Goal ID: G05 (实现完成)
- Summary: 实现 Web 安全漏洞（URL 重定向、文件上传、JWT、CORS/CSRF/Cookies、CRLF 注入）
- Impact: `java-vuln-lab/pom.xml`, `java-vuln-lab/src/main/java/com/vulnlab/controller/`, `validation/`
- Changes:
  - 添加依赖：JWT 0.9.1, Commons FileUpload
  - 创建 UrlRedirectController.java：4 个 URL 重定向端点
  - 创建 FileUploadController.java：3 个文件上传端点
  - 创建 JwtController.java：4 个 JWT 漏洞端点
  - 创建 CorsCsrfController.java：6 个 CORS/CSRF/Cookies 端点
  - 创建 CrlfInjectionController.java：6 个 CRLF 注入端点
  - 创建 validation/payloads/web_vuln_payloads.txt
  - 更新 validation/quick_validate.sh：添加 Web 漏洞测试
- Tests: 编译通过
- Commit: 5247a5c

- Goal ID: G04 (实现完成)
- Summary: 实现模板注入漏洞（Velocity、FreeMarker）
- Impact: `java-vuln-lab/pom.xml`, `java-vuln-lab/src/main/java/com/vulnlab/controller/`, `validation/`
- Changes:
  - 添加依赖：Velocity 1.7, FreeMarker 2.3.31
  - 创建 VelocityController.java：2 个 Velocity SSTI 端点
  - 创建 FreeMarkerController.java：2 个 FreeMarker SSTI 端点
  - 创建 validation/payloads/ssti_payloads.txt
  - 更新 validation/quick_validate.sh：添加 SSTI 测试
- Tests: 编译通过
- Commit: 6a61481

- Goal ID: G03 (实现完成)
- Summary: 实现表达式注入漏洞（SpEL、QLExpress）
- Impact: `java-vuln-lab/pom.xml`, `java-vuln-lab/src/main/java/com/vulnlab/controller/`, `validation/`
- Changes:
  - 添加依赖：QLExpress 3.3.4
  - 创建 SpelController.java：2 个 SpEL 注入端点
  - 创建 QLExpressController.java：2 个 QLExpress 注入端点
  - 创建 validation/payloads/expression_payloads.txt
  - 更新 validation/quick_validate.sh：添加表达式注入验证
- Tests: 编译通过
  - /spel/vuln1 ✅ (StandardEvaluationContext)
  - /spel/vuln2 ✅ (模板解析)
  - /qlexpress/vuln ✅ (无沙箱)
  - /qlexpress/vuln2 ✅ (带上下文)
- Dead Code: not run
- Security: not run
- Commit Status: not committed

- Goal ID: G02 (同步完成)
- Summary: 同步 G02 完成，提交 AI 工作流和基础代码
- Impact: 完整仓库提交
- Changes:
  - 提交 AI 工作流骨架 (.claude/, docs/ai/)
  - 提交基础漏洞代码和验证脚本
  - 添加 .gitignore
- Commit: 78fd119
- Push: 已推送到远程

- Goal ID: G03 (设计阶段)
- Summary: 启动表达式注入目标设计
- Impact: 待实现
- Changes:
  - 确认范围: SpEL (2个端点) + QLExpress (1个端点)
  - 设计已写入 current-goal.md
- Tests: pending
- Dead Code: pending
- Security: pending

## 2026-03-30

- Goal ID: G02 (实现完成)
- Summary: 实现 XXE 漏洞（XMLReader、SAXBuilder、DocumentBuilder）
- Impact: `java-vuln-lab/src/main/java/com/vulnlab/controller/`, `validation/`
- Changes:
  - 创建 XXEController.java：3 种 XXE 漏洞端点
  - 创建 validation/payloads/xxe_payloads.txt
  - 更新 validation/quick_validate.sh：添加 XXE 端点验证
- Tests: 端点验证通过，成功读取 /etc/passwd
  - /xxe/xmlReader/vuln ✅
  - /xxe/saxBuilder/vuln ✅ (成功泄露文件内容)
  - /xxe/documentBuilder/vuln ✅ (成功泄露文件内容)
- Dead Code: not run
- Security: not run
- Commit Status: not committed

## 2026-03-30

- Goal ID: G01 (实现完成)
- Summary: 实现反序列化漏洞（Jackson、Fastjson、Shiro、Cookie）
- Impact: `java-vuln-lab/pom.xml`, `java-vuln-lab/src/main/java/com/vulnlab/controller/`, `validation/`
- Changes:
  - 添加依赖：fastjson 1.2.24、shiro-core 1.2.4、commons-collections 3.1、dom4j 2.0.0
  - 创建 DeserializeController.java：Jackson enableDefaultTyping 反序列化、Cookie 反序列化
  - 创建 FastjsonController.java：Fastjson autoType 反序列化
  - 创建 ShiroController.java：Shiro rememberMe 反序列化（Shiro-550）
  - 创建 validation/payloads/deserialize_payloads.txt
  - 更新 validation/quick_validate.sh：添加反序列化端点验证
- Tests: 端点验证通过
  - /deserialize/jackson ✅
  - /deserialize/rememberMe/vuln ✅
  - /fastjson/deserialize ✅
  - /shiro/deserialize ✅
- Dead Code: not run
- Security: not run
- Commit Status: not committed

## 2026-03-30

- Goal ID: G01 (设计阶段)
- Summary: 分析 java-sec-code 并创建完整路线图，识别出 15+ 种待实现漏洞类型
- Impact: `docs/ai/roadmap.md`, `docs/ai/project-summary.md`, `docs/ai/project-tree.md`, `docs/ai/current-goal.md`
- Changes:
  - 更新路线图，规划 6 个阶段目标（G01-G06）
  - 识别出 15+ 种缺失漏洞类型：反序列化、XXE、表达式注入、模板注入、Web 安全漏洞
  - 设置当前目标为 G01：反序列化漏洞（Jackson、Fastjson、Shiro）
  - 更新项目摘要和项目结构文档
- Tests: pending
- Dead Code: not run
- Security: not run
- Commit Status: not committed

## 2026-03-19

- Goal ID: bootstrap
- Summary: Initialized the Claude Code workflow skeleton.
- Impact: `docs/ai`, `.claude/commands`, `.claude/skills`, `.claude/agents`
- Tests: structure verification pending
- Dead Code: not run
- Security: not run
- Commit Status: not committed
