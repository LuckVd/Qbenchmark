# Current Goal

## Goal

**G15: Logic Flaw（业务逻辑漏洞）**

添加业务逻辑漏洞，如支付逻辑、验证码绕过等。

## Background

业务逻辑漏洞是由于应用程序业务流程设计缺陷导致的安全问题。

- **攻击向量**: 金额篡改、验证码复用、支付绕过
- **危害**: 经济损失、权限提升
- **常见类型**: 支付逻辑、优惠券滥用、验证码绕过

## Sub-Goals

| 子目标 | 描述 | 状态 |
|--------|------|------|
| G15-S01 | 支付逻辑漏洞 | 实现 `/logic/payment` 端点 | pending |
| G15-S02 | 验证码绕过 | 实现 `/logic/captcha` 端点 | pending |

## Implementation Plan

### G15-S01: 支付逻辑漏洞

1. 创建 `LogicController.java`
2. 实现金额篡改端点
3. 实现支付绕过端点
4. 添加验证脚本

### G15-S02: 验证码绕过

1. 扩展 `LogicController.java`
2. 实现验证码复用/可预测端点
3. 添加验证脚本

## Dependencies

- 无特殊依赖

## Acceptance Criteria

- [ ] LogicController.java 创建完成
- [ ] `/logic/payment` 端点可被利用
- [ ] `/logic/captcha` 端点可被利用
- [ ] 验证脚本测试通过
- [ ] 包含安全版本对比

## Current State

**状态**: 准备开始
**开始时间**: 2026-03-31
**预计完成**: 2026-03-31

## Completed Goals (G01-G14)

| 目标 | 名称 | 状态 | 提交 ID |
|------|------|------|---------|
| G01 | 反序列化漏洞 | ✅ 完成 | 605501a |
| G02 | XXE 漏洞 | ✅ 完成 | 4cc1104 |
| G03 | 表达式注入 | ✅ 完成 | 04ea225 |
| G04 | 模板注入 | ✅ 完成 | 6a61481 |
| G05 | Web 安全漏洞 | ✅ 完成 | 5247a5c |
| G06 | 验证测试完善 | ✅ 完成 | cf2d8af |
| G07 | 环境验证靶场 | ✅ 完成 | fe42241 |
| G08 | 扩展反序列化漏洞 | ✅ 完成 | a09de39 |
| G09 | 脚本引擎注入 | ✅ 完成 | 3742f57 |
| G10 | 其他注入类漏洞 | ✅ 完成 | 6420461 |
| G11 | JNDI 注入 | ✅ 完成 | pending |
| G12 | HTTP Smuggling | ✅ 完成 | pending |
| G13 | IDOR | ✅ 完成 | pending |
| G14 | DoS/DDoS | ✅ 完成 | pending |
