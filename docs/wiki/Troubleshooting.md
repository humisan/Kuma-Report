# トラブルシューティング

このページでは、Kuma-Reportでよくある問題とその解決方法を説明します。

## 目次

1. [インストール・起動の問題](#インストール起動の問題)
2. [データベースの問題](#データベースの問題)
3. [Discord通知の問題](#discord通知の問題)
4. [GUI・フォームの問題](#guiフォームの問題)
5. [権限の問題](#権限の問題)
6. [パフォーマンスの問題](#パフォーマンスの問題)
7. [その他の問題](#その他の問題)

---

## インストール・起動の問題

### プラグインが起動しない

#### 症状
```
Could not load 'plugins/Kuma-Report-X.X.X.jar' in folder 'plugins'
```

#### 原因と解決策

**原因1: Javaバージョンが古い**

確認:
```bash
java -version
```

解決策:
- Java 17以降をインストール
- サーバーの起動スクリプトでJava 17を使用するよう設定

```bash
# Linuxの場合
/usr/lib/jvm/java-17-openjdk/bin/java -jar server.jar
```

**原因2: Minecraftバージョンが古い**

確認:
```
/version
```

解決策:
- Minecraft 1.21以降にアップデート
- Paper/Spigot/Purpurを使用

**原因3: 互換性のないサーバーソフトウェア**

解決策:
- Bukkit、CraftBukkitではなく、Paper/Spigot/Purpurを使用

---

### プラグインが有効化されない

#### 症状
```
[Server] Kuma-Report v1.0.0 has been disabled
```

#### 原因と解決策

**原因1: 依存プラグインの欠如**

確認:
サーバーログを確認:
```
Missing dependency: ...
```

解決策:
- 必要な依存プラグインをインストール
- Kuma-Reportは独立したプラグインなので、通常は依存プラグインは不要

**原因2: 設定ファイルのエラー**

確認:
サーバーログを確認:
```
Error loading config.yml: ...
```

解決策:
1. `config.yml` のYAML構文エラーを修正
2. [YAML Validator](https://www.yamllint.com/)でチェック
3. 修正できない場合は、ファイルを削除して再生成

```bash
# 設定ファイルを削除（バックアップを取ってから）
rm plugins/Kuma-Report/config.yml
# サーバーを再起動すると新しいファイルが生成される
```

---

### コマンドが認識されない

#### 症状
```
Unknown command. Type "/help" for help.
```

#### 原因と解決策

**原因: プラグインが正しく起動していない**

確認:
```
/plugins
```

Kuma-Reportが緑色（有効）で表示されているか確認

解決策:
1. サーバーログを確認してエラーを特定
2. プラグインを再インストール
3. `/reload confirm` でリロード（非推奨）

---

## データベースの問題

### 通報が保存されない

#### 症状
通報を送信しても、`/reportadmin list` に表示されない

#### 原因と解決策

**原因1: データベースファイルの権限**

確認（Linuxの場合）:
```bash
ls -l plugins/Kuma-Report/reports.db
```

解決策:
```bash
chmod 644 plugins/Kuma-Report/reports.db
chmod 755 plugins/Kuma-Report/
```

**原因2: ディスク容量不足**

確認:
```bash
df -h
```

解決策:
- ディスク容量を確保
- 不要なファイルを削除

**原因3: データベースの破損**

確認:
サーバーログを確認:
```
SQLException: database disk image is malformed
```

解決策:
```bash
# バックアップから復元
cp backup/reports.db plugins/Kuma-Report/reports.db

# バックアップがない場合
# データベースを再作成（データは失われます）
rm plugins/Kuma-Report/reports.db
# サーバーを再起動
```

---

### データベースエラーが発生する

#### 症状
```
[Kuma-Report] SQLException: ...
```

#### 原因と解決策

**原因1: データベースファイルがロックされている**

解決策:
1. サーバーを正常に停止
2. データベースを使用している他のプロセスを終了
3. サーバーを再起動

**原因2: データベースファイルが破損している**

解決策:
```bash
# SQLiteでデータベースをチェック
sqlite3 plugins/Kuma-Report/reports.db "PRAGMA integrity_check;"

# 破損している場合
# 1. バックアップから復元
# 2. または再作成
```

---

## Discord通知の問題

### Discord通知が届かない

#### 症状
通報してもDiscordに通知が来ない

#### 原因と解決策

**原因1: Discord連携が無効**

確認:
```yaml
discord:
  enabled: false  # ← これがfalseになっていないか
```

解決策:
```yaml
discord:
  enabled: true
```

`/reportadmin reload` でリロード

**原因2: Webhook URLが間違っている**

確認:
```yaml
discord:
  webhook-url: "https://discord.com/api/webhooks/..."
```

解決策:
1. Webhook URLが正しいか確認
2. 余分なスペースや改行がないか確認
3. Webhookが削除されていないか確認

**原因3: ネットワークの問題**

確認:
サーバーログを確認:
```
[Kuma-Report] Discord通知の送信に失敗しました: ...
```

解決策:
1. サーバーからインターネットに接続できるか確認
```bash
ping discord.com
```

2. ファイアウォールの設定を確認
3. プロキシ設定が必要な場合は設定

**原因4: Webhook URLのテスト**

確認:
curlでWebhookをテスト:
```bash
curl -X POST "YOUR_WEBHOOK_URL" \
  -H "Content-Type: application/json" \
  -d '{"content": "テストメッセージ"}'
```

解決策:
- エラーが返ってくる場合、Webhook URLが無効
- 新しいWebhookを作成して再設定

---

### ロールメンションが機能しない

#### 症状
Discord通知は届くが、ロールがメンションされない

#### 原因と解決策

**原因1: ロールIDが間違っている**

確認:
```yaml
discord:
  mention-role-id: "123456789012345678"
```

解決策:
1. Discord開発者モードを有効化
2. ロールIDを再度コピー
3. 18桁の数字であることを確認

**原因2: ロールがメンション可能ではない**

確認:
Discordのサーバー設定 → ロール → 対象のロール

解決策:
「このロールについてメンションを許可する」をONにする

---

## GUI・フォームの問題

### GUIが開かない（Java版）

#### 症状
`/report` を実行してもGUIが開かない

#### 原因と解決策

**原因1: インベントリが満杯**

解決策:
インベントリを整理してから再度実行

**原因2: 他のプラグインとの競合**

確認:
他のGUIプラグインが導入されているか確認

解決策:
1. 競合しているプラグインを特定
2. 優先順位を調整
3. 必要に応じて他のプラグインを無効化

**原因3: パーミッションの問題**

確認:
```bash
lp user <プレイヤー名> permission check kumareport.use
```

解決策:
必要な権限を付与

---

### Bedrock版でFormが表示されない

#### 症状
Bedrock版プレイヤーに通報フォームが表示されない

#### 原因と解決策

**原因1: GeyserまたはFloodgateが導入されていない**

確認:
```
/plugins
```

解決策:
1. Geyserをインストール: https://geysermc.org/download
2. Floodgateをインストール: https://github.com/GeyserMC/Floodgate/releases

**原因2: plugin.ymlの設定**

確認:
`plugin.yml` で `softdepend: [floodgate]` が設定されているか

解決策:
プラグインを再インストール

---

## 権限の問題

### コマンドが実行できない

#### 症状
```
[Kuma-Report] 権限がありません。
```

#### 原因と解決策

**原因: 必要な権限がない**

確認:
```bash
# LuckPermsの場合
lp user <プレイヤー名> permission check kumareport.use

# GroupManagerの場合
/manulistp <プレイヤー名>
```

解決策:
必要な権限を付与

```bash
# LuckPermsの場合
lp user <プレイヤー名> permission set kumareport.use true

# GroupManagerの場合
/manuaddp <プレイヤー名> kumareport.use
```

---

### スタッフ通知が受け取れない

#### 症状
通報があってもチャット通知が表示されない

#### 原因と解決策

**原因1: kumareport.notify権限がない**

確認:
```bash
lp user <プレイヤー名> permission check kumareport.notify
```

解決策:
```bash
lp user <プレイヤー名> permission set kumareport.notify true
```

**原因2: 通知が無効になっている**

確認:
```yaml
notifications:
  enable-staff-notification: false  # ← これがfalseになっていないか
```

解決策:
```yaml
notifications:
  enable-staff-notification: true
```

---

### クールダウンが効かない

#### 症状
クールダウン時間を無視して連続で通報できる

#### 原因と解決策

**原因: kumareport.bypass.cooldown権限がある**

確認:
```bash
lp user <プレイヤー名> permission check kumareport.bypass.cooldown
```

解決策:
不要な場合は権限を削除

```bash
lp user <プレイヤー名> permission unset kumareport.bypass.cooldown
```

---

## パフォーマンスの問題

### サーバーが重くなる

#### 症状
プラグインを導入後、サーバーのTPS（Tick Per Second）が低下する

#### 原因と解決策

**原因1: 大量の通報データ**

確認:
```
/reportadmin stats
```

解決策:
- 古い通報データを定期的にアーカイブ
- データベースファイルを最適化

```bash
# SQLiteの最適化
sqlite3 plugins/Kuma-Report/reports.db "VACUUM;"
```

**原因2: Discord通知の同期送信**

解決策:
現在のバージョンでは非同期送信に対応しているため、問題ないはずです。
サーバーログを確認してエラーがないか確認してください。

---

## その他の問題

### 16進数カラーコードが表示されない

#### 症状
`&#60A5FA` などのコードがそのまま表示される

#### 原因と解決策

**原因: サーバーがHex色に対応していない**

確認:
Minecraft 1.16未満ではHex色に対応していません

解決策:
```yaml
colors:
  use-hex-colors: false
```

`/reportadmin reload` でリロード

---

### 通知音が鳴らない

#### 症状
通報があっても音が鳴らない

#### 原因と解決策

**原因1: 通知音が無効**

確認:
```yaml
notifications:
  play-sound: false  # ← これがfalseになっていないか
```

解決策:
```yaml
notifications:
  play-sound: true
```

**原因2: 音量が0**

確認:
```yaml
notifications:
  sound-volume: 0.0  # ← これが0.0になっていないか
```

解決策:
```yaml
notifications:
  sound-volume: 1.0
```

**原因3: サウンドタイプが無効**

確認:
```yaml
notifications:
  sound-type: INVALID_SOUND  # ← 無効なサウンド名
```

解決策:
有効なサウンド名を設定

```yaml
notifications:
  sound-type: BLOCK_NOTE_BLOCK_PLING
```

---

### 設定変更が反映されない

#### 症状
`config.yml` を編集しても変更が反映されない

#### 原因と解決策

**原因1: リロードしていない**

解決策:
```
/reportadmin reload
```

**原因2: YAML構文エラー**

確認:
サーバーログを確認:
```
[Kuma-Report] 設定ファイルの読み込みに失敗しました
```

解決策:
1. [YAML Validator](https://www.yamllint.com/)でチェック
2. インデント（スペース）を確認
3. タブ文字を使用していないか確認

**原因3: キャッシュの問題**

解決策:
サーバーを再起動

---

## ログの確認方法

### サーバーログの確認

**リアルタイム確認:**
```bash
tail -f logs/latest.log
```

**特定のエラーを検索:**
```bash
grep "Kuma-Report" logs/latest.log
grep "ERROR" logs/latest.log | grep "Kuma-Report"
```

### デバッグモードの有効化

`config.yml`:
```yaml
debug: true
```

詳細なログが出力されます。

---

## バックアップとリストア

### データベースのバックアップ

```bash
# 手動バックアップ
cp plugins/Kuma-Report/reports.db backup/reports-$(date +%Y%m%d).db

# 自動バックアップ（cron）
0 3 * * * cp /path/to/plugins/Kuma-Report/reports.db /backup/reports-$(date +\%Y\%m\%d).db
```

### データベースのリストア

```bash
# サーバーを停止
stop

# バックアップから復元
cp backup/reports-20251227.db plugins/Kuma-Report/reports.db

# サーバーを起動
```

---

## サポートが必要な場合

問題が解決しない場合は、以下の情報を含めてGitHub Issuesで報告してください:

1. **環境情報:**
   - Minecraftバージョン
   - サーバーソフトウェア（Paper/Spigot/Purpur）
   - Kuma-Reportバージョン
   - Javaバージョン

2. **問題の詳細:**
   - 症状
   - 再現手順
   - 期待される動作

3. **ログ:**
   - サーバーログ（エラー部分）
   - スタックトレース（あれば）

**GitHub Issues:**
https://github.com/humisan/Kuma-Report/issues

---

前のページ: [FAQ](FAQ.md) | 次のページ: [API ドキュメント](API-Documentation.md)
