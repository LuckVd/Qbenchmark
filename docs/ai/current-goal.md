# Current Goal

## Goal

**G02: XXE 漏洞（高危）** - 添加 XMLReader、SAXBuilder、DocumentBuilder XXE 漏洞

## Current State

实现已完成，测试通过。

## Confirmed Approach

**范围：** 全部 3 种 XXE 漏洞
**依赖：** 使用现有依赖（dom4j 2.0.0、jdom2 2.0.6）
**版本：** 仅漏洞版本（不实现安全版本）

## Acceptance Criteria

- [x] 创建 XXEController.java
- [x] 实现 3 个 XXE 端点
- [x] 创建 validation/payloads/xxe_payloads.txt
- [x] 更新 validation/quick_validate.sh 添加 XXE 测试
- [x] 所有端点可访问并响应预期结果

## Test Plan

- XMLReader XXE 读取 /etc/passwd ✅
- SAXBuilder XXE 读取 /etc/passwd ✅ (成功泄露文件内容)
- DocumentBuilder XXE 读取 /etc/passwd ✅ (成功泄露文件内容)

## Implementation Plan

### Task 1: 创建 XXEController.java

文件路径: java-vuln-lab/src/main/java/com/vulnlab/controller/XXEController.java

端点列表:
- `/xxe/xmlReader/vuln` - XMLReader XXE
- `/xxe/saxBuilder/vuln` - SAXBuilder XXE (JDOM2)
- `/xxe/documentBuilder/vuln` - DocumentBuilder XXE

攻击向量:
- 文件读取: <!ENTITY xxe SYSTEM "file:///etc/passwd">
- SSRF: <!ENTITY xxe SYSTEM "http://127.0.0.1:8080">

### Task 2: 创建验证 Payload 文件

文件路径: validation/payloads/xxe_payloads.txt

包含 3 种解析器的 XXE payload 示例

### Task 3: 更新验证脚本

在 validation/quick_validate.sh 添加 XXE 测试

### Task 4: 编译测试

- mvn clean compile ✅
- 启动应用验证端点可访问 ✅

## Blockers

- 无

## Open Questions

- 无

## Sync Notes

- 2026-03-30: 设计已确认，开始实现
- 2026-03-30: 实现完成，3 个端点测试通过
