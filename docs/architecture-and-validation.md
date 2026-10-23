# 実装・検証の補足

READMEから分離した開発用の説明です。過去の測定値は対応する当時の環境の記録であり、現在の動作保証値ではありません。対応環境はREADMEと互換性・リリース文を確認してください。

## ビルド

```bash
./gradlew spotlessApply
./gradlew spotlessCheck
./gradlew testDebugUnitTest assembleDebug
./gradlew assembleRelease
```

KotlinとGradle Kotlin DSLはSpotless + ktfmtで統一整形します。`preBuild`は
`spotlessCheck`とライセンス台帳検証を必ず実行するため、未整形のコードや必要な
ライセンスファイルの欠落を含むAPKは生成しません。

デバッグ用APKは `app/build/outputs/apk/debug/app-debug.apk` に生成されます。未難読化かつ
`run-as`可能で、スタックトレースと端末内ログの調査を優先します。

GitHub配布用APKは `app/build/outputs/apk/release/app-release.apk` に生成されます。こちらは
非デバッグビルドで、R8のコード最適化・未使用コード削除・リソース縮小を有効にします。
開発中の既存インストールを上書きできるよう、現時点では同じ開発用署名鍵で署名します。

## 診断ログ

現在ログは最大8MBのJSON Lines、過去ログは最大16世代のgzip圧縮JSON Linesとして、Direct Boot対応のアプリ専用領域に保存します。複数プロセスからの書き込みとローテーションはファイルロックで直列化し、旧版で生じたNUL領域は初回起動時にJSON行を保ったまま自動修復します。状態が変わらないWi-Fi待機判定は10分ごとの要約、同種のリアルタイム通知は30秒ごとの要約にして、品質・比較・切替ログを埋もれにくくしています。

各NetMonster観測でPrimary / Secondary / Neighbor全セルの全取得値を間引かず記録し、無線ページ表示中の5秒観測も保存します。電話番号、ICCID、IMSI、IMEIは記録しません。各監視周期には通信経路、SIM名／ID、dBm、RSRP/RSRQ/SINR、CQI、帯域幅、CA、セル識別情報、無線評価、品質スコアと内訳、両SIMの比較値、残容量、判定、切替しなかった理由、Shizuku状態を含みます。SIM Pilotが要求した変更は `app/ui_manual`、`app/auto_failover`、`app/comparison_probe`、`app/comparison_revert`、`app/wifi_restore`、それ以外の変更は `external_user_or_system` と記録します。

接続中の端末からログと現在のシステム状態をまとめて取得できます。デバッグ版は`run-as`、
GitHub配布用の非デバッグrelease版は`android.permission.DUMP`でshellだけに公開した読み取り専用
ContentProviderを自動的に使います。どちらも現在JSONL、gzipアーカイブ、`dumpsys`、既定SIM、
logcatを同じスクリプトで保存できます。

```bash
./tools/pull-device-logs.sh DEVICE_SERIAL
```

デバッグ版で現在ログだけを直接読む場合:

```bash
adb -s DEVICE_SERIAL exec-out run-as dev.simpilot cat /data/user_de/0/dev.simpilot/files/diagnostics/sim-pilot-current.jsonl
```

release版で現在ログだけを直接保存する場合:

```bash
adb -s DEVICE_SERIAL exec-out content read \
  --uri content://dev.simpilot.diagnostics/logs/sim-pilot-current.jsonl > sim-pilot-current.jsonl
```

アプリ画面では「設定 → 診断ログ」から最近の無線・判定・切替・警告ログを絞り込み表示できます。
各項目の生JSONも確認でき、「ZIPへ保存」で現在ログと全アーカイブを端末上の任意の保存先へ
書き出せます。ストレージ権限は要求しません。

設定画面の「Wi-Fi接続時」では、復帰機能全体とデータ・通話・メッセージの各対象を独立してON/OFFできます。「現在の既定SIMを復帰先にセット」を使うと、現在の3つの割り当てをまとめて記録できます。SIM名やSIMの有無は `SubscriptionManager` から毎回取得し、通信会社名をコードへ固定していません。

## 判定と通信量

通常は小さな204応答だけを確認し、定期確認・弱電波・RSRQ/SINR悪化・遅延悪化時に64KBをダウンロードして実効速度を測ります。電波、電波品質、遅延、速度を0〜100の悪化スコアへ合算し、既定値では45以上を低品質、20以上を品質低下として扱います。候補回線が低品質のままなら、電波と瞬間速度だけが良くても採用しません。ただし現在回線が疎通不能、または候補の総合スコアが12点以上改善する場合は復旧を優先します。目標速度、弱電波dBm、連続回数、監視間隔、クールダウンは「設定 → 通信品質」で調整できます。NetMonster情報を取得できない端末やタイミングでは、従来のAndroid公開API値だけで判定を継続します。

「設定 → データプラン」ではSIMごとに次を設定できます。

- 毎月更新: 容量と更新日。31日など存在しない月はその月の末日に丸めます。
- 期間指定: 開始日、終了日、容量。
- 柔軟プラン: 開始日・終了日・容量が異なる複数のデータトッピング。povoの24時間・30日・365日などを併存できます。
- 手動残量: 事業者アプリの残量を優先。自動集計との差や複数トッピングの消費順が不明な場合に使います。

SIM別実績はShizukuのshell権限からAndroidのネットワーク統計を読みます。Galaxy S26 Ultraでは公開APIで加入者別取得が拒否されるため、`dumpsys netstats` の`subId`別・1時間バケットを端末内で集計するフォールバックを使います。加入者識別子は一時的なAPI照会以外に使わず、保存・画面表示・ログ出力をしません。統計にはOS側の集計遅延があり、プラン残量の正確性は事業者アプリが優先です。

残容量は通信品質が同等の場合のタイブレークです。圏外・疎通不能からの復旧が常に最優先で、現在回線が良好な状態で15分以上安定し、候補回線の電波も十分な場合のみ、残量率が15ポイント以上かつ500MB以上多いSIMを比較対象にします。

## 実機で確認した切替経路

| 対象 | ISub transaction | 検証先 |
|---|---:|---|
| データ | 31 | `mActiveDataSubId` / `multi_sim_data_call` |
| 通話 | 34 | Telecom `defaultOutgoing` / `multi_sim_voice_call` |
| メッセージ | 37 | `defaultSmsSubId` / `multi_sim_sms` |

`settings put global multi_sim_data_call` だけでは表示値しか変わらず、実データ回線は切り替わりません。このため本アプリは設定値の直接書換えではなく、Samsung端末上のSubscription Binderをshell権限で呼び、変更後の値も検証します。
