# Current Goal

## 状态

**无活跃目标** ✅

G23 (语义信息移除) 已完成。

## 上次完成目标

**G23: 语义信息移除** ✅

移除两个靶场中的漏洞语义信息，同时更新验证脚本确保漏洞功能正常。

## Completed Work

### 主靶场 (`java-test-app/`) 变更

**重命名的控制器** (30 个):
| 原类名 | 新类名 |
|-------|--------|
| `SQLInjectionController` | `QueryController` |
| `XSSController` | `WebRenderController` |
| `CommandInjectionController` | `SystemExecController` |
| `SSRFController` | `DataFetchController` |
| `PathTraversalController` | `FileAccessController` |
| `DeserializeController` | `DataDecodeController` |
| `Log4jController` | `LogDataController` |
| `XXEController` | `XmlDataController` |
| `SpelController` | `ExprEvalController` |
| `QLExpressController` | `ScriptExprController` |
| `VelocityController` | `TplRenderController` |
| `FreeMarkerController` | `TplDataController` |
| `UrlRedirectController` | `NavController` |
| `FileUploadController` | `FileStoreController` |
| `JwtController` | `TokenAuthController` |
| `CorsCsrfController` | `BrowserSecController` |
| `CrlfInjectionController` | `HttpHeaderController` |
| `DosController` | `ResourceLimitController` |
| `ExtendDeserializeController` | `DataDecodeV2Controller` |
| `FastjsonController` | `JsonDataController` |
| `IDORController` | `ResourceAccessController` |
| `IndexController` | `HomeController` |
| `IPForgeryController` | `NetworkInfoController` |
| `JNDIController` | `RemoteDataController` |
| `LogicController` | `BusinessFlowController` |
| `OtherVulnController` | `MiscFeatureController` |
| `ShiroController` | `SessionDataController` |
| `SmugglingController` | `HttpRequestController` |
| `XPathController` | `XmlQueryController` |

**路径重映射** (主要路径):
| 原路径 | 新路径 |
|-------|--------|
| `/sqli/jdbc/vuln` | `/api/v1/query/user` |
| `/sqli/like/vuln` | `/api/v1/query/search` |
| `/xss/reflect` | `/api/v1/web/render` |
| `/cmd/ping/vuln` | `/api/v1/system/ping` |
| `/ssrf/*` | `/api/v1/data/fetch/*` |
| `/deserialize/*` | `/api/v1/data/decode/*` |
| `/spel/*` | `/api/v1/expr/*` |
| `/qlexpress/*` | `/api/v1/script/*` |

**注释处理**:
- 移除所有 Javadoc 中的漏洞描述
- 移除方法内解释漏洞的中文注释
- 移除测试 URL 注释

### 混淆靶场 (`java-test-app-obf/`) 变更

**注释清理** (18 个文件):
- 移除所有包含 "SQL", "injection", "vulnerability", "exploit" 的注释
- 移除路径映射注释
- 简化类注释，移除漏洞描述

### 验证脚本更新

所有主要验证脚本已更新路径映射。

## Next Steps

使用 `/ai-roadmap` 查看所有可用目标，或 `/ai-goal` 启动新目标。
