# Core Components (OIE-PCS-1.0 §1, §3)

This document is incorporated into the OI Enhancements Personal and
Commercial Source License (OIE-PCS-1.0). Modifications to the files and
directories listed below, when **Distributed** or made available as a
**Network Service** (as defined in LICENSE §1 and §3), must be made
available under the terms of OIE-PCS-1.0 per LICENSE §3.

This list is the **single source of truth** for which paths are
"Core Components" for this repository. Paths NOT listed here are not
Core Components and are not subject to the source-availability
obligation of LICENSE §3 when used outside Commercial Use.


## Core Components (modifications must be made available under OIE-PCS-1.0)

### 应用入口与运行时(Application / Activity / ViewModel)
- `app/src/main/java/com/example/fitness/FitnessApp.kt`
- `app/src/main/java/com/example/fitness/FitnessApplication.kt`
- `app/src/main/java/com/example/fitness/MainActivity.kt`
- `app/src/main/java/com/example/fitness/ui/exercise/ExerciseViewModel.kt`

### 数据层(Exercise / Plan / Repository / MediaCache)
- `app/src/main/java/com/example/fitness/data/DataTranslations.kt`
- `app/src/main/java/com/example/fitness/data/Exercise.kt`
- `app/src/main/java/com/example/fitness/data/ExerciseRepository.kt`
- `app/src/main/java/com/example/fitness/data/MediaCache.kt`
- `app/src/main/java/com/example/fitness/data/PlanRepository.kt`
- `app/src/main/java/com/example/fitness/data/WorkoutPlan.kt`
- `app/src/main/java/com/example/fitness/data/WorkoutPresets.kt`

### UI 层(Compose)
- `app/src/main/java/com/example/fitness/ui/browse/BrowseScreen.kt`
- `app/src/main/java/com/example/fitness/ui/components/SlidableRow.kt`
- `app/src/main/java/com/example/fitness/ui/exercise/ExerciseViewModel.kt`
- `app/src/main/java/com/example/fitness/ui/favorites/FavoritesScreen.kt`
- `app/src/main/java/com/example/fitness/ui/home/HomeScreen.kt`
- `app/src/main/java/com/example/fitness/ui/home/WaterRing.kt`
- `app/src/main/java/com/example/fitness/ui/onboarding/OnboardingScreen.kt`
- `app/src/main/java/com/example/fitness/ui/plans/PlanScreens.kt`
- `app/src/main/java/com/example/fitness/ui/search/SearchScreen.kt`
- `app/src/main/java/com/example/fitness/ui/settings/SettingsScreen.kt`
- `app/src/main/java/com/example/fitness/ui/theme/FitnessTheme.kt`

### 通知 / i18n
- `app/src/main/java/com/example/fitness/i18n/LocaleManager.kt`
- `app/src/main/java/com/example/fitness/notifications/WaterReminderReceiver.kt`
- `app/src/main/java/com/example/fitness/notifications/WaterReminderScheduler.kt`

### Android 清单与构建
- `app/build.gradle.kts`
- `app/proguard-rules.pro`
- `app/src/main/AndroidManifest.xml`
- `build.gradle.kts`
- `gradle.properties`
- `gradle/wrapper/gradle-wrapper.jar`
- `gradle/wrapper/gradle-wrapper.properties`
- `gradlew.bat`
- `settings.gradle.kts`

### 资源(资源 ID 与 i18n 文案,Core Component 因影响多语言行为)
- `app/src/main/res/drawable/ic_water_drop.xml`
- `app/src/main/res/mipmap-hdpi/ic_launcher.png`
- `app/src/main/res/mipmap-hdpi/ic_launcher_round.png`
- `app/src/main/res/mipmap-mdpi/ic_launcher.png`
- `app/src/main/res/mipmap-mdpi/ic_launcher_round.png`
- `app/src/main/res/mipmap-xhdpi/ic_launcher.png`
- `app/src/main/res/mipmap-xhdpi/ic_launcher_round.png`
- `app/src/main/res/mipmap-xxhdpi/ic_launcher.png`
- `app/src/main/res/mipmap-xxhdpi/ic_launcher_round.png`
- `app/src/main/res/mipmap-xxxhdpi/ic_launcher.png`
- `app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.png`
- `app/src/main/res/values-zh/strings.xml`
- `app/src/main/res/values/strings.xml`
- `app/src/main/res/values/themes.xml`


## NOT Core Components (modifications do NOT trigger LICENSE §3 obligations)

The following are NOT Core Components. Modifications to these paths
do NOT, on their own, trigger the source-availability obligation of
LICENSE §3, provided such modifications are not Distributed as part
of Commercial Use without a commercial license.

### 文档与法律文件
- `README.md`, `*.md` at any depth
- `LICENSE`, `LICENSE-APACHE`, `LICENSE-POLICY.md`
- `CORE-COMPONENTS.md`, `TRADEMARKS.md`, `COMMERCIAL-LICENSE.md`
- `CONTRIBUTING.md`, `SECURITY.md`, `THIRD-PARTY-NOTICES`
- `CHANGELOG.md`, `NOTICE`, `PRIVACY.md`

### 文档目录
- `docs/`

### 测试 / 评估
- `tests/`, `test/`, `*_test/`, `*_eval/`
- `tests/fixtures/`

### CI / 工作流 / 工具脚本(非核心业务)
- `.github/`

### 资源文件
- `icons/`, `extension/icons/`
- `assets/` (图标/UI 资源;Brand 元素使用受 TRADEMARKS.md 约束)

### 个人开发 / 实验 / 备份 / 临时
- `_*.py`, `_*.png`, `_*.log`, `_*.db`
- `backup-*/`, `*.bak`, `*.tmp`

### 构建产物与本地运行时
- `__pycache__/`, `.venv/`, `node_modules/`, `dist/`, `build/`
- `target/` (Rust), `release/` (Android APK)
- 本地配置文件: `.env`, `*.db`, `*.db-shm`, `*.db-wal`


## How to interpret this list

- Paths are matched as **prefixes** (directory) or **exact files**.
- A modification that **transitively** affects a Core Component
  (e.g. by changing its public API used by another module) is
  itself considered a modification of the Core Component for the
  purposes of LICENSE §3.
- If a Core Component path is renamed or moved, this document is
  authoritative: the path listed here continues to be a Core Component
  regardless of the actual file system location, and the contributor
  of the rename must update this document in the same commit.
- If You are uncertain whether a path is a Core Component, treat it
  as a Core Component, or contact the Project Copyright Holder before
  Distribution.


## Changes to this document

This document may be amended by the Project Copyright Holder at any
time. The version of this document in effect at the time of Your
Distribution governs the source-availability obligation for that
Distribution.

Last updated: 2026-08-28
