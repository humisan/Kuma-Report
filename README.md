# Kuma-Report

**KumaEarth向けに作成された多機能のreportプラグイン**

[![Minecraft Version](https://img.shields.io/badge/Minecraft-1.21-brightgreen.svg)](https://www.minecraft.net/)
[![API Version](https://img.shields.io/badge/API-Paper%2FSpigot-orange.svg)](https://papermc.io/)

---

## 目次

1. [概要](#概要)
2. [主な機能](#主な機能)
3. [インストール](#インストール)
4. [権限設定](#権限設定)
5. [コマンド一覧](#コマンド一覧)
6. [設定ファイル](#設定ファイル)
7. [Discord連携](#discord連携)
8. [GUI機能](#gui機能)
9. [トラブルシューティング](#トラブルシューティング)
10. [開発者情報](#開発者情報)

---

## 概要

Kuma-Reportは、Minecraftサーバー向けの包括的な通報・バグ報告システムです。
プレイヤーの不正行為や迷惑行為を簡単に報告でき、管理者が効率的に対応できる機能を提供します。

### 特徴

- **直感的なGUIシステム** - Java版・統合版（Bedrock）両対応
- **リアルタイム通知** - スタッフにインゲーム通知 + Discord Webhook対応
- **詳細な管理機能** - 通報の承認・却下・統計情報の閲覧
- **クールダウン機能** - スパム防止のための制限時間設定
- **16進数カラーコード対応** - 美しいグラデーション表示
- **データベース管理** - SQLiteによる永続的なデータ保存
- **多言語対応** - カスタマイズ可能なメッセージシステム

---

## 主な機能

### 1. プレイヤー通報システム

プレイヤーが他のプレイヤーを通報できる機能です。

**通報方法:**
- GUIから選択（Java版・Bedrock版両対応）
- コマンドで直接入力

**通報カテゴリ:**
- チート使用（X-Ray、Fly、KillAuraなど）
- 迷惑行為（PK、アイテム破壊など）
- 不適切発言（暴言、差別発言など）
- 荒らし行為（建築物の破壊、妨害など）
- その他（カスタム理由）

### 2. バグレポートシステム

サーバーのバグや不具合を報告できる機能です。

**機能:**
- 詳細な説明入力（10～1000文字）
- 自動的に座標情報を記録
- スタッフへのリアルタイム通知

### 3. 管理者向け通報管理

**処理可能な操作:**
- 通報一覧の閲覧（ページネーション対応）
- 個別通報の詳細表示
- 通報の承認・却下
- プレイヤー別の通報検索
- 統計情報の閲覧
- 設定のリロード

### 4. Discord連携

Discord Webhookを通じて、通報やバグレポートをDiscordチャンネルに自動送信できます。

**通知内容:**
- 通報ID
- 通報者・被通報者情報
- 理由
- 日時
- オプション：特定ロールへのメンション

### 5. 通知システム

**スタッフへの通知:**
- インゲームチャット通知（カラーコード対応）
- 通知音の再生（カスタマイズ可能）
- Discord Webhook連携

---

## インストール

### 必須要件

- Minecraft Server 1.21以降
- Paper / Spigot / Purpur
- Java 17以降

### オプション

- [Geyser](https://geysermc.org/) + [Floodgate](https://github.com/GeyserMC/Floodgate) - Bedrock版プレイヤー対応

### インストール手順

1. **プラグインのダウンロード**
   - リリースページから最新の `Kuma-Report-X.X.X.jar` をダウンロード

2. **サーバーへの配置**
   ```bash
   # プラグインフォルダに配置
   plugins/Kuma-Report-X.X.X.jar
   ```

3. **サーバーの起動**
   - サーバーを起動すると、自動的に設定ファイルが生成されます
   ```
   plugins/Kuma-Report/
   ├── config.yml         # メイン設定ファイル
   ├── messages.yml       # メッセージ設定
   └── reports.db         # データベース（自動生成）
   ```

4. **設定のカスタマイズ**
   - `config.yml` と `messages.yml` を編集
   - `/reportadmin reload` で設定を再読み込み

---

## 権限設定

### 権限一覧

| 権限ノード | 説明 | デフォルト |
|-----------|------|-----------|
| `kumareport.use` | プレイヤー通報機能の使用 | `true`（全員） |
| `kumareport.bugreport` | バグレポート機能の使用 | `true`（全員） |
| `kumareport.admin` | 管理者コマンドの実行 | `op`（管理者のみ） |
| `kumareport.notify` | 通報時の通知を受け取る | `op`（管理者のみ） |
| `kumareport.bypass.cooldown` | クールダウンをバイパス | `op`（管理者のみ） |

### LuckPermsでの設定例

```bash
# 一般プレイヤーに通報権限を付与
lp group default permission set kumareport.use true
lp group default permission set kumareport.bugreport true

# モデレーターに通知権限を付与
lp group moderator permission set kumareport.notify true

# 管理者に全権限を付与
lp group admin permission set kumareport.admin true
lp group admin permission set kumareport.bypass.cooldown true
```

---

## コマンド一覧

### プレイヤー向けコマンド

#### `/report` - プレイヤー通報

**エイリアス:** `/rep`

**使用方法:**
```
/report                    # GUI経由で通報
/report <プレイヤー名> <理由>  # テキストで直接通報
```

**使用例:**
```
/report                      # GUIを開く
/report Player123 チートを使用している  # 直接通報
```

**権限:** `kumareport.use`

#### `/bugreport` - バグレポート

**エイリアス:** `/bug`, `/breport`

**使用方法:**
```
/bugreport               # GUIから入力
/bugreport <説明>         # 直接報告
```

**使用例:**
```
/bugreport ブロックを置くと消える
/bugreport クラフトができない
```

**権限:** `kumareport.bugreport`

---

### 管理者向けコマンド

#### `/reportadmin` - 通報管理

**エイリアス:** `/radmin`, `/ra`

**サブコマンド一覧:**

##### 1. GUI表示（引数なし）
```
/reportadmin
```
管理者向けGUIを開きます（Java版のみ）。

##### 2. list - 通報一覧表示
```
/reportadmin list [ページ番号]
```
通報一覧をページ単位で表示します。

**使用例:**
```
/reportadmin list      # 1ページ目を表示
/reportadmin list 2    # 2ページ目を表示
```

##### 3. view - 通報詳細表示
```
/reportadmin view <通報ID>
```
指定した通報の詳細情報を表示します。

**表示内容:**
- 通報者・被通報者
- 通報理由
- 通報タイプ
- ステータス
- 作成日時
- 処理者情報（処理済みの場合）
- 処理メモ

**使用例:**
```
/reportadmin view 42
```

##### 4. accept - 通報承認
```
/reportadmin accept <通報ID> [メモ]
```
通報を承認し、ステータスを「承認済み」に変更します。

**使用例:**
```
/reportadmin accept 42
/reportadmin accept 42 3日間のBANを実施
```

**動作:**
- 通報者に承認通知が送信されます
- 処理者名・処理日時が記録されます

##### 5. deny - 通報却下
```
/reportadmin deny <通報ID> [メモ]
```
通報を却下し、ステータスを「却下済み」に変更します。

**使用例:**
```
/reportadmin deny 42
/reportadmin deny 42 証拠不十分のため却下
```

**動作:**
- 通報者に却下通知が送信されます
- 処理者名・処理日時が記録されます

##### 6. search - プレイヤー検索
```
/reportadmin search <プレイヤー名>
```
特定のプレイヤーに関する通報を検索します。

**使用例:**
```
/reportadmin search Player123
```

**注意:** 現在準備中の機能です。

##### 7. stats - 統計情報表示
```
/reportadmin stats
```
通報の統計情報を表示します。

**表示内容:**
- 総通報数
- 未処理の通報数
- 承認済みの通報数
- 却下済みの通報数

##### 8. reload - 設定リロード
```
/reportadmin reload
```
設定ファイルとメッセージファイルを再読み込みします。

**リロード対象:**
- `config.yml`
- `messages.yml`

**権限:** `kumareport.admin`

---

## 設定ファイル

### config.yml

メインの設定ファイルです。

#### データベース設定

```yaml
database:
  type: sqlite                              # データベースタイプ
  path: plugins/Kuma-Report/reports.db      # SQLiteファイルのパス
```

#### クールダウン設定

```yaml
cooldown:
  player-report: 60      # プレイヤー通報のクールダウン（秒）
  bug-report: 120        # バグレポートのクールダウン（秒）
```

**推奨値:**
- プレイヤー通報: 30～120秒
- バグレポート: 60～300秒

#### 通知設定

```yaml
notifications:
  enable-staff-notification: true   # スタッフへの通知を有効化
  play-sound: true                  # 通知音を再生
  sound-volume: 1.0                 # 音量（0.0～1.0）
  sound-pitch: 1.2                  # ピッチ（0.5～2.0）
  sound-type: BLOCK_NOTE_BLOCK_PLING  # サウンドタイプ
```

**利用可能なサウンドタイプ:**
- `BLOCK_NOTE_BLOCK_PLING` - 優しいピン音
- `ENTITY_EXPERIENCE_ORB_PICKUP` - 経験値取得音
- `BLOCK_ANVIL_LAND` - アンビル着地音（重厚）
- `ENTITY_PLAYER_LEVELUP` - レベルアップ音

#### Discord Webhook設定

```yaml
discord:
  enabled: false                    # Discord連携の有効化
  webhook-url: ""                   # Webhook URL
  mention-role-id: ""               # メンションするロールID（オプション）
```

**設定方法:**

1. Discordサーバーで通知チャンネルを作成
2. チャンネル設定 → 連携サービス → Webhookを作成
3. Webhook URLをコピーして `webhook-url` に貼り付け
4. `enabled: true` に変更
5. `/reportadmin reload` でリロード

**ロールメンション設定:**
1. Discord開発者モードを有効化
2. ロールを右クリック → IDをコピー
3. `mention-role-id` に貼り付け

#### カラーシステム設定

```yaml
colors:
  use-hex-colors: true    # 16進数カラーコードを使用（false: 従来の§コード）
  debug-mode: false       # デバッグモード（true: カラーコードなしで表示）
```

**16進数カラーコード形式:**
```yaml
# messages.ymlで使用
message: "&#60A5FAこれは青色のテキストです"
```

#### GUI設定

```yaml
gui:
  items-per-page: 45      # 1ページあたりの表示数（最大45）
  title:
    player-select: "&#60A5FA通報するプレイヤーを選択"
    category-select: "&#60A5FA通報理由を選択"
    report-list: "&#60A5FA通報一覧"
    report-detail: "&#60A5FA通報詳細 - ID: {id}"
```

#### 通報理由カテゴリ

```yaml
report-categories:
  - name: "チート使用"
    material: BARRIER
    description:
      - "&#94A3B8不正なModやクライアント改造"
      - "&#94A3B8X-Ray、Fly、KillAuraなど"

  - name: "迷惑行為"
    material: TNT
    description:
      - "&#94A3B8他プレイヤーへの嫌がらせ"
      - "&#94A3B8PK、アイテム破壊など"

  - name: "不適切発言"
    material: WRITABLE_BOOK
    description:
      - "&#94A3B8暴言、差別発言"
      - "&#94A3B8過度な下ネタなど"

  - name: "荒らし行為"
    material: DIAMOND_PICKAXE
    description:
      - "&#94A3B8建築物の破壊"
      - "&#94A3B8意図的なサーバー妨害"

  - name: "その他"
    material: PAPER
    description:
      - "&#94A3B8上記以外の理由"
      - "&#94A3B8クリック後にチャットで入力"
```

**カスタマイズ:**
- `name`: カテゴリ名
- `material`: アイテムアイコン（[Material一覧](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html)）
- `description`: 説明文（複数行可）

---

### messages.yml

すべてのメッセージをカスタマイズできます。

#### カラーパレット

```yaml
# ベース: &#1E3A8A (ネイビーブルー)
# 成功: &#10B981 (ミントグリーン)
# エラー: &#EF4444 (ソフトレッド)
# 警告: &#F59E0B (アンバー)
# 情報: &#60A5FA (ライトブルー)
# グレー: &#94A3B8 (スレートグレー)
```

#### メッセージのカスタマイズ

**プレースホルダー対応:**

| プレースホルダー | 説明 |
|----------------|------|
| `{prefix}` | プラグインのプレフィックス |
| `{player}` | プレイヤー名 |
| `{time}` | 残り時間（秒） |
| `{id}` | 通報ID |
| `{reporter}` | 通報者名 |
| `{reported}` | 被通報者名 |
| `{reason}` | 通報理由 |
| `{description}` | バグの説明 |
| `{location}` | 座標情報 |
| `{page}` | 現在のページ |
| `{maxpage}` | 最大ページ数 |

**カスタマイズ例:**

```yaml
report:
  success: "{prefix} &#10B981&#F3F4F6{player}&#10B981を通報しました。スタッフが確認します。"
  cooldown: "{prefix} &#F59E0B通報のクールダウン中です。あと&#FBBF24{time}秒&#F59E0Bお待ちください。"
```

---

## Discord連携

### Webhook URL の取得

1. **Discordサーバーで設定**
   - 通知を送りたいチャンネルを選択
   - チャンネル設定（⚙️）を開く

2. **連携サービス設定**
   - 「連携サービス」タブをクリック
   - 「ウェブフック」→「新しいウェブフック」

3. **Webhookの作成**
   - 名前: `Kuma-Report`
   - アイコン: お好みの画像
   - 「ウェブフックURLをコピー」

4. **config.ymlに設定**
   ```yaml
   discord:
     enabled: true
     webhook-url: "https://discord.com/api/webhooks/YOUR_WEBHOOK_URL"
     mention-role-id: ""  # オプション
   ```

5. **設定をリロード**
   ```
   /reportadmin reload
   ```

### ロールメンションの設定

特定のロールにメンションを送る場合:

1. **Discord開発者モードを有効化**
   - ユーザー設定 → 詳細設定 → 開発者モード: ON

2. **ロールIDを取得**
   - サーバー設定 → ロール
   - 対象のロールを右クリック → 「IDをコピー」

3. **config.ymlに設定**
   ```yaml
   discord:
     enabled: true
     webhook-url: "https://discord.com/api/webhooks/YOUR_WEBHOOK_URL"
     mention-role-id: "123456789012345678"
   ```

### 通知メッセージの例

**プレイヤー通報:**
```
🚨 新しい通報

ID: #42
通報者: PlayerA
被通報者: PlayerB
理由: チート使用 - X-Rayを使用している疑い
日時: 2025-12-27 14:30:00
```

**バグレポート:**
```
🐛 新しいバグレポート

ID: #15
報告者: PlayerC
説明: ブロックを置くと消えてしまう
場所: world: X=123, Y=64, Z=-456
日時: 2025-12-27 14:35:00
```

---

## GUI機能

### Java版プレイヤー向けGUI

#### プレイヤー選択GUI
- オンラインプレイヤー一覧を表示
- クリックで選択
- 次へ/戻るボタンでページ移動

#### カテゴリ選択GUI
- 通報理由のカテゴリを表示
- 各アイテムに説明が表示される
- 「その他」を選択すると、チャットで理由を入力

#### 通報一覧GUI（管理者用）
- 未処理/全ての通報を表示
- クリックで詳細表示
- ステータスに応じた色分け

#### 通報詳細GUI（管理者用）
- 通報の詳細情報を表示
- 承認・却下ボタン
- 処理済みの場合は処理者情報も表示

### Bedrock版プレイヤー向けForm

Geyser + Floodgate環境で、Bedrock版プレイヤーにも対応しています。

**利用可能なForm:**
- SimpleForm - 選択肢を表示
- ModalForm - 入力フィールド付きフォーム
- CustomForm - カスタムUI要素

**自動判定:**
プラグインは自動的にプレイヤーのプラットフォームを判定し、適切なUIを表示します。

---

## トラブルシューティング

### よくある問題と解決方法

#### 1. プラグインが起動しない

**症状:**
```
Could not load 'plugins/Kuma-Report-X.X.X.jar'
```

**原因と解決策:**
- Javaバージョンが古い → Java 17以降を使用
- サーバーバージョンが古い → Minecraft 1.21以降を使用
- Paper/Spigot以外のサーバー → Paper/Spigot/Purpurを使用

#### 2. 通報が保存されない

**症状:**
通報を送信しても、`/reportadmin list` に表示されない

**原因と解決策:**
1. データベースファイルの権限を確認
   ```bash
   # Linuxの場合
   chmod 755 plugins/Kuma-Report/
   chmod 644 plugins/Kuma-Report/reports.db
   ```

2. サーバーログを確認
   ```
   [Kuma-Report] 通報の保存に失敗しました: ...
   ```

3. config.ymlのデータベース設定を確認

#### 3. Discord通知が届かない

**症状:**
通報してもDiscordに通知が来ない

**チェックリスト:**
- [ ] `config.yml` で `enabled: true` になっているか
- [ ] Webhook URLが正しいか（https://discord.com/api/webhooks/...）
- [ ] サーバーログにエラーが出ていないか
- [ ] Discordチャンネルの権限設定が正しいか

**テスト方法:**
```bash
# curlでWebhookをテスト
curl -X POST "YOUR_WEBHOOK_URL" \
  -H "Content-Type: application/json" \
  -d '{"content": "Test message"}'
```

#### 4. スタッフに通知が届かない

**症状:**
通報しても管理者に通知が来ない

**確認項目:**
1. 権限の確認
   ```
   /lp user <管理者名> permission check kumareport.notify
   ```

2. config.ymlの通知設定
   ```yaml
   notifications:
     enable-staff-notification: true
   ```

3. 管理者がオンラインか確認

#### 5. GUIが開かない（Java版）

**症状:**
`/report` を実行してもGUIが開かない

**原因と解決策:**
- インベントリが満杯 → インベントリを整理
- 他のプラグインとの競合 → プラグインの優先順位を確認
- サーバーログを確認

#### 6. Bedrock版でFormが表示されない

**症状:**
Bedrock版プレイヤーに通報フォームが表示されない

**確認項目:**
- [ ] Geyserがインストールされているか
- [ ] Floodgateがインストールされているか
- [ ] plugin.ymlで `softdepend: [floodgate]` が設定されているか

#### 7. クールダウンが機能しない

**症状:**
連続で通報できてしまう

**確認:**
1. プレイヤーの権限を確認
   ```
   /lp user <プレイヤー名> permission check kumareport.bypass.cooldown
   ```
   `bypass.cooldown` 権限がある場合、クールダウンは適用されません。

2. config.ymlの設定を確認
   ```yaml
   cooldown:
     player-report: 60
   ```

#### 8. 16進数カラーコードが表示されない

**症状:**
`&#60A5FA` などのコードがそのまま表示される

**原因と解決策:**
- サーバーがHex色に対応していない（Minecraft 1.16未満）
- config.ymlで `use-hex-colors: false` に設定
- または Paper/Spigot 1.16以降を使用

#### 9. データベースが破損した

**症状:**
```
[Kuma-Report] SQLException: database disk image is malformed
```

**対処法:**
1. バックアップがある場合
   ```bash
   # バックアップから復元
   cp backup/reports.db plugins/Kuma-Report/reports.db
   ```

2. バックアップがない場合
   ```bash
   # データベースを再作成（通報データは失われます）
   rm plugins/Kuma-Report/reports.db
   # サーバーを再起動すると新しいDBが作成されます
   ```

3. 定期バックアップの設定（推奨）
   ```bash
   # cronで毎日バックアップ
   0 3 * * * cp /path/to/plugins/Kuma-Report/reports.db /path/to/backup/reports-$(date +\%Y\%m\%d).db
   ```

---

## 開発者情報

### ビルド方法

```bash
# クローン
git clone https://github.com/humisan/Kuma-Report.git
cd Kuma-Report

# Gradleでビルド
./gradlew build

# 生成されたJARファイル
build/libs/Kuma-Report-X.X.X.jar
```

### プロジェクト構造

```
src/main/java/lol/hanyuu/kumaReport/
├── KumaReport.java              # メインクラス
├── command/                      # コマンドハンドラ
│   ├── ReportCommand.java
│   ├── BugReportCommand.java
│   └── ReportAdminCommand.java
├── database/                     # データベース層
│   ├── DatabaseManager.java
│   ├── ReportDAO.java
│   └── BugReportDAO.java
├── gui/                          # GUI実装（Java版）
│   ├── ReportPlayerGUI.java
│   ├── ReportCategoryGUI.java
│   ├── ReportListGUI.java
│   ├── ReportDetailGUI.java
│   └── BugReportGUI.java
├── gui/forms/                    # Form実装（Bedrock版）
│   ├── FormManager.java
│   ├── ReportPlayerForm.java
│   ├── ReportCategoryForm.java
│   ├── ReportReasonInputForm.java
│   ├── ReportListForm.java
│   └── ReportDetailForm.java
├── listener/                     # イベントリスナー
│   ├── GUIListener.java
│   ├── ChatListener.java
│   └── PlayerJoinListener.java
├── manager/                      # マネージャークラス
│   ├── ConfigManager.java
│   ├── MessageManager.java
│   ├── CooldownManager.java
│   └── DiscordWebhookManager.java
└── model/                        # データモデル
    ├── Report.java
    ├── BugReport.java
    ├── ReportType.java
    └── ReportStatus.java
```

### データベーススキーマ

#### reports テーブル

```sql
CREATE TABLE reports (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    reporter_uuid TEXT NOT NULL,
    reporter_name TEXT NOT NULL,
    reported_uuid TEXT NOT NULL,
    reported_name TEXT NOT NULL,
    reason TEXT NOT NULL,
    report_type TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'PENDING',
    handler_uuid TEXT,
    handler_name TEXT,
    handler_note TEXT,
    created_at INTEGER NOT NULL,
    handled_at INTEGER
);
```

#### bug_reports テーブル

```sql
CREATE TABLE bug_reports (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    reporter_uuid TEXT NOT NULL,
    reporter_name TEXT NOT NULL,
    description TEXT NOT NULL,
    location TEXT NOT NULL,
    created_at INTEGER NOT NULL
);
```

### API使用例

他のプラグインからKuma-Reportの機能を利用する場合:

```java
// プラグインインスタンスの取得
KumaReport kumaReport = (KumaReport) Bukkit.getPluginManager().getPlugin("Kuma-Report");

// 通報の作成
Report report = new Report(
    reporterUUID,
    reporterName,
    reportedUUID,
    reportedName,
    reason,
    ReportType.CHEAT
);

// データベースに保存
int reportId = kumaReport.getDatabaseManager()
    .getReportDAO()
    .createReport(report);

// スタッフに通知
kumaReport.notifyStaff(report);
```

---

## 更新履歴

### v1.0.0 (2025-12-27)
- 初回リリース
- プレイヤー通報機能
- バグレポート機能
- 管理者向けコマンド
- Discord Webhook連携
- Java版/Bedrock版両対応
- 16進数カラーコード対応

---

## ライセンス

このプロジェクトは [MITライセンス](LICENSE) の下で公開されています。

---

## サポート

### 問題を報告する

バグや機能要望は [GitHub Issues](https://github.com/humisan/Kuma-Report/issues) で報告してください。

### 貢献する

プルリクエストを歓迎します。大きな変更を加える場合は、まずIssueで議論してください。

---

## 作成者

- **Author:** humisan
- **GitHub:** https://github.com/humisan
- **Project:** KumaEarth

---

## 謝辞

このプラグインの開発にあたり、以下のプロジェクトを参考にさせていただきました:

- [Paper API](https://papermc.io/)
- [Geyser](https://geysermc.org/)
- [Floodgate](https://github.com/GeyserMC/Floodgate)

---

**Kuma-Report** - プレイヤーが安心して遊べるサーバー環境を目指して
