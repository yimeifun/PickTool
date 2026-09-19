# Pick Tool（取件助手）

> **一键打开你的全部取件码。** 面向菜鸟 / 淘宝 / 拼多多 / 京东 / 小红书的 Android 取件快捷工具。
>
> 一键直达取件码 / 身份码页面。零广告、零后台。取件，就一步。

---

## ✨ 功能特性

| | |
|---|---|
| 🚀 **一键直达** | 点击即可打开目标 App 的取件码 / 身份码页面 |
| 📦 **7 个入口** | 菜鸟、淘宝取件、淘宝待取、拼多多取件、拼多多待取、京东、小红书 |
| 🔗 **网页兜底** | 目标 App 未安装时，自动打开对应网页版 |
| 📌 **桌面快捷方式** | 长按应用图标 → 4 个固定快捷方式（拼多多/淘宝 × 身份码/取件）|
| 🧩 **桌面小组件** | 8 槽位可缩放小组件，自由勾选要显示的入口（含实时计数选择器）|
| 🔄 **应用内更新** | 启动时静默检查 Gitee Releases；有新版本弹窗 → 应用内下载（圆形进度环）→ 一键安装 |
| 📅 **每日一言** | 每日从 hitokoto.cn 拉取一言（离线时使用内置文案）|
| 🎨 **主题** | 亮色 / 暗色 / 跟随系统，玻璃拟态设计，超圆角 |
| 🔒 **隐私** | 不读取短信 / 通知 / 照片 / 账号；仅使用 INTERNET 权限（更新检测 + 每日一言）|

## 🔄 更新机制说明

- App 启动后 1.5 秒，**静默**请求 `gitee.com/yimei-fun/picktool/releases` 检查最新版本。
- **无新版本** → 不做任何事（无 Toast、无弹窗）。
- **发现新版本** → 弹出圆角卡片（应用图标 + 版本号 + 更新日志 + 立即下载）。
- 点击「立即下载」→ **圆形渐变进度环**（实时百分比 / 大小 / 速度）→ 完成后按钮变为「立即安装」→ 调起系统安装器（内置 FileProvider，无 AndroidX 依赖）。
- 所有更新弹窗与 App 整体玻璃拟态风格一致（24 dp 圆角、品牌渐变）。

## 📸 截图

| 主界面 | 关于软件 | 更新弹窗 | 小组件配置 |
|---|---|---|---|
| ![main](docs/screenshots/01-main.png) | ![about](docs/screenshots/02-about.png) | ![update](docs/screenshots/03-update-dialog.png) | ![widget](docs/screenshots/04-widget-config.png) |

## 🛠️ 构建

**环境要求：** JDK 11+（javac source 11）、Android SDK 35、build-tools 36.0.0+

### 手动管道构建（无需 Gradle 依赖）
```powershell
# aapt2 compile+link → javac → d8 → zipalign → apksigner
python build_scripts/build_manual.py
```
APK 输出：`app/build/outputs/apk/debug/app-debug-phone.apk`
（同时复制到 `dist/`，文件名带版本标识。）

### 安装到设备
```powershell
adb install -r -t app/build/outputs/apk/debug/app-debug-phone.apk
```

### 发版 / Gitee 上传清单
1. 构建 APK，重命名清晰：`PickTool-v<版本>-<大小>B-<sha1前12位>.apk`
2. 在 Gitee 创建 Release，**版本 tag 与 APK versionName 保持一致**（如 `1.0.1`），并上传 APK 附件。
3. App 按语义化版本比较 `tag_name` 与本地 `versionName` —— 两者必须同步：
   - `build_scripts/build_manual.py` → `--version-name 1.0.0`
   - Gitee Release tag → `1.0.0` / `1.0.1` ……
4. 确认附件：Release 附件中**第一个 `.apk` 文件**就是应用内下载器获取的目标。

⚠️ **切勿提交 `keystore.properties`、`*.jks`、`*.keystore`** —— 已在 `.gitignore` 中忽略（参考 `keystore.properties.example`）。

## 🧩 小组件说明

- 8 槽位、2 行布局，图标**自适应大小**（weight 填充，上限 48 dp）
- 紧凑模式：小组件拖到最小时隐藏入口名称，只显示图标
- `resizeMode="horizontal|vertical"` + `minResizeWidth/minResizeHeight`，让 MIUI/HyperOS 显示缩放手柄
- **刻意不设 `targetCellWidth/Height`**：MIUI 会当作固定网格导致无法手动缩放

## 🔐 隐私承诺

- **不读取**短信 / 通讯录 / 照片 / 账号
- **唯一 INTERNET 权限**：按需更新检测 + 每日一言
- **无**统计、广告、后台服务
- **纯本地运行**：你的数据不会离开手机

## 🧩 兼容性

- minSdk **23**（Android 6.0 Marshmallow）
- targetSdk **35**（Android 15）
- 已在小米 MIUI / HyperOS 桌面实测（Redmi K70 Pro）

## ⚠️ 链接维护说明

本项目用到的第三方应用内链接**并非稳定公开 API**。每次发版前，请在装有最新目标 App 的真机上逐条验证所有快捷方式。

## 📜 开源许可

[MIT](./LICENSE)

## 👤 作者

**亿槑** · 邮箱 `yimei@ymei.top` · QQ `3629424534`

---

<p align="center">
  <sub>为所有在 5 个 App 之间翻找取件码的人而做。</sub>
</p>