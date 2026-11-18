# Build environment / ビルド環境

Updated on 2026-11-18. Stable releases are pinned; preview dependencies and dynamic version selectors are not used.

| Component | Version |
| --- | --- |
| Build JDK | 27 |
| Gradle Wrapper | 9.9.1 (distribution checksum verified) |
| Android Gradle Plugin | 9.5.1 |
| Kotlin compiler | 2.4.21 |
| Compile SDK | Android 17, API 37.2 |
| Build Tools | 37.0.0 |
| Java / Kotlin bytecode | Java 17 |

The build JDK does not change the minimum Android version or force Java 27 APIs into the APK. Existing minimum/target SDK contracts and saved user settings are preserved. Android 16/17 hook paths and the existing Xposed API generation remain separate where required. Library versions are pinned in this repository's Gradle files or version catalog.

ビルド用JDKとAPKの実行要件は別です。最低対応Android・target SDK・保存済みの設定は維持しています。依存ライブラリはGradle設定またはバージョンカタログで固定し、新しい安定版へ更新しています。

AGP's built-in Kotlin support is used. The root Kotlin Android plugin declaration selects the compiler version without applying the legacy Android Kotlin plugin to app modules. CI uses the same JDK and SDK pins as local verification. Each app builds independently.

AGP内蔵Kotlinでコンパイルし、ルートのKotlinプラグイン宣言で版を固定しています。アプリ同士のデータ受け渡しや常駐プロセスは追加していません。

```sh
./gradlew :app:testDebugUnitTest :app:lintRelease :app:assembleRelease
```

An unsigned release build is a validation artifact. Follow this repository's release packaging instructions and existing signing identity before distribution. Do not commit signing keys or credentials.

## Compatibility notes / 互換性


## Validation scope / 検証範囲

JVM tests, release compilation, lint, license inventory checks and the repository's release structure checks are run locally. Host-native tests are run for native components where available. These checks do not establish physical One UI hook compatibility or root authorization; no phone UI, sound, device restart or OS changes are part of this build migration.

ローカル試験と実機での描画・フックの確認は区別しています。この更新作業では端末操作、音声再生、再起動は行っていません。

Kotlin currently publishes a fully tested range through Gradle 9.7.0 / AGP 9.3.1. Its documentation permits newer stable releases with compatibility caveats. The newer combination above is checked by this repository's tests and CI; it is not represented as being inside that published test matrix.

## References

- [AGP 9.5 compatibility](https://developer.android.com/build/releases/agp-9-5-0-release-notes)
- [Gradle Java compatibility](https://docs.gradle.org/9.9.1/userguide/compatibility.html)
- [Kotlin Gradle compatibility](https://kotlinlang.org/docs/gradle-configure-project.html)
- [Android SDK configuration](https://developer.android.com/build)
