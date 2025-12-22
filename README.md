# Kuma Report

Minecraft plugin for report management.

## Release Process

このプロジェクトは、GitHub Releasesへの自動JAR配布を設定しています。

### リリースの作成方法

1. GitHubのリポジトリページで「Releases」セクションに移動
2. 「Draft a new release」をクリック
3. タグを作成（例：`v1.0.0`）
4. リリースタイトルと説明を入力
5. 「Publish release」をクリック

リリースを公開すると、GitHub Actionsが自動的に:
- プロジェクトをビルド
- シェーディングされたJARファイルを生成
- JARファイルをリリースに自動アップロード

ビルドされたJARファイルは `Kuma-Report-1.0-shaded.jar` という名前でリリースに添付されます。

## Development

### Requirements
- Java 21
- Gradle 8.x

### Building
```bash
./gradlew build
```

ビルドされたJARは `build/libs/` ディレクトリに生成されます。
