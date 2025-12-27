# Discord連携

このページでは、Discordへの通知機能の設定方法を詳しく説明します。

## 目次

1. [概要](#概要)
2. [Webhook URLの取得](#webhook-urlの取得)
3. [基本設定](#基本設定)
4. [ロールメンション](#ロールメンション)
5. [通知内容](#通知内容)
6. [カスタマイズ](#カスタマイズ)
7. [トラブルシューティング](#トラブルシューティング)

---

## 概要

Kuma-ReportはDiscord Webhookを使用して、通報やバグレポートをDiscordチャンネルに自動送信できます。

### 機能

- **リアルタイム通知** - 通報・バグレポートが送信されると即座にDiscordへ通知
- **詳細な情報** - 通報ID、通報者、被通報者、理由などを含む
- **ロールメンション** - 特定のロールにメンション可能
- **カスタマイズ可能** - Embedメッセージで見やすく表示

### メリット

- **24時間監視** - サーバーにログインしていなくても通知を受け取れる
- **チーム連携** - 複数のスタッフで通報を共有
- **記録保持** - Discordに通報の履歴が残る
- **モバイル対応** - スマートフォンでも確認可能

---

## Webhook URLの取得

### ステップ1: Discordサーバーでチャンネルを作成

1. Discordサーバーを開く
2. 新しいテキストチャンネルを作成
   - 例: `#server-reports` または `#通報通知`

3. チャンネルの権限を設定
   - スタッフのみが閲覧できるように設定することを推奨

### ステップ2: Webhookを作成

1. **チャンネル設定を開く**
   - チャンネル名の横の⚙️アイコンをクリック

2. **連携サービスタブを開く**
   - 左メニューの「連携サービス」をクリック

3. **ウェブフックを作成**
   - 「ウェブフック」セクションで「ウェブフックを作成」をクリック

4. **Webhookの設定**
   - **名前**: `Kuma-Report` または任意の名前
   - **アイコン**: お好みの画像をアップロード（オプション）
   - **チャンネル**: 通知を送信するチャンネルを選択

5. **Webhook URLをコピー**
   - 「ウェブフックURLをコピー」ボタンをクリック
   - URLは以下の形式になります:
     ```
     https://discord.com/api/webhooks/1234567890/abcdefghijklmnopqrstuvwxyz
     ```

**重要:** Webhook URLは秘密情報です。他人に共有しないでください。

### ステップ3: URLを安全に保管

- Webhook URLを第三者に知られると、誰でもそのチャンネルにメッセージを送信できます
- 必要に応じて、Webhookを削除・再作成できます

---

## 基本設定

### config.yml の編集

`plugins/Kuma-Report/config.yml` を開き、以下のように設定します:

```yaml
discord:
  enabled: true  # Discord連携を有効化
  webhook-url: "https://discord.com/api/webhooks/1234567890/abcdefghijklmnopqrstuvwxyz"
  mention-role-id: ""  # ロールメンションは後で設定
```

### 設定のリロード

```
/reportadmin reload
```

### 動作確認

1. プレイヤーとして通報を送信
   ```
   /report TestPlayer テスト通報
   ```

2. Discordチャンネルに通知が届くことを確認

**通知例:**
```
🚨 新しい通報

ID: #1
通報者: PlayerA
被通報者: TestPlayer
理由: テスト通報
種類: その他
日時: 2025-12-27 14:30:00
```

---

## ロールメンション

特定のロールにメンション通知を送ることができます。

### ステップ1: Discord開発者モードを有効化

1. **Discordの設定を開く**
   - Discordアプリで歯車アイコン（ユーザー設定）をクリック

2. **詳細設定タブを開く**
   - 左メニューの「詳細設定」をクリック

3. **開発者モードをON**
   - 「開発者モード」のトグルをONにする

### ステップ2: ロールIDを取得

1. **サーバー設定を開く**
   - サーバー名を右クリック → 「サーバー設定」

2. **ロールタブを開く**
   - 左メニューの「ロール」をクリック

3. **ロールIDをコピー**
   - メンションしたいロール（例: `@Moderator`）を右クリック
   - 「IDをコピー」をクリック
   - IDは18桁の数字です（例: `987654321098765432`）

### ステップ3: config.yml に設定

```yaml
discord:
  enabled: true
  webhook-url: "https://discord.com/api/webhooks/1234567890/abcdefghijklmnopqrstuvwxyz"
  mention-role-id: "987654321098765432"  # ここにロールIDを貼り付け
```

### ステップ4: 設定をリロード

```
/reportadmin reload
```

### 動作確認

通報を送信すると、Discordでロールがメンションされます:

```
<@&987654321098765432>
🚨 新しい通報

ID: #2
通報者: PlayerB
被通報者: Cheater123
理由: チート使用
種類: チート
日時: 2025-12-27 15:00:00
```

---

## 通知内容

### プレイヤー通報の通知

```
<@&ロールID> (ロールメンション設定時のみ)
🚨 新しい通報

ID: #123
通報者: PlayerA
被通報者: PlayerB
理由: X-Rayを使用している
種類: チート
日時: 2025-12-27 14:30:00
サーバー: YourServerName
```

### バグレポートの通知

```
<@&ロールID> (ロールメンション設定時のみ)
🐛 新しいバグレポート

ID: #45
報告者: PlayerC
説明: ブロックを置くと消えてしまう
場所: world: X=123, Y=64, Z=-456
日時: 2025-12-27 15:00:00
サーバー: YourServerName
```

### 通知の要素

| 要素 | プレイヤー通報 | バグレポート |
|------|--------------|------------|
| ロールメンション | ✓ | ✓ |
| 絵文字アイコン | 🚨 | 🐛 |
| ID | ✓ | ✓ |
| 通報者/報告者 | ✓ | ✓ |
| 被通報者 | ✓ | - |
| 理由/説明 | ✓ | ✓ |
| 種類 | ✓ | - |
| 場所 | - | ✓ |
| 日時 | ✓ | ✓ |
| サーバー名 | ✓ | ✓ |

---

## カスタマイズ

Discord Webhookの通知内容は、プラグインのソースコードで定義されています。

### 通知をカスタマイズする方法

カスタマイズするには、`DiscordWebhookManager.java` を編集する必要があります。

**ファイル位置:**
```
src/main/java/lol/hanyuu/kumaReport/manager/DiscordWebhookManager.java
```

### 例1: 通知の色を変更

Discord Embedメッセージの色を変更できます:

```java
// デフォルトの色（赤）
embed.setColor(Color.RED);

// カスタム色（青）
embed.setColor(Color.BLUE);

// カスタム色（16進数）
embed.setColor(new Color(0x60A5FA));
```

### 例2: フィールドを追加

追加の情報をフィールドとして表示できます:

```java
embed.addField("サーバー", "YourServerName", true);
embed.addField("優先度", "高", true);
```

### 例3: 画像を追加

Embedメッセージにサムネイル画像を追加:

```java
embed.setThumbnail("https://example.com/icon.png");
```

### 例4: フッターを追加

```java
embed.setFooter("Kuma-Report v1.0.0", null);
```

---

## トラブルシューティング

### 通知が届かない

#### 原因1: Webhook URLが間違っている

**確認:**
```yaml
discord:
  webhook-url: "https://discord.com/api/webhooks/..."
```

**解決策:**
- Webhook URLが正しいか確認
- `https://discord.com/api/webhooks/` で始まっているか確認
- URLにスペースや改行が含まれていないか確認

#### 原因2: Discord連携が無効になっている

**確認:**
```yaml
discord:
  enabled: false  # ← これが false になっていないか確認
```

**解決策:**
```yaml
discord:
  enabled: true
```

#### 原因3: ネットワークの問題

**確認:**
サーバーログを確認:
```
[Kuma-Report] Discord通知の送信に失敗しました: ...
```

**解決策:**
- サーバーからインターネットに接続できるか確認
- ファイアウォールでDiscord APIへのアクセスが許可されているか確認
- プロキシ設定が必要な場合は設定

#### 原因4: Webhookが削除されている

**確認:**
Discordのチャンネル設定でWebhookが存在するか確認

**解決策:**
新しいWebhookを作成し、URLを更新

---

### ロールメンションが機能しない

#### 原因1: ロールIDが間違っている

**確認:**
```yaml
discord:
  mention-role-id: "123456789012345678"  # ← 18桁の数字か確認
```

**解決策:**
- 開発者モードが有効になっているか確認
- ロールIDを再度コピーして貼り付け

#### 原因2: ロールがメンション可能ではない

**確認:**
Discordのロール設定で「このロールについてメンションを許可する」がONになっているか

**解決策:**
1. サーバー設定 → ロール
2. 対象のロールを選択
3. 「このロールについてメンションを許可する」をON

---

### Webhook URLのテスト

curlコマンドでWebhookをテストできます:

```bash
curl -X POST "YOUR_WEBHOOK_URL" \
  -H "Content-Type: application/json" \
  -d '{"content": "テストメッセージ"}'
```

**成功時:**
Discordチャンネルに「テストメッセージ」が表示されます。

**エラー時:**
```
{"message": "Invalid Webhook Token", "code": 50027}
```

この場合、Webhook URLが間違っています。

---

### 複数のWebhookを使用

通報とバグレポートで別々のチャンネルに通知を送ることも可能です。

**config.yml:**
```yaml
discord:
  enabled: true
  webhook-url: "https://discord.com/api/webhooks/通報用"
  bugreport-webhook-url: "https://discord.com/api/webhooks/バグレポート用"
  mention-role-id: "987654321098765432"
```

**注意:** この機能は現在サポートされていません。将来のバージョンで実装予定です。

---

## セキュリティのベストプラクティス

### 1. Webhook URLを秘密にする

- config.ymlをGitリポジトリにコミットしない
- `.gitignore` に追加
  ```
  plugins/Kuma-Report/config.yml
  ```

### 2. チャンネルの権限を制限

- 通知チャンネルはスタッフのみが閲覧できるように設定
- 一般プレイヤーには非公開

### 3. Webhookの定期的な更新

- セキュリティ上の理由で、定期的にWebhookを再作成することを推奨
- 古いWebhookは削除

### 4. ロールの適切な設定

- メンション対象のロールは必要最小限に
- スパム防止のため、ロールの権限を適切に設定

---

## 高度な設定

### Embed メッセージのカスタマイズ

詳細なカスタマイズは、`DiscordWebhookManager.java` を編集してください。

**例: カスタムEmbed**

```java
EmbedObject embed = new EmbedObject();
embed.setTitle("🚨 緊急通報");
embed.setDescription("重大な違反が報告されました");
embed.setColor(Color.RED);
embed.addField("通報者", report.getReporterName(), true);
embed.addField("被通報者", report.getReportedName(), true);
embed.addField("理由", report.getReason(), false);
embed.setFooter("Kuma-Report", "https://example.com/icon.png");
embed.setTimestamp(Instant.now());
```

### Webhook送信のリトライ

ネットワークエラーが発生した場合、自動的にリトライすることができます。

**実装例:**

```java
for (int i = 0; i < 3; i++) {
    try {
        webhook.execute();
        break;  // 成功
    } catch (IOException e) {
        if (i == 2) {
            plugin.getLogger().severe("Discord通知の送信に失敗しました: " + e.getMessage());
        }
    }
}
```

---

前のページ: [権限設定](Permissions.md) | 次のページ: [GUIガイド](GUI-Guide.md)
