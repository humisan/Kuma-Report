# Kuma-Report Wiki へようこそ

Kuma-Reportは、Minecraftサーバー向けの包括的な通報・バグ報告システムです。

## クイックリンク

- [インストールガイド](Installation.md)
- [設定ファイル詳細](Configuration.md)
- [コマンドリファレンス](Commands.md)
- [権限設定](Permissions.md)
- [Discord連携](Discord-Integration.md)
- [GUI使用ガイド](GUI-Guide.md)
- [トラブルシューティング](Troubleshooting.md)
- [API ドキュメント](API-Documentation.md)
- [よくある質問](FAQ.md)

## クイックスタート

### 5分で始める Kuma-Report

#### 1. インストール

```bash
# プラグインをダウンロード
# pluginsフォルダに配置
plugins/Kuma-Report-X.X.X.jar
```

#### 2. サーバーを起動

サーバーを起動すると、設定ファイルが自動生成されます。

```
plugins/Kuma-Report/
├── config.yml
├── messages.yml
└── reports.db
```

#### 3. 基本設定

`config.yml` を編集して、基本的な設定を行います。

```yaml
# クールダウン時間を設定
cooldown:
  player-report: 60
  bug-report: 120

# スタッフ通知を有効化
notifications:
  enable-staff-notification: true
  play-sound: true
```

#### 4. 権限設定

LuckPermsなどで権限を設定します。

```bash
# 一般プレイヤー
lp group default permission set kumareport.use true
lp group default permission set kumareport.bugreport true

# スタッフ（通知を受け取る）
lp group staff permission set kumareport.notify true

# 管理者（全権限）
lp group admin permission set kumareport.admin true
```

#### 5. 設定をリロード

```
/reportadmin reload
```

#### 6. 動作確認

プレイヤーとして通報を試してみましょう。

```
/report
```

## 主な機能

### プレイヤー通報
- GUI・コマンドの両方で通報可能
- カテゴリ別の通報理由選択
- クールダウン機能でスパム防止

### バグレポート
- 詳細な説明入力
- 自動座標記録
- スタッフへのリアルタイム通知

### 管理機能
- 通報一覧の閲覧
- 通報の承認・却下
- 統計情報の表示
- プレイヤー別検索

### Discord連携
- Webhook経由で通知
- ロールメンション対応
- カスタマイズ可能な通知内容

### マルチプラットフォーム対応
- Java版プレイヤー：チェストGUI
- Bedrock版プレイヤー：カスタムForm
- 自動判定で適切なUIを表示

## 基本的な使い方

### プレイヤー向け

#### 通報の送信

**方法1: GUI経由（推奨）**
```
/report
```
1. 通報するプレイヤーを選択
2. 理由のカテゴリを選択
3. 「その他」の場合は詳細をチャットで入力

**方法2: コマンド直接入力**
```
/report <プレイヤー名> <理由>
```

#### バグの報告

**方法1: GUI経由**
```
/bugreport
```
チャットでバグの説明を入力

**方法2: コマンド直接入力**
```
/bugreport <バグの説明>
```

### 管理者向け

#### 通報の確認

**GUIで確認（Java版）**
```
/reportadmin
```

**コマンドで確認**
```
/reportadmin list        # 一覧表示
/reportadmin view <ID>   # 詳細表示
```

#### 通報の処理

**承認**
```
/reportadmin accept <ID> [メモ]
```

**却下**
```
/reportadmin deny <ID> [メモ]
```

#### 統計情報

```
/reportadmin stats
```

## サポートが必要ですか？

- [トラブルシューティング](Troubleshooting.md) - 問題解決ガイド
- [よくある質問](FAQ.md) - FAQ
- [GitHub Issues](https://github.com/humisan/Kuma-Report/issues) - バグ報告・機能要望

## 最新情報

### v1.0.0 の新機能
- 初回リリース
- プレイヤー通報システム
- バグレポートシステム
- 管理者向けコマンド
- Discord Webhook連携
- Java版/Bedrock版両対応

## コミュニティ

- **作成者:** humisan
- **GitHub:** https://github.com/humisan/Kuma-Report
- **プロジェクト:** KumaEarth

---

次のページ: [インストールガイド](Installation.md)
