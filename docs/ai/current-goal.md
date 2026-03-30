# Current Goal

## Goal

**G01: 反序列化漏洞（高危）** - 添加 Jackson、Fastjson、Shiro、Cookie 反序列化漏洞

## Current State

设计已确认，开始实现阶段。

## Confirmed Approach

**范围：** 全部 4 种反序列化漏洞
**依赖：** 完整依赖（Commons Collections、Fastjson、Shiro）
**版本：** 仅漏洞版本（不实现安全版本）

## Acceptance Criteria

- [x] pom.xml 包含所有必需依赖
- [x] DeserializeController.java 创建成功
- [x] FastjsonController.java 创建成功
- [x] ShiroController.java 创建成功
- [x] 4 个端点均可访问（返回预期响应）
- [x] 验证 Payload 文件已创建
- [x] 验证脚本已更新

## Test Plan

- 使用 ysoserial 生成 payload 测试 Cookie 反序列化
- 使用 JSON payload 测试 Jackson 反序列化
- 使用 @type payload 测试 Fastjson 反序列化
- 使用 Shiro 默认密钥测试 rememberMe 反序列化

## Implementation Plan

### Task 1: 更新 pom.xml 添加依赖

依赖列表：
- fastjson 1.2.24
- shiro-core 1.2.4
- commons-collections 3.1
- dom4j 2.0.0

### Task 2: 创建 DeserializeController.java

端点：
- `/deserialize/jackson` - Jackson enableDefaultTyping 反序列化
- `/deserialize/rememberMe` - Cookie 反序列化

### Task 3: 创建 FastjsonController.java

端点：
- `/fastjson/deserialize` - Fastjson autoType 反序列化

### Task 4: 创建 ShiroController.java

端点：
- `/shiro/deserialize` - Shiro rememberMe 反序列化

### Task 5: 创建验证 Payload 文件

- validation/payloads/deserialize_payloads.txt

### Task 6: 更新验证脚本

- 在 quick_validate.sh 添加反序列化测试

### Task 7: 编译测试

- mvn clean compile
- 启动应用验证端点可访问

## Blockers

- 无

## Open Questions

- 无

## Sync Notes

- 2026-03-30: 设计已确认，开始实现
