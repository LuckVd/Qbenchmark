# Current Goal

## Goal

**G05: Web 安全漏洞（低-中危）** - 添加 URL 重定向、文件上传、JWT、CORS/CSRF/Cookies、CRLF 注入漏洞

## Current State

设计已确认，准备开始实现。

## Confirmed Approach

**范围：** 全部 5 种 Web 安全漏洞
**依赖：** 需添加 JWT 依赖 (jjwt 0.9.1 漏洞版本)
**版本：** 仅漏洞版本（不实现安全版本）

## Acceptance Criteria

- [ ] 在 pom.xml 添加 JWT 依赖
- [ ] 创建 UrlRedirectController.java (3个端点)
- [ ] 创建 FileUploadController.java (2个端点)
- [ ] 创建 JwtController.java (2个端点)
- [ ] 创建 CorsCsrfController.java (3个端点)
- [ ] 创建 CrlfInjectionController.java (2个端点)
- [ ] 创建 validation/payloads/web_vuln_payloads.txt
- [ ] 更新 validation/quick_validate.sh 添加 Web 漏洞测试
- [ ] 编译测试通过

## Test Plan

### URL 重定向测试
1. `/urlRedirect/redirect?url=http://evil.com` - redirect 方式
2. `/urlRedirect/setHeader?url=http://evil.com` - setHeader 方式
3. `/urlRedirect/forward?path=http://evil.com` - forward 方式

### 文件上传测试
1. `/file/upload` - 上传任意文件
2. `/file/upload/picture` - 伪造成图片的 JSP Webshell

### JWT 漏洞测试
1. `/jwt/generate?username=admin` - 生成弱签名 token
2. `/jwt/verify?token=...` - 验证可伪造的 token

### CORS/CSRF/Cookies 测试
1. `/cors/simple` - CORS 漏洞
2. `/csrf/vuln` - CSRF 漏洞（无 token）
3. `/cookies/steal` - Cookie 敏感信息

### CRLF 注入测试
1. `/crlf/injection?name=admin\\r\\nSet-Cookie:admin=true` - CRLF 注入

## Implementation Plan

### Task 1: 添加 JWT 依赖

文件路径: `java-vuln-lab/pom.xml`

添加依赖:
```xml
<!-- JWT 0.9.1 (漏洞版本) -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt</artifactId>
    <version>0.9.1</version>
</dependency>
```

### Task 2: 创建 UrlRedirectController.java

端点:
- `/urlRedirect/redirect` - 使用 response.sendRedirect()
- `/urlRedirect/setHeader` - 使用 response.setHeader("Location", ...)
- `/urlRedirect/forward` - 使用 request.getRequestDispatch().forward()

### Task 3: 创建 FileUploadController.java

端点:
- `/file/upload` - 上传任意文件到 /tmp
- `/file/upload/picture` - 仅检查扩展名，可上传 JSP Webshell

### Task 4: 创建 JwtController.java

端点:
- `/jwt/generate` - 使用弱密钥生成 JWT
- `/jwt/verify` - 验证 JWT（可伪造）

### Task 5: 创建 CorsCsrfController.java

端点:
- `/cors/simple` - 设置 Access-Control-Allow-Origin: *
- `/csrf/vuln` - 无 CSRF token 的敏感操作
- `/cookies/steal` - 在 Cookie 中存储敏感信息

### Task 6: 创建 CrlfInjectionController.java

端点:
- `/crlf/injection` - 将用户输入直接写入 HTTP 响应头

### Task 7: 创建验证 Payload 文件

文件路径: `validation/payloads/web_vuln_payloads.txt`

### Task 8: 更新验证脚本

在 `validation/quick_validate.sh` 添加 Web 漏洞测试

### Task 9: 编译测试

- `mvn clean compile`

## Blockers

- 无

## Open Questions

- 无

## Sync Notes

- 2026-03-31: G04 同步完成，启动 G05 设计
- 2026-03-31: 确认完整实现全部 5 种 Web 安全漏洞
