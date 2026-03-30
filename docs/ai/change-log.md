# Change Log

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
