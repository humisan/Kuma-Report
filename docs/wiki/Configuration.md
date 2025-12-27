# 設定ファイル詳細

このページでは、Kuma-Reportの設定ファイルについて詳しく説明します。

## 目次

1. [config.yml](#configyml)
2. [messages.yml](#messagesyml)
3. [ベストプラクティス](#ベストプラクティス)
4. [高度な設定](#高度な設定)

---

## config.yml

メインの設定ファイルです。すべての機能はこのファイルで制御できます。

### ファイル構造

```yaml
database:        # データベース設定
cooldown:        # クールダウン設定
notifications:   # 通知設定
discord:         # Discord連携設定
colors:          # カラーシステム設定
gui:             # GUI設定
report-categories:  # 通報カテゴリ設定
debug:           # デバッグモード
```

---

### データベース設定

```yaml
database:
  type: sqlite
  path: plugins/Kuma-Report/reports.db
```

#### パラメータ説明

| キー | 説明 | デフォルト値 | 可能な値 |
|------|------|-------------|---------|
| `type` | データベースタイプ | `sqlite` | `sqlite`（現在はSQLiteのみ） |
| `path` | データベースファイルのパス | `plugins/Kuma-Report/reports.db` | 任意のパス |

#### 使用例

**相対パス:**
```yaml
database:
  type: sqlite
  path: plugins/Kuma-Report/reports.db
```

**絶対パス:**
```yaml
database:
  type: sqlite
  path: /var/minecraft/data/reports.db
```

**カスタムフォルダ:**
```yaml
database:
  type: sqlite
  path: plugins/Kuma-Report/data/production.db
```

#### 注意事項

- パスに日本語を含めないでください
- バックアップを定期的に取ることを推奨
- ファイル権限を適切に設定してください（Linux: 644）

---

### クールダウン設定

```yaml
cooldown:
  player-report: 60
  bug-report: 120
```

#### パラメータ説明

| キー | 説明 | デフォルト値 | 推奨値 |
|------|------|-------------|-------|
| `player-report` | プレイヤー通報のクールダウン（秒） | 60 | 30-120 |
| `bug-report` | バグレポートのクールダウン（秒） | 120 | 60-300 |

#### サーバー規模別の推奨値

**小規模サーバー（10-50人）:**
```yaml
cooldown:
  player-report: 30
  bug-report: 60
```

**中規模サーバー（50-200人）:**
```yaml
cooldown:
  player-report: 60
  bug-report: 120
```

**大規模サーバー（200人以上）:**
```yaml
cooldown:
  player-report: 120
  bug-report: 300
```

#### クールダウンのバイパス

管理者やスタッフはクールダウンをバイパスできます:

```bash
lp user <プレイヤー名> permission set kumareport.bypass.cooldown true
```

---

### 通知設定

```yaml
notifications:
  enable-staff-notification: true
  play-sound: true
  sound-volume: 1.0
  sound-pitch: 1.2
  sound-type: BLOCK_NOTE_BLOCK_PLING
```

#### パラメータ説明

| キー | 説明 | デフォルト値 | 可能な値 |
|------|------|-------------|---------|
| `enable-staff-notification` | スタッフへの通知を有効化 | `true` | `true`, `false` |
| `play-sound` | 通知音を再生 | `true` | `true`, `false` |
| `sound-volume` | 音量 | `1.0` | `0.0` - `1.0` |
| `sound-pitch` | ピッチ | `1.2` | `0.5` - `2.0` |
| `sound-type` | サウンドタイプ | `BLOCK_NOTE_BLOCK_PLING` | [Soundリスト](#サウンドタイプ一覧) |

#### サウンドタイプ一覧

**推奨サウンド:**

| サウンド名 | 特徴 | 用途 |
|----------|------|------|
| `BLOCK_NOTE_BLOCK_PLING` | 優しいピン音 | 通常の通知（推奨） |
| `ENTITY_EXPERIENCE_ORB_PICKUP` | 経験値取得音 | さりげない通知 |
| `ENTITY_PLAYER_LEVELUP` | レベルアップ音 | 重要な通知 |
| `BLOCK_ANVIL_LAND` | アンビル着地音 | 緊急の通知 |

**その他のサウンド:**
- `BLOCK_BELL_USE` - 鐘の音
- `ENTITY_ARROW_HIT_PLAYER` - 矢が当たる音
- `ITEM_GOAT_HORN_SOUND_0` - ゴートホーンの音

#### カスタム設定例

**静かな通知:**
```yaml
notifications:
  enable-staff-notification: true
  play-sound: true
  sound-volume: 0.3
  sound-pitch: 1.0
  sound-type: ENTITY_EXPERIENCE_ORB_PICKUP
```

**派手な通知:**
```yaml
notifications:
  enable-staff-notification: true
  play-sound: true
  sound-volume: 1.0
  sound-pitch: 1.5
  sound-type: ENTITY_PLAYER_LEVELUP
```

**通知オフ:**
```yaml
notifications:
  enable-staff-notification: false
  play-sound: false
```

---

### Discord Webhook設定

```yaml
discord:
  enabled: false
  webhook-url: ""
  mention-role-id: ""
```

#### パラメータ説明

| キー | 説明 | 必須 | 例 |
|------|------|------|---|
| `enabled` | Discord連携を有効化 | はい | `true` |
| `webhook-url` | Webhook URL | はい（有効時） | `https://discord.com/api/webhooks/...` |
| `mention-role-id` | メンションするロールID | いいえ | `123456789012345678` |

#### 設定手順

詳細は [Discord連携ガイド](Discord-Integration.md) を参照してください。

**基本設定:**
```yaml
discord:
  enabled: true
  webhook-url: "https://discord.com/api/webhooks/1234567890/abcdefg"
  mention-role-id: ""
```

**ロールメンション付き:**
```yaml
discord:
  enabled: true
  webhook-url: "https://discord.com/api/webhooks/1234567890/abcdefg"
  mention-role-id: "987654321098765432"
```

---

### カラーシステム設定

```yaml
colors:
  use-hex-colors: true
  debug-mode: false
```

#### パラメータ説明

| キー | 説明 | デフォルト値 | 用途 |
|------|------|-------------|------|
| `use-hex-colors` | 16進数カラーコードを使用 | `true` | Minecraft 1.16以降で美しい色表示 |
| `debug-mode` | デバッグモード | `false` | カラーコードなしで表示（開発用） |

#### 16進数カラーコードについて

**対応バージョン:**
- Minecraft 1.16以降
- Paper/Spigot 1.16以降

**形式:**
```yaml
message: "&#RRGGBB文字列"
```

**例:**
```yaml
# 青色のテキスト
message: "&#60A5FA通報を受け付けました"

# グラデーション（複数色の組み合わせ）
prefix: "&#1E3A8AK&#2B4D9Bu&#3860ACm&#4573BDa"
```

**従来の§コードを使用する場合:**
```yaml
colors:
  use-hex-colors: false
```

この場合、`messages.yml` で `§a`, `§c` などの従来のコードを使用します。

---

### GUI設定

```yaml
gui:
  items-per-page: 45
  title:
    player-select: "&#60A5FA通報するプレイヤーを選択"
    category-select: "&#60A5FA通報理由を選択"
    report-list: "&#60A5FA通報一覧"
    report-detail: "&#60A5FA通報詳細 - ID: {id}"
```

#### パラメータ説明

| キー | 説明 | デフォルト値 | 可能な値 |
|------|------|-------------|---------|
| `items-per-page` | 1ページあたりの表示数 | `45` | `27`, `36`, `45` |
| `title.*` | GUIのタイトル | 各種 | 任意の文字列 |

#### アイテム表示数について

**チェストのサイズ:**
- `27` = 3行（小）
- `36` = 4行（中）
- `45` = 5行（大）

**推奨:**
```yaml
gui:
  items-per-page: 45  # 最大効率
```

#### タイトルのカスタマイズ

**プレースホルダー対応:**

| タイトル | 利用可能なプレースホルダー |
|---------|-------------------------|
| `player-select` | なし |
| `category-select` | なし |
| `report-list` | なし |
| `report-detail` | `{id}` - 通報ID |

**カスタム例:**
```yaml
gui:
  title:
    player-select: "&#FF6B6Bプレイヤーを選択してください"
    category-select: "&#4ECDC4通報の理由を選んでください"
    report-list: "&#95E1D3通報管理システム"
    report-detail: "&#F38181通報 #{id} の詳細"
```

---

### 通報カテゴリ設定

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
```

#### パラメータ説明

| キー | 説明 | 必須 | 例 |
|------|------|------|---|
| `name` | カテゴリ名 | はい | `チート使用` |
| `material` | アイテムアイコン | はい | `BARRIER` |
| `description` | 説明文（複数行可） | はい | リスト形式 |

#### Material（アイテム）一覧

**推奨アイテム:**

| Material | アイコン | 用途 |
|----------|---------|------|
| `BARRIER` | バリアブロック | チート・不正 |
| `TNT` | TNT | 迷惑行為・荒らし |
| `WRITABLE_BOOK` | 本と羽ペン | 不適切発言 |
| `DIAMOND_PICKAXE` | ダイヤのツルハシ | 建築破壊 |
| `PAPER` | 紙 | その他 |
| `REDSTONE` | レッドストーン | バグ |
| `IRON_SWORD` | 鉄の剣 | PvP関連 |
| `CHEST` | チェスト | アイテム盗難 |

完全なMaterialリストは [Spigot Javadoc](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html) を参照。

#### カスタムカテゴリの追加

**例: PvP関連の通報カテゴリを追加**

```yaml
report-categories:
  # 既存のカテゴリ...

  - name: "PvP違反"
    material: IRON_SWORD
    description:
      - "&#94A3B8PvPエリア外での攻撃"
      - "&#94A3B8ログアウト逃げなど"

  - name: "アイテム盗難"
    material: CHEST
    description:
      - "&#94A3B8チェストからアイテムを盗む"
      - "&#94A3B8無断でアイテムを持ち去る"
```

#### カテゴリの削除・変更

**不要なカテゴリを削除:**
```yaml
report-categories:
  - name: "チート使用"
    material: BARRIER
    description:
      - "&#94A3B8不正なModやクライアント改造"

  # 「迷惑行為」カテゴリを削除
  # - name: "迷惑行為"
  #   material: TNT
  #   ...
```

**カテゴリ名を変更:**
```yaml
report-categories:
  - name: "ハッキング"  # 「チート使用」から変更
    material: BARRIER
    description:
      - "&#94A3B8不正なツールの使用"
```

---

## messages.yml

メッセージのカスタマイズファイルです。

### ファイル構造

```yaml
prefix:          # プラグインのプレフィックス
common:          # 共通メッセージ
report:          # 通報関連メッセージ
bugreport:       # バグレポート関連メッセージ
staff:           # スタッフ通知メッセージ
notification:    # 処理完了通知
admin:           # 管理コマンドメッセージ
status:          # ステータス表示
type:            # 通報タイプ表示
```

### カラーパレット

```yaml
# ベース: &#1E3A8A (ネイビーブルー)
# 成功: &#10B981 (ミントグリーン)
# エラー: &#EF4444 (ソフトレッド)
# 警告: &#F59E0B (アンバー)
# 情報: &#60A5FA (ライトブルー)
# グレー: &#94A3B8 (スレートグレー)
```

### プレースホルダー一覧

| プレースホルダー | 説明 | 使用箇所 |
|----------------|------|---------|
| `{prefix}` | プラグインのプレフィックス | すべてのメッセージ |
| `{player}` | プレイヤー名 | 通報成功、プレイヤー検索など |
| `{time}` | 残り時間（秒） | クールダウンメッセージ |
| `{id}` | 通報ID | 通知、詳細表示など |
| `{reporter}` | 通報者名 | スタッフ通知 |
| `{reported}` | 被通報者名 | スタッフ通知 |
| `{reason}` | 通報理由 | スタッフ通知 |
| `{description}` | バグの説明 | バグレポート通知 |
| `{location}` | 座標情報 | バグレポート通知 |
| `{page}` | 現在のページ | 一覧表示 |
| `{maxpage}` | 最大ページ数 | 一覧表示 |
| `{handler}` | 処理者名 | 詳細表示 |
| `{date}` | 日時 | 詳細表示 |
| `{note}` | 処理メモ | 詳細表示 |

### カスタマイズ例

**プレフィックスの変更:**
```yaml
# デフォルト（グラデーション）
prefix: "&#94A3B8[&#1E3A8AK&#2B4D9Bu&#3860ACm&#4573BDa&#5286CE-&#60A5FAR&#6DB8FFe&#7ACBFFp&#87DEFFo&#94F0FFr&#A1FFFFt&#94A3B8]&r"

# シンプル
prefix: "&8[&bKuma-Report&8]&r"

# カスタム
prefix: "&6[&c通報システム&6]&r"
```

**成功メッセージの変更:**
```yaml
# デフォルト
report:
  success: "{prefix} &#10B981&#F3F4F6{player}&#10B981を通報しました。スタッフが確認します。"

# カスタム
report:
  success: "{prefix} &a{player}さんへの通報を受け付けました。"
```

**クールダウンメッセージの変更:**
```yaml
# デフォルト
report:
  cooldown: "{prefix} &#F59E0B通報のクールダウン中です。あと&#FBBF24{time}秒&#F59E0Bお待ちください。"

# カスタム
report:
  cooldown: "{prefix} &e少し待ってください。あと&6{time}秒&eで再度通報できます。"
```

---

## ベストプラクティス

### 1. 定期的なバックアップ

```bash
# 毎日3時にバックアップ（cronで設定）
0 3 * * * cp /path/to/plugins/Kuma-Report/reports.db /backup/reports-$(date +\%Y\%m\%d).db
```

### 2. 適切なクールダウン設定

- プレイヤー通報: 60秒以上
- バグレポート: 120秒以上
- スパム対策として最低30秒は設定すること

### 3. Discord通知の活用

- 24時間監視体制の場合は必ず設定
- ロールメンションでスタッフに通知

### 4. 権限の適切な管理

- 一般プレイヤー: `use`, `bugreport` のみ
- モデレーター: `notify` を追加
- 管理者: `admin`, `bypass.cooldown` を追加

### 5. メッセージの一貫性

- サーバーの雰囲気に合わせてカスタマイズ
- カラーコードを統一
- 丁寧な言葉遣いを心がける

---

## 高度な設定

### 複数サーバーでの共有

複数のサーバーで通報データを共有する場合:

```yaml
# 共有ストレージにデータベースを配置
database:
  type: sqlite
  path: /mnt/shared/reports.db
```

**注意:**
- SQLiteは同時書き込みに弱い
- 大規模な場合はMySQLへの移行を検討（将来のバージョンで対応予定）

### 環境別設定

**開発環境:**
```yaml
cooldown:
  player-report: 5    # 短いクールダウン
  bug-report: 10

notifications:
  enable-staff-notification: true
  play-sound: false   # 音は無効

debug: true          # デバッグモードON
```

**本番環境:**
```yaml
cooldown:
  player-report: 60
  bug-report: 120

notifications:
  enable-staff-notification: true
  play-sound: true

discord:
  enabled: true      # Discord通知ON

debug: false
```

---

前のページ: [インストールガイド](Installation.md) | 次のページ: [コマンドリファレンス](Commands.md)
