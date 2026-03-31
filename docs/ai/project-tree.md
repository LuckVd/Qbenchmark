# Project Tree

保持此文件简洁。只包含重要路径。

```text
Qbenchmark/
├── java-vuln-lab/                    # Spring Boot 靶场应用
│   ├── src/main/java/com/vulnlab/
│   │   ├── VulnLabApplication.java            # 应用入口
│   │   ├── controller/                         # 漏洞控制器 (17个)
│   │   │   ├── IndexController.java
│   │   │   ├── SQLInjectionController.java
│   │   │   ├── SSRFController.java
│   │   │   ├── CommandInjectionController.java
│   │   │   ├── XSSController.java
│   │   │   ├── Log4jController.java
│   │   │   ├── PathTraversalController.java
│   │   │   ├── DeserializeController.java      # G01: Jackson, Cookie 反序列化
│   │   │   ├── FastjsonController.java         # G01: Fastjson 反序列化
│   │   │   ├── ShiroController.java            # G01: Shiro 反序列化
│   │   │   ├── XXEController.java              # G02: XMLReader, SAXBuilder, DocumentBuilder
│   │   │   ├── SpelController.java             # G03: SpEL 表达式注入
│   │   │   ├── QLExpressController.java        # G03: QLExpress 表达式注入
│   │   │   ├── VelocityController.java         # G04: Velocity SSTI
│   │   │   ├── FreeMarkerController.java       # G04: FreeMarker SSTI
│   │   │   ├── UrlRedirectController.java      # G05: URL 重定向
│   │   │   ├── FileUploadController.java       # G05: 文件上传
│   │   │   ├── JwtController.java              # G05: JWT 漏洞
│   │   │   ├── CorsCsrfController.java         # G05: CORS, CSRF, Cookies
│   │   │   └── CrlfInjectionController.java    # G05: CRLF 注入
│   │   └── util/                               # 工具类
│   │       ├── CommandUtil.java
│   │       ├── PathUtil.java
│   │       └── HttpUtil.java
│   ├── pom.xml                                 # Maven 配置
│   └── target/                                 # 编译输出
├── validation/                    # 验证测试套件
│   ├── payloads/                               # 漏洞 Payload (11个文件)
│   │   ├── sqli_payloads.txt
│   │   ├── ssrf_payloads.txt
│   │   ├── cmd_payloads.txt
│   │   ├── xss_payloads.txt
│   │   ├── log4shell_payloads.txt
│   │   ├── path_traversal_payloads.txt
│   │   ├── deserialize_payloads.txt
│   │   ├── xxe_payloads.txt
│   │   ├── expression_payloads.txt
│   │   ├── ssti_payloads.txt
│   │   └── web_vuln_payloads.txt
│   ├── scripts/                                # 验证脚本和工具
│   │   ├── init_db.sql
│   │   ├── quick_validate.sh                  # 快速验证脚本 (33项测试, 100% 通过)
│   │   ├── generate_payloads.py               # Payload 生成器
│   │   └── jndi_server.py                      # Log4Shell RCE 测试服务器
│   ├── local_validate.sh                       # 完整验证脚本
│   └── HOWTO_VALIDATE.md                       # 验证使用说明
├── docker-compose.yml                 # Docker 编排配置 (G06 新增)
└── java-vuln-lab/Dockerfile            # 应用容器镜像 (G06 新增)
├── docs/ai/                        # AI 工作流
│   ├── roadmap.md                              # 项目路线图
│   ├── current-goal.md                         # 当前目标 (空闲)
│   ├── current-goal.state.yaml                 # 目标状态
│   ├── project-summary.md                      # 项目摘要
│   ├── project-tree.md                         # 项目结构
│   ├── change-log.md                           # 变更日志
│   └── constraints/                            # 约束配置
├── .claude/                         # Claude Code 配置
│   ├── skills/                                 # 技能定义
│   ├── agents/                                 # Agent 配置
│   └── commands/                               # 命令定义
├── ANALYSIS.md                      # 漏洞对比分析
└── README.md                        # 项目说明
```

## Key Entry Points

- 应用入口: `java-vuln-lab/src/main/java/com/vulnlab/VulnLabApplication.java`
- 启动命令: `cd java-vuln-lab && mvn spring-boot:run`
- 验证测试: `bash validation/quick_validate.sh`

## Key Config Files

- Maven 配置: `java-vuln-lab/pom.xml`
- Claude 设置: `.claude/settings.local.json`
- 工作流状态: `docs/ai/current-goal.state.yaml`

## 项目统计

- 控制器数量: 17
- 漏洞端点: 50+
- Payload 文件: 10
- 已实现漏洞类型: 15+
- 提交记录: 5+
