# インストールガイド

このガイドでは、Kuma-Reportプラグインのインストール手順を詳しく説明します。

## 目次

1. [必須要件](#必須要件)
2. [オプション要件](#オプション要件)
3. [インストール手順](#インストール手順)
4. [初回起動](#初回起動)
5. [基本設定](#基本設定)
6. [動作確認](#動作確認)
7. [アップグレード](#アップグレード)

---

## 必須要件

### サーバー要件

| 項目 | 要件 |
|------|------|
| Minecraftバージョン | 1.21以降 |
| サーバーソフトウェア | Paper / Spigot / Purpur |
| Javaバージョン | Java 17以降 |
| API Version | 1.21 |

### 推奨環境

- **RAM:** 最低2GB（サーバー全体で4GB以上推奨）
- **CPU:** マルチコア推奨
- **ストレージ:** 50MB以上の空き容量

### 互換性チェック

サーバーのバージョンを確認：

```bash
# サーバーコンソールで
/version

# または
java -version
```

**出力例:**
```
This server is running Paper version git-Paper-387 (MC: 1.21) (Implementing API version 1.21-R0.1-SNAPSHOT)
You are running the latest version
```

---

## オプション要件

### Bedrock版プレイヤー対応

Bedrock版（統合版）プレイヤーにも対応させる場合:

- [Geyser](https://geysermc.org/) - Java版とBedrock版の橋渡し
- [Floodgate](https://github.com/GeyserMC/Floodgate) - Bedrock版プレイヤーの認証

**インストール方法:**

1. Geyserプラグインをダウンロード
   ```
   https://geysermc.org/download
   ```

2. Floodgateプラグインをダウンロード
   ```
   https://github.com/GeyserMC/Floodgate/releases
   ```

3. pluginsフォルダに配置
   ```
   plugins/
   ├── Geyser-Spigot.jar
   ├── floodgate-bukkit.jar
   └── Kuma-Report.jar
   ```

### 権限管理プラグイン

権限管理には以下のプラグインを推奨:

- [LuckPerms](https://luckperms.net/) - 高機能な権限管理
- [GroupManager](https://github.com/ElgarL/GroupManager) - シンプルな権限管理

---

## インストール手順

### ステップ1: プラグインのダウンロード

1. **GitHubリリースページにアクセス**
   ```
   https://github.com/humisan/Kuma-Report/releases
   ```

2. **最新版をダウンロード**
   - `Kuma-Report-X.X.X.jar` をクリックしてダウンロード

3. **ファイルを確認**
   - ダウンロードしたファイルのサイズを確認（約500KB～2MB）
   - ファイル名が正しいか確認

### ステップ2: サーバーへの配置

1. **サーバーを停止**
   ```bash
   # サーバーコンソールで
   stop
   ```

2. **pluginsフォルダに配置**
   ```bash
   # Windowsの場合
   copy Kuma-Report-X.X.X.jar server\plugins\

   # Linux/Macの場合
   cp Kuma-Report-X.X.X.jar /path/to/server/plugins/
   ```

3. **ファイル権限を設定（Linux/Macのみ）**
   ```bash
   chmod 644 plugins/Kuma-Report-X.X.X.jar
   ```

### ステップ3: サーバーの起動

1. **サーバーを起動**
   ```bash
   # Windowsの場合
   start.bat

   # Linux/Macの場合
   ./start.sh
   ```

2. **起動ログを確認**
   ```
   [Kuma-Report] プラグインを有効化しています...
   [Kuma-Report] データベースを初期化しています...
   [Kuma-Report] 設定ファイルをロードしました
   [Kuma-Report] v1.0.0 を有効化しました
   ```

3. **エラーがないか確認**
   ```bash
   # ログファイルを確認
   tail -f logs/latest.log
   ```

---

## 初回起動

### 自動生成されるファイル

初回起動時に以下のファイルが自動生成されます:

```
plugins/Kuma-Report/
├── config.yml           # メイン設定ファイル
├── messages.yml         # メッセージ設定
└── reports.db          # SQLiteデータベース（通報データ）
```

### デフォルト設定の確認

1. **config.yml を開く**
   ```bash
   # Windowsの場合
   notepad plugins\Kuma-Report\config.yml

   # Linux/Macの場合
   nano plugins/Kuma-Report/config.yml
   ```

2. **デフォルト値を確認**
   - クールダウン時間
   - 通知設定
   - GUI設定
   - 通報カテゴリ

---

## 基本設定

### 1. クールダウン時間の設定

`config.yml` を編集:

```yaml
cooldown:
  player-report: 60      # プレイヤー通報（推奨: 30-120秒）
  bug-report: 120        # バグレポート（推奨: 60-300秒）
```

**推奨値:**
- 小規模サーバー（10-50人）: 30-60秒
- 中規模サーバー（50-200人）: 60-120秒
- 大規模サーバー（200人以上）: 120-300秒

### 2. 通知設定

```yaml
notifications:
  enable-staff-notification: true    # スタッフ通知を有効化
  play-sound: true                   # 通知音を再生
  sound-volume: 1.0                  # 音量（0.0-1.0）
  sound-pitch: 1.2                   # ピッチ（0.5-2.0）
  sound-type: BLOCK_NOTE_BLOCK_PLING # サウンドタイプ
```

**サウンドタイプの選択肢:**
- `BLOCK_NOTE_BLOCK_PLING` - 優しいピン音（推奨）
- `ENTITY_EXPERIENCE_ORB_PICKUP` - 経験値取得音
- `BLOCK_ANVIL_LAND` - アンビル着地音（重厚）
- `ENTITY_PLAYER_LEVELUP` - レベルアップ音

### 3. GUI設定

```yaml
gui:
  items-per-page: 45    # 1ページの表示数（最大45）
```

**推奨値:**
- 通常: 45（最大効率）
- カスタム: 27, 36, 45のいずれか（チェストのサイズ）

### 4. 権限の設定

**LuckPermsの場合:**

```bash
# 一般プレイヤーグループ
lp group default permission set kumareport.use true
lp group default permission set kumareport.bugreport true

# モデレーターグループ（通知を受け取る）
lp group moderator permission set kumareport.use true
lp group moderator permission set kumareport.bugreport true
lp group moderator permission set kumareport.notify true

# 管理者グループ（全権限）
lp group admin permission set kumareport.admin true
lp group admin permission set kumareport.notify true
lp group admin permission set kumareport.bypass.cooldown true
```

**GroupManagerの場合:**

`groups.yml` を編集:

```yaml
groups:
  default:
    permissions:
    - kumareport.use
    - kumareport.bugreport

  moderator:
    permissions:
    - kumareport.notify

  admin:
    permissions:
    - kumareport.admin
    - kumareport.bypass.cooldown
```

### 5. 設定のリロード

設定を変更したら、リロードします:

```
/reportadmin reload
```

---

## 動作確認

### テスト手順

#### 1. プレイヤー通報のテスト

1. **サーバーに参加**
   - テスト用アカウントでログイン

2. **通報コマンドを実行**
   ```
   /report
   ```

3. **GUIが開くことを確認**
   - オンラインプレイヤー一覧が表示される
   - クリックで選択できる

4. **カテゴリ選択**
   - 通報理由のカテゴリが表示される
   - 各アイテムに説明が表示される

5. **通報完了メッセージを確認**
   ```
   [Kuma-Report] Player123を通報しました。スタッフが確認します。
   ```

#### 2. スタッフ通知のテスト

1. **管理者アカウントでログイン**
   - `kumareport.notify` 権限を持つアカウント

2. **他のプレイヤーから通報を送信**

3. **通知を確認**
   - チャット通知が表示される
   - 通知音が再生される

   ```
   [Kuma-Report] 新しい通報が届きました
   ID: #1 | 通報者: PlayerA | 被通報者: PlayerB
   理由: チート使用
   確認: /reportadmin view 1
   ```

#### 3. 管理コマンドのテスト

1. **通報一覧を表示**
   ```
   /reportadmin list
   ```

2. **通報詳細を表示**
   ```
   /reportadmin view 1
   ```

3. **統計情報を表示**
   ```
   /reportadmin stats
   ```

#### 4. バグレポートのテスト

1. **バグレポートを送信**
   ```
   /bugreport ブロックを置くと消えてしまう
   ```

2. **完了メッセージを確認**
   ```
   [Kuma-Report] バグレポートを送信しました。ご報告ありがとうございます。
   ```

#### 5. クールダウンのテスト

1. **通報を送信**
   ```
   /report PlayerB チート
   ```

2. **すぐにもう一度送信**
   ```
   /report PlayerB チート
   ```

3. **クールダウンメッセージを確認**
   ```
   [Kuma-Report] 通報のクールダウン中です。あと58秒お待ちください。
   ```

---

## アップグレード

### バージョンアップ手順

#### ステップ1: バックアップ

```bash
# 設定ファイルとデータベースをバックアップ
cp -r plugins/Kuma-Report plugins/Kuma-Report.backup

# または日付付きバックアップ
cp -r plugins/Kuma-Report plugins/Kuma-Report.backup-$(date +%Y%m%d)
```

#### ステップ2: サーバー停止

```bash
# サーバーコンソールで
stop
```

#### ステップ3: 旧バージョンの削除

```bash
# 古いJARファイルを削除
rm plugins/Kuma-Report-*.jar

# 注意: 設定ファイルとデータベースは削除しないこと！
```

#### ステップ4: 新バージョンの配置

```bash
# 新しいJARファイルをコピー
cp Kuma-Report-X.X.X.jar plugins/
```

#### ステップ5: サーバー起動

```bash
# サーバーを起動
./start.sh
```

#### ステップ6: 動作確認

1. **バージョンの確認**
   ```
   /reportadmin
   ```
   または
   ```
   /plugins
   ```

2. **既存データの確認**
   ```
   /reportadmin list
   ```
   既存の通報データが表示されるか確認

3. **新機能のテスト**
   - 新しいコマンド
   - 新しいGUI
   - 新しい設定項目

### トラブルシューティング

アップグレード時に問題が発生した場合:

1. **バックアップから復元**
   ```bash
   # サーバーを停止
   stop

   # バックアップを復元
   rm -r plugins/Kuma-Report
   cp -r plugins/Kuma-Report.backup plugins/Kuma-Report

   # 旧バージョンのJARを戻す
   # サーバーを起動
   ```

2. **ログを確認**
   ```bash
   tail -n 100 logs/latest.log
   ```

3. **設定ファイルの互換性**
   - 新バージョンで設定項目が変更されている場合がある
   - `config.yml.new` が生成されている場合、そちらを参照

---

## 次のステップ

インストールが完了したら、以下のページを参照してください:

- [設定ファイル詳細](Configuration.md) - 詳細な設定方法
- [コマンドリファレンス](Commands.md) - 全コマンドの使い方
- [Discord連携](Discord-Integration.md) - Discord通知の設定

---

前のページ: [ホーム](Home.md) | 次のページ: [設定ファイル詳細](Configuration.md)
