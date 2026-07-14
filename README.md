# CorePlan

> 免登录免费的健身动作指导 Android App。提供 1,324 个动作 GIF 演示、DIY 训练计划、饮水提醒。

[📥 下载最新 APK](https://github.com/63894696/coreplan-android/releases) · [🐛 反馈问题](https://github.com/63894696/coreplan-android/issues)

---

## ✨ 主要功能

- 🏋️ **1,324 个健身动作** — 含 GIF 演示 + 步骤说明 + 目标肌群
- 🌍 **9 种语言** — 中文（默认）/ English / Español / Italiano / हिन्दी / 한국어 / Polski / Русский / Türkçe
- 🗓️ **DIY 训练计划** — 自建月度/季度计划，组数/次数/休息时间都可调
- 💧 **饮水提醒** — 系统闹钟 + 倒计时 + 次数，简洁无开关
- ⭐ **3 个内置方案** — 轻度日 / 中度日 / 强度减肥日
- 🔒 **完全本地** — 无登录、无数据收集、无广告

## 📸 截图

(在 assets/screenshots/ 下)

## 📥 安装

下载最新 `CorePlan.apk`（约 22 MB）→ 在手机"文件管理"中点击 → 允许"未知来源安装" → 完成。

最低系统要求：Android 7.0 (API 24)

## 🛠 技术栈

- **Kotlin 1.9** + **Jetpack Compose** + **Material 3**
- **Hilt** 依赖注入
- **Room** 数据库（仅本地存储）
- **Coroutines** + **StateFlow**
- **Coil** GIF 加载（从 CDN 流式获取）
- **AlarmManager** + **Notification** 饮水提醒

## 🏗 项目结构

```
app/
├── src/main/
│   ├── java/com/example/fitness/
│   │   ├── data/         # Room + 业务逻辑
│   │   ├── ui/           # Compose 屏幕
│   │   │   ├── home/     # 首页（方案 + 饮水）
│   │   │   ├── browse/   # 动作浏览
│   │   │   ├── search/   # 搜索
│   │   │   ├── favorites/ # 我的方案
│   │   │   ├── plans/    # 方案详情/编辑/新建
│   │   │   ├── exercise/ # 动作详情
│   │   │   ├── settings/ # 关于/赞助
│   │   │   ├── components/
│   │   │   └── theme/    # 颜色/排版
│   │   ├── notifications/ # 饮水提醒 Receiver
│   │   └── i18n/         # 多语言
│   ├── res/raw/exercises.json
│   ├── res/values-zh/strings.xml
│   └── assets/exercise_names_zh.json
└── build.gradle.kts
```

## 📚 数据来源

- 动作 GIF 数据：[hasaneyldrm/exercises-dataset](https://github.com/hasaneyldrm/exercises-dataset) （1,324 动作，9 语言，多谢作者开源）
- GIF 媒体 URL：`https://static.exercisedb.dev/media/{media_id}.gif`
- 饮水量推荐：US National Academies of Sciences, Engineering, and Medicine 2004 报告
  - 男：3.7 L / 天（含食物水约 20%，纯饮水 2.5-3.0 L）
  - 女：2.7 L / 天（纯饮水 2.0-2.5 L）
  - 本应用默认 2.5 L / 天（即 2500 ml）

## ⚠️ 免责声明

本应用仅供参考，**不能替代专业医疗或健身建议**。
- 每个人的身体状况不同，**开始任何训练前请先咨询专业医生或认证健身教练**
- 所有训练动作请在专业指导下进行，循序渐进，避免受伤
- 使用本应用产生的任何后果由使用者本人承担

## 👥 开发者

- **Jack Li** — 需求、产品、测试
- **Claude** （[Anthropic](https://www.anthropic.com)） — 代码实现

## ☕ 支持

CorePlan 完全免费、无广告。如果你觉得有用，可以请开发者喝杯咖啡：

| 微信 | 支付宝 | PayPal |
|:---:|:---:|:---:|
| 在 App 内"设置 → 关于 → 微信赞赏码"扫码 | 在 App 内"设置 → 关于 → 支付宝收款码"扫码 | https://paypal.me/JackLi5673 |

## 📜 许可证

[MIT License](LICENSE) — 你可以自由使用、修改、分发，但需保留版权声明。

---

Made with ❤️ by Jack Li & Claude
