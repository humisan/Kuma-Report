# 権限設定

このページでは、Kuma-Reportの権限システムについて詳しく説明します。

## 目次

1. [権限一覧](#権限一覧)
2. [権限グループの推奨設定](#権限グループの推奨設定)
3. [LuckPermsでの設定](#luckpermsでの設定)
4. [GroupManagerでの設定](#groupmanagerでの設定)
5. [個別プレイヤーへの権限付与](#個別プレイヤーへの権限付与)

---

## 権限一覧

### 基本権限

| 権限ノード | 説明 | デフォルト | 推奨対象 |
|-----------|------|-----------|---------|
| `kumareport.use` | プレイヤー通報機能の使用 | `true` | 全プレイヤー |
| `kumareport.bugreport` | バグレポート機能の使用 | `true` | 全プレイヤー |
| `kumareport.admin` | 管理者コマンドの実行 | `op` | 管理者のみ |
| `kumareport.notify` | 通報時の通知を受け取る | `op` | スタッフ・管理者 |
| `kumareport.bypass.cooldown` | クールダウンをバイパス | `op` | 管理者のみ |

### 権限の詳細説明

#### kumareport.use

**概要:**
プレイヤー通報機能を使用できる権限です。

**対応コマンド:**
- `/report`
- `/rep`

**機能:**
- GUIでプレイヤーを選択して通報
- コマンドで直接通報

**推奨設定:**
全プレイヤーに付与（`default: true`）

---

#### kumareport.bugreport

**概要:**
バグレポート機能を使用できる権限です。

**対応コマンド:**
- `/bugreport`
- `/bug`
- `/breport`

**機能:**
- GUIまたはコマンドでバグを報告
- 座標情報の自動記録

**推奨設定:**
全プレイヤーに付与（`default: true`）

---

#### kumareport.admin

**概要:**
管理者向けコマンドをすべて実行できる権限です。

**対応コマンド:**
- `/reportadmin` （すべてのサブコマンド）
- `/radmin`
- `/ra`

**機能:**
- 通報一覧の閲覧
- 通報詳細の表示
- 通報の承認・却下
- プレイヤー検索
- 統計情報の表示
- 設定のリロード

**推奨設定:**
管理者のみ（`default: op`）

**注意:**
この権限を持つプレイヤーは、すべての通報データにアクセスできます。
信頼できるスタッフのみに付与してください。

---

#### kumareport.notify

**概要:**
新しい通報やバグレポートが送信された際に通知を受け取る権限です。

**機能:**
- チャット通知の受信
- 通知音の再生（設定で有効化されている場合）

**通知内容:**
```
[Kuma-Report] 新しい通報が届きました
ID: #1 | 通報者: PlayerA | 被通報者: PlayerB
理由: チート使用
確認: /reportadmin view 1
```

**推奨設定:**
スタッフ・モデレーター・管理者（`default: op`）

**注意:**
通知を受け取りたくないスタッフがいる場合は、個別に権限を削除してください。

---

#### kumareport.bypass.cooldown

**概要:**
通報とバグレポートのクールダウンを無視できる権限です。

**機能:**
- プレイヤー通報のクールダウンをバイパス
- バグレポートのクールダウンをバイパス

**推奨設定:**
管理者のみ（`default: op`）

**用途:**
- テスト目的での連続通報
- 緊急時の複数プレイヤーの通報

**注意:**
一般プレイヤーに付与すると、スパム通報が可能になるため推奨しません。

---

## 権限グループの推奨設定

### グループ構成の例

```
default (デフォルト)
  └ vip (VIP)
    └ helper (ヘルパー)
      └ moderator (モデレーター)
        └ admin (管理者)
          └ owner (オーナー)
```

### default（一般プレイヤー）

**付与する権限:**
```
kumareport.use
kumareport.bugreport
```

**説明:**
通報とバグレポートの基本機能のみ使用可能。

---

### vip（VIP）

**付与する権限:**
```
# defaultから継承
kumareport.use
kumareport.bugreport
```

**説明:**
一般プレイヤーと同じ。特別な権限は不要。

---

### helper（ヘルパー）

**付与する権限:**
```
# defaultから継承
kumareport.use
kumareport.bugreport

# 追加権限
kumareport.notify
```

**説明:**
通報の通知を受け取り、管理者に報告する役割。
管理コマンドは使用できない。

---

### moderator（モデレーター）

**付与する権限:**
```
# helperから継承
kumareport.use
kumareport.bugreport
kumareport.notify

# 追加権限
kumareport.admin
```

**説明:**
通報の管理が可能。承認・却下などの処理を実行できる。

---

### admin（管理者）

**付与する権限:**
```
# moderatorから継承
kumareport.use
kumareport.bugreport
kumareport.notify
kumareport.admin

# 追加権限
kumareport.bypass.cooldown
```

**説明:**
すべての機能を使用可能。クールダウンもバイパスできる。

---

### owner（オーナー）

**付与する権限:**
```
kumareport.*  # すべての権限
```

**説明:**
ワイルドカードですべての権限を付与。

---

## LuckPermsでの設定

### インストール

```bash
# LuckPermsをダウンロード
# https://luckperms.net/download

# pluginsフォルダに配置
plugins/LuckPerms-Bukkit-X.X.X.jar

# サーバーを再起動
```

### グループの作成

```bash
# グループを作成
lp creategroup default
lp creategroup helper
lp creategroup moderator
lp creategroup admin
```

### 権限の付与

#### default グループ

```bash
# 基本権限を付与
lp group default permission set kumareport.use true
lp group default permission set kumareport.bugreport true

# グループを設定
lp user <プレイヤー名> parent set default
```

#### helper グループ

```bash
# defaultを継承
lp group helper parent set default

# 通知権限を追加
lp group helper permission set kumareport.notify true

# グループを設定
lp user <プレイヤー名> parent set helper
```

#### moderator グループ

```bash
# helperを継承
lp group moderator parent set helper

# 管理権限を追加
lp group moderator permission set kumareport.admin true

# グループを設定
lp user <プレイヤー名> parent set moderator
```

#### admin グループ

```bash
# moderatorを継承
lp group admin parent set moderator

# クールダウンバイパス権限を追加
lp group admin permission set kumareport.bypass.cooldown true

# グループを設定
lp user <プレイヤー名> parent set admin
```

### 権限の確認

```bash
# プレイヤーの権限を確認
lp user <プレイヤー名> permission check kumareport.use
lp user <プレイヤー名> permission check kumareport.admin

# グループの権限を確認
lp group moderator permission info
```

### 権限の削除

```bash
# 個別権限を削除
lp group <グループ名> permission unset kumareport.notify

# プレイヤーから権限を削除
lp user <プレイヤー名> permission unset kumareport.use
```

---

## GroupManagerでの設定

### groups.yml の編集

`plugins/GroupManager/worlds/<ワールド名>/groups.yml` を編集:

```yaml
groups:
  default:
    default: true
    permissions:
      - kumareport.use
      - kumareport.bugreport
    inheritance: []
    info:
      prefix: '&7'
      suffix: ''
      build: true

  helper:
    default: false
    permissions:
      - kumareport.notify
    inheritance:
      - default
    info:
      prefix: '&e[Helper]&f '
      suffix: ''
      build: true

  moderator:
    default: false
    permissions:
      - kumareport.admin
    inheritance:
      - helper
    info:
      prefix: '&b[Mod]&f '
      suffix: ''
      build: true

  admin:
    default: false
    permissions:
      - kumareport.bypass.cooldown
      - kumareport.*
    inheritance:
      - moderator
    info:
      prefix: '&c[Admin]&f '
      suffix: ''
      build: true
```

### グループの変更

```bash
# プレイヤーにグループを設定
/manuadd <プレイヤー名> <グループ名>

# 例
/manuadd Player123 helper
/manuadd AdminUser admin
```

### 個別権限の付与

```bash
# プレイヤーに権限を追加
/manuaddp <プレイヤー名> kumareport.admin

# プレイヤーから権限を削除
/manudelp <プレイヤー名> kumareport.notify
```

---

## 個別プレイヤーへの権限付与

### LuckPermsの場合

```bash
# 通知権限を個別に付与（グループに関係なく）
lp user Player123 permission set kumareport.notify true

# クールダウンバイパスを個別に付与
lp user AdminUser permission set kumareport.bypass.cooldown true

# 期間限定で権限を付与（30日間）
lp user Player123 permission settemp kumareport.admin true 30d

# 権限を削除
lp user Player123 permission unset kumareport.notify
```

### GroupManagerの場合

```bash
# 個別に権限を付与
/manuaddp Player123 kumareport.notify

# 個別に権限を削除
/manudelp Player123 kumareport.notify
```

---

## 権限のトラブルシューティング

### コマンドが実行できない

**症状:**
```
[Kuma-Report] 権限がありません。
```

**確認:**
```bash
# LuckPermsの場合
lp user <プレイヤー名> permission check kumareport.use

# GroupManagerの場合
/manulistp <プレイヤー名>
```

**解決策:**
必要な権限が付与されているか確認し、ない場合は付与する。

---

### 通知が受け取れない

**症状:**
通報があってもチャット通知が表示されない。

**確認:**
```bash
# kumareport.notify 権限があるか確認
lp user <プレイヤー名> permission check kumareport.notify
```

**解決策:**
1. `kumareport.notify` 権限を付与
2. `config.yml` で `enable-staff-notification: true` になっているか確認

---

### クールダウンが効かない

**症状:**
クールダウン時間を無視して連続で通報できてしまう。

**確認:**
```bash
# kumareport.bypass.cooldown 権限があるか確認
lp user <プレイヤー名> permission check kumareport.bypass.cooldown
```

**解決策:**
不要な場合は `kumareport.bypass.cooldown` 権限を削除する。

---

## ベストプラクティス

### 1. 最小権限の原則

必要最小限の権限のみを付与してください。

**悪い例:**
```bash
# 一般プレイヤーに管理権限を付与
lp group default permission set kumareport.admin true
```

**良い例:**
```bash
# 管理者のみに管理権限を付与
lp group admin permission set kumareport.admin true
```

### 2. グループ継承の活用

権限の重複を避けるため、グループ継承を活用してください。

```bash
# moderator が helper を継承
lp group moderator parent set helper
```

### 3. 定期的な権限の見直し

- 退任したスタッフの権限を削除
- 不要な権限がないか確認
- グループ構成の最適化

### 4. テストアカウントでの確認

新しい権限設定を本番環境に適用する前に、テストアカウントで動作確認を行ってください。

```bash
# テスト用グループを作成
lp creategroup test

# 権限を設定
lp group test permission set kumareport.admin true

# テストアカウントに適用
lp user TestUser parent set test
```

---

前のページ: [コマンドリファレンス](Commands.md) | 次のページ: [Discord連携](Discord-Integration.md)
