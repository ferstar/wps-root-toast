# WPS Root 提示隐藏

## 项目状态：已归档

本项目停止维护，源码和已发布 APK 保留供参考。对于下面实测的 WPS Root 提示，推荐使用 **隐藏应用列表（Hide My Applist，HMA）** 隐藏 Root 管理器包；本模块保留为只屏蔽提示的历史实现。

## 新方案：向 WPS 隐藏 Root 管理器

完整排查过程见博客：[你关的是开关，它开的是名单：WPS 是怎么查到我 Root 的](https://blog.ferstar.org/posts/how-to-avoid-wps-root-detection/)。文章记录了 Toast 模块、六项检测链、本机命中项以及 HMA 方案的验证过程。

在 Android 16、WPS Office 26.9.1（1991）、KernelSU 的实机上，进一步排查发现：WPS 的 Root 判断包含多条路径，本机命中的是通过 PackageManager 查询 `me.weishu.kernelsu`（KernelSU 管理器），而不是 PATH 中的 `su`、相关系统属性或 `test-keys`。

WPS 在 Manifest 的 `<queries>` 中声明了相关包，因此即使没有授予“读取应用列表”权限，仍可查询这些指定包。仅拒绝应用列表权限不足以阻断本机这条判断路径。

实测配置（HMA 3.8.3，LSPosed 2.2.1）：

1. 从 [HMA 模块发布页](https://github.com/Xposed-Modules-Repo/com.tsng.hidemyapplist) 安装隐藏应用列表，并按其说明在 LSPosed 中启用；本机作用域仅勾选系统框架。
2. 在 HMA 创建黑名单模板，将 `me.weishu.kernelsu` 加入隐藏列表。使用其他 Root 管理器时应选择实际安装的包；本项目未验证其他管理器的效果。
3. 将模板仅应用于 WPS Office（`cn.wps.moffice_eng`）。本机配置启用“排除系统应用”，关闭“增强过滤”和详细日志。
4. 关闭本项目的 **WPS Root 提示隐藏** 模块，按 HMA 的要求使配置生效，再强制停止并重新打开 WPS。

本机在关闭 Toast 屏蔽模块后，Root 提示仍未出现。这说明隐藏管理器包已经阻断本机命中的检测路径，不需要再屏蔽提示。无需修改 WPS APK 或其 Manifest。

此结果仅适用于上述已验证环境：隐藏包不会改变设备实际的 Root 状态，也不保证绕过 WPS 的其他检测路径；不同版本或设备需要重新验证。HMA 的使用问题请查阅其项目文档，本仓库归档后不再接受新问题。

## 历史模块说明

一个使用 **libxposed Modern API 102.0.0** 的轻量模块，只隐藏 WPS Office 启动时的这条简体中文提示：

> 设备已Root，使用WPS Office可能存在安全风险。

其他 Toast（例如保存、复制、文件打开失败等提示）正常放行。模块不修改 WPS 的 root 检测结果、不禁用应用组件、不修改 WPS APK，也不授予 WPS root 权限。

## 安装

1. 从 [Releases](https://github.com/ferstar/wps-root-toast/releases) 下载并安装 APK。
2. 在支持 **libxposed API 102** 的 LSPosed 中启用 **WPS Root 提示隐藏**。
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
| LSPosed | 2.2.1（7912），libxposed API 102 |

APK 的最低 Android 版本为 8.0（API 26），但其他 Android/WPS 组合尚未做实机验证。**需要支持 libxposed API 102 的框架，不支持 legacy Xposed API 或 API 100/101。** 仅匹配上述简体中文文案；WPS 改变文案、语言或提示实现后，可能需要更新模块。WPS 定制版、极速版及其他包名不在作用域内。

## 原理与隐私

模块使用 `XposedModule`、modern 入口文件和 interceptor chain API，仅在 `cn.wps.moffice_eng` 中拦截公开的 `Toast.makeText/setText/show` 方法。创建或更新 Toast 时，忽略排版空白后对整句提示做精确匹配，用弱引用集合标记对应的 Toast；只有被标记对象的 `show()` 被跳过，其他调用通过 `chain.proceed()` 放行。更新为普通文字或自定义视图时移除标记。模块不读取隐藏的 `Toast.mText` 字段、不保留提示文本，Hook 失败时恢复应用原有行为。API 102 的稳定 hook id 支持框架热重载时原子替换此模块的旧 hook。

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

libxposed API 102.0.0 仅作编译依赖，不随 APK 分发。依赖来自 Google Maven 和 Maven Central，不需要第三方 Maven 镜像。

GitHub Actions 对提交执行单元测试、lint 和 debug 构建。维护者可运行 **Build signed release**，使用仓库 Secrets 中的 `ANDROID_KEYSTORE_BASE64` 和 `ANDROID_KEYSTORE_PASSWORD` 构建正式签名 APK。

## 反馈

本仓库已归档，不再接受新 Issue 或提供版本适配。历史问题与实现保留供查阅。

本项目与 WPS/金山办公没有关联，不包含 WPS 的代码或商标图标。

## License

MIT。
