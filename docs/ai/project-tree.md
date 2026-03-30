# Project Tree

保持此文件简洁。只包含重要路径。

```text
Qbenchmark/
├── java-vuln-lab/           # Spring Boot 靶场应用
│   ├── src/main/java/com/vulnlab/
│   │   ├── VulnLabApplication.java       # 应用入口
│   │   ├── controller/                    # 漏洞控制器
│   │   │   ├── SQLInjectionController.java
│   │   │   ├── SSRFController.java
│   │   │   ├── CommandInjectionController.java
│   │   │   ├── XSSController.java
│   │   │   ├── Log4jController.java
│   │   │   └── PathTraversalController.java
│   │   └── util/                          # 工具类
│   │       ├── CommandUtil.java
│   │       ├── PathUtil.java
│   │       └── HttpUtil.java
│   ├── pom.xml                            # Maven 配置
│   └── target/                            # 编译输出
├── validation/                 # 验证测试套件
│   ├── payloads/                        # 漏洞 Payload
│   │   ├── sqli_payloads.txt
│   │   ├── ssrf_payloads.txt
│   │   ├── cmd_payloads.txt
│   │   ├── xss_payloads.txt
│   │   ├── log4shell_payloads.txt
│   │   └── path_traversal_payloads.txt
│   ├── expected/                        # 预期结果
│   ├── scripts/                         # 测试脚本
│   └── quick_validate.sh                # 快速验证脚本
├── docs/ai/                    # AI 工作流
│   ├── roadmap.md                       # 项目路线图
│   ├── current-goal.md                  # 当前目标
│   ├── current-goal.state.yaml          # 目标状态
│   ├── project-summary.md               # 项目摘要
│   ├── project-tree.md                  # 项目结构
│   ├── change-log.md                    # 变更日志
│   └── constraints/                     # 约束配置
├── .claude/                    # Claude Code 配置
│   ├── skills/                          # 技能定义
│   ├── agents/                          # Agent 配置
│   └── commands/                        # 命令定义
├── ANALYSIS.md                 # 漏洞对比分析
└── README.md                   # 项目说明
```

## Key Entry Points

- 应用入口: `java-vuln-lab/src/main/java/com/vulnlab/VulnLabApplication.java`
- 启动命令: `cd java-vuln-lab && mvn spring-boot:run`
- 验证测试: `bash validation/quick_validate.sh`

## Key Config Files

- Maven 配置: `java-vuln-lab/pom.xml`
- Claude 设置: `.claude/settings.local.json`
- 工作流状态: `docs/ai/current-goal.state.yaml`

## 参考

- java-sec-code 位置: `/opt/target/java-sec-code`
