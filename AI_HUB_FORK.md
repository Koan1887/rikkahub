# AI Hub fork 说明

这个工作目录是 [Koan1887/rikkahub](https://github.com/Koan1887/rikkahub) 的本地检出，基于上游提交 `eebdea0bd5c83e3c1bd66c526ac2cbc43ba59811`。上游项目及本 fork 继续遵守根目录的 [AGPL-3.0 许可证](LICENSE)。保留原作者版权声明，不把 fork 误称为上游官方版本。

## 本轮调整

- 以原生 Kotlin/Jetpack Compose、Room、DataStore 与上游流式聊天实现作为以后修改的基础。
- 将应用显示名称改为「小黄瓜 AI Hub」，绘制新的对话图标，移除 APK 中上游启动图的 PNG。
- 使用 `dev.koan.aihub.native.debug` 作为 debug 应用 ID，使它与旧 `dev.koan.aihub` 并存。旧版加密数据暂不覆盖；迁移完成后再讨论是否合并应用 ID。
- 移除上游 Firebase Analytics/Crashlytics 和 Google Services 插件依赖，不使用上游的 `google-services.json`。
- 移除设置页指向上游社群和捐赠的入口，以及聊天抽屉中的上游更新卡片。关于页保留上游源码和许可证链接作归属说明。

## 旧工程和数据

旧 WebView 工程的源码快照在 `/Users/kerithea/ai-hub-webview-2026-10-08.tar.gz`，该轮 debug APK 在 `/Users/kerithea/ai-hub-webview-2026-10-08-debug.apk`。原始改动前快照在 `/Users/kerithea/ai-hub-baseline-2026-10-08.tar.gz`。不要卸载旧应用或清除它的数据：旧 `vault` / `vault_v2` 使用 Android Keystore 加密，原生 fork 还没有实现读取和迁移。

## 构建

上游当前要求 JDK 17、Gradle wrapper 9.6、Android SDK 37.2、NDK 28.2.13676358 和 `material3/material-color-utilities` 子模块。本 fork 已移除对 `google-services.json` 的要求。

```sh
git submodule update --init --depth 1
./gradlew :app:assembleDebug
```

首次构建会下载大量依赖。2026-10-08 已执行 `./gradlew :app:assembleDebug` 并成功构建；ARM64 APK 在 `app/build/outputs/apk/debug/app-arm64-v8a-debug.apk`，另有 x86_64 和 universal 版本。ARM64 APK 已通过 `apksigner verify` 的 v2 签名校验，并安装到 Android 模拟器。模拟器已打开聊天页、导航抽屉、设置页和服务商列表，启动期间没有 AndroidRuntime 崩溃。`./gradlew :app:testDebugUnitTest` 通过 297 个测试用例。没有配置真实 API Key，因此尚未验证真实服务聊天。

## 继续改动的顺序

先让原生聊天与服务商配置在 fork 中贯通并验证安装；然后为多成员房间加入真实作者、顺序与静音；再迁移旧加密记录和任务/计划。实现旧数据迁移之前保持两个应用 ID 并存，不通过清空数据解决升级。
