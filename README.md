# 应用跳转白名单

ColorOS / OxygenOS 上按 **调用方包名** 跳过应用间跳转确认（如「允许 30 天」）的 LSPosed 模块。

- 包名：`com.nazze.oplusjumpallowlist`
- 版本：`0.1.0`
- 勾选名单里的 App 作为 caller 启动其它应用时不弹确认；未勾选走系统原逻辑
- 默认空名单；改名单热生效（读配置失败则不豁免）
- 启停交给 LSPosed；应用内只有名单设置

## 环境

- 已 Root + LSPosed
- 机型验证：OnePlus PKX110 / ColorOS 16（`PKX110_16.0.1.301(CN01)`）

## 构建

```bash
cd /home/nazze/Studio/myrepo/oplus-home-jump-exempt
JAVA_HOME=/usr/lib/jvm/java-17-openjdk ./gradlew --no-daemon :app:assembleDebug
```

产物：`app/build/outputs/apk/debug/app-debug.apk`

## 安装

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

1. LSPosed → 启用「应用跳转白名单」
2. 作用域只勾 **`system`**（`system_server`）
3. 首次启用 / 更新模块后按 LSPosed 提示重启
4. 打开模块 → 搜索并勾选要豁免的调用方（如 Niagara / `bitpit.launcher`）

不要把 `com.oplus.securitypermission` 加进作用域；决策点在 `system_server`。

## 验证

| 步骤 | 预期 |
| --- | --- |
| 勾选 Niagara，从桌面打开会弹窗的 App | 无跳转确认 |
| 取消勾选后再打开 | 恢复系统弹窗（改名单无需重启） |
| LSPosed 关闭模块 | 完全恢复系统行为 |

## 排障

| 现象 | 处理 |
| --- | --- |
| 完全无效 | 模块已启用、作用域含 `system`、装包后已按提示重启 |
| 仍弹窗 | 勾的是 **调用方** 包名，不是目标 App |
| Hook 未加载 | LSPosed 日志应有 `OplusJumpAllowlist` / `checkAllowStartActivity` |

## 不做

自动跟默认桌面、caller→callee 配对、恢复「永久允许」UI、其它 ColorOS 桌面/手势问题。

## 技术摘要

Hook：`com.android.server.am.OplusSecurityPermissionManager.checkAllowStartActivity`  
命中白名单时返回 `-1`，跳过确认框。
