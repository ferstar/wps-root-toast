# WPS Root 提示隐藏

一个轻量的 LSPosed/Xposed 模块，只隐藏 WPS Office 启动时的这条简体中文提示：

> 设备已Root，使用WPS Office可能存在安全风险。

其他 Toast（例如保存、复制、文件打开失败等提示）正常放行。模块不修改 WPS 的 root 检测结果、不禁用应用组件、不修改 WPS APK，也不授予 WPS root 权限。

## 安装

1. 从 [Releases](https://github.com/ferstar/wps-root-toast/releases) 下载并安装 APK。
2. 在 LSPosed 中启用 **WPS Root 提示隐藏**。
3. 作用域只勾选 **WPS Office**（`cn.wps.moffice_eng`），不需要勾选系统框架。
4. 强制停止 WPS 后重新打开，无需重启手机。

模块没有独立的桌面入口或设置页面。关闭模块并重启 WPS，即可恢复提示。卸载模块同样可以恢复。

如果此前安装了本地试用版 `local.wps.roottoast` 或 ToastBlocker，请先关闭或卸载它们，避免重复 Hook。正式版包名是 `io.github.ferstar.wpsroottoast`。

## 支持范围

实机验证环境：

| 项目 | 已验证版本 |
| --- | --- |
| Android | 16 |
| WPS Office | 26.9.1（1991） |
| LSPosed | 2.2.1（7912），传统 Xposed API 兼容层 |

APK 的最低 Android 版本为 8.0（API 26），但其他 Android/WPS 组合尚未做实机验证。仅匹配上述简体中文文案；WPS 改变文案、语言或提示实现后，可能需要更新模块。WPS 定制版、极速版及其他包名不在作用域内。

## 原理与隐私

模块仅在 `cn.wps.moffice_eng` 进程中 Hook `Toast.show()`，读取 Toast 的 `mText`，忽略排版空白后对整句提示做精确匹配。只有匹配这句 root 提醒时才跳过显示。无法读取文本或 Hook 失败时，保留应用原有行为。

不申请权限，不联网，不收集文档、账号或设备信息，不记录 Toast 文本。LSPosed 日志仅记录模块加载和命中拦截的状态。隐藏提示不改变设备实际的 root 状态或安全风险。

## 构建

需要 JDK 17 和 Android SDK（平台 36）。

```sh
./gradlew :app:testDebugUnitTest :app:lintRelease :app:assembleDebug
```

构建 release APK：

```sh
./gradlew :app:assembleRelease
```

未配置签名时输出 unsigned APK。为 release 配置你自己的 PKCS12/JKS 密钥，别名为 `release`，并设置环境变量 `ANDROID_KEYSTORE_PATH` 与 `ANDROID_KEYSTORE_PASSWORD`。发布签名密钥不在本仓库中。

传统 Xposed API 82 仅作编译依赖，不随 APK 分发；因原 API 仓库不可用，构建使用限制到 `de.robv.android.xposed` 的 Maven 镜像。除此之外的依赖来自 Google Maven 和 Maven Central。

GitHub Actions 对提交执行单元测试、lint 和 debug 构建。维护者可运行 **Build signed release**，使用仓库 Secrets 中的 `ANDROID_KEYSTORE_BASE64` 和 `ANDROID_KEYSTORE_PASSWORD` 构建正式签名 APK。

## 反馈

请在 [Issues](https://github.com/ferstar/wps-root-toast/issues) 中提供 Android、WPS、LSPosed 版本，以及相关的模块日志。截图请遮挡账号和文档名称。

本项目与 WPS/金山办公没有关联，不包含 WPS 的代码或商标图标。

## License

MIT。
