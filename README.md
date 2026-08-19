# 应用跳转白名单

在 ColorOS / OxygenOS 上，按调用方跳过「应用间跳转」确认框（例如「允许 30 天」）的 LSPosed 模块。

适合第三方桌面（如 Niagara）或其它你信任的 App：勾进白名单后，由它打开其它应用时不再弹确认；没勾选的 App 仍走系统拦截。

## 功能

- 在模块设置里勾选 **调用方**（谁发起跳转）
- 白名单命中 → 不弹确认
- 未命中 / 名单为空 → 系统原样
- 改名单一般立即生效，不必为改名单重启
- 模块开关在 LSPosed 里；本应用不做总开关

## 要求

- 已 Root，并安装 LSPosed（Zygisk / Riru）
- 系统带有 ColorOS / OxygenOS 的应用跳转拦截

已在 OnePlus（ColorOS 16）上验证。其它机型/版本不保证，OTA 后钩子可能失效。

## 安装

1. 安装模块 APK  
2. 打开 LSPosed → 模块 → 启用 **应用跳转白名单**  
3. 作用域只勾选 **`system`**  
4. 按 LSPosed 提示重启  
5. 打开本应用，搜索并勾选要豁免的调用方（例如 Niagara）

不要把安全中心 / `com.oplus.securitypermission` 加进作用域。

仅更新设置界面时重装 APK 即可；若改了 Hook 相关逻辑，仍需按 LSPosed 要求重启后再生效。

## 使用示例

1. 勾选 Niagara  
2. 从 Niagara 打开平时会弹窗的应用 → 应不再确认  
3. 取消勾选 → 弹窗应回来  

勾错对象是最常见问题：要勾 **发起跳转的 App**，不是被打开的目标 App。

## 从源码构建

需要 JDK 17 与 Android SDK。

```bash
./gradlew :app:assembleDebug
```

APK：`app/build/outputs/apk/debug/app-debug.apk`

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## 排障

| 现象 | 可检查项 |
| --- | --- |
| 完全没效果 | 模块是否启用；作用域是否含 `system`；启用/更新后是否已重启 |
| 还在弹窗 | 白名单是否勾了对的调用方包名 |
| 怀疑 Hook 没挂上 | 看 LSPosed 日志里是否出现本模块及 `checkAllowStartActivity` |

## 不会做的事

- 自动跟随默认桌面  
- 按「A → B」配对放行  
- 恢复系统「永久允许」对话框  
- 修复桌面动画、手势等其它问题  

## 包名

`com.nazze.oplusjumpallowlist`

## License

MIT © nazze (nazze@qq.com). 见 [LICENSE](LICENSE)。
