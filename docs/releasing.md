# 署名済みreleaseの作成

`versionCode`と`versionName`を更新して変更をコミットし、次を実行します。JDKとAndroid SDKの版はGradle設定を使用します。

```sh
python3 tools/package_release.py --source-ref HEAD
```

非公開の`.signing/signing.properties`、または環境変数`ONEUI_SIGNING_PROPERTIES`で指定したファイルに、`storeFile`、`storePassword`、`keyAlias`、`keyPassword`を用意します。storeFileの相対パスはpropertiesファイルの位置から解決します。このファイルと鍵はGitへ追加しません。秘密情報は公開のログ・Release本文・ソースZIPへ含めません。

既存APKと同じ配布証明書を維持します。スクリプトはreleaseビルド、テスト、lint、16KB境界の整列、APK構造、署名証明書を検証し、`dist/`へAPK・同一コミットのソースZIP・署名検証結果・公開メタデータ・SHA256SUMSを生成します。debug variantを配布せず、署名だけを理由に鍵を置き換えません。

公開時は新しいタグを同一コミットに作り、生成した5種類のファイルを添付します。既存タグは移動しません。リリース文は利用者向けにし、検証の詳細は開発ドキュメントへ分けます。
