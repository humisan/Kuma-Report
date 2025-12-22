# Kuma Report

Minecraft plugin for report management.

## Release Process

このプロジェクトは、GitHub Releasesへの自動JAR配布を設定しています。

### 自動リリース

プルリクエストがmainブランチにマージされると、GitHub Actionsが自動的に:
- プロジェクトをビルド
- シェーディングされたJARファイルを生成
- 新しいリリースを作成
- JARファイルをリリースに自動アップロード

リリースは自動的に `v{version}-{build_number}` の形式でタグ付けされます（例：`v1.0-1`, `v1.0-2`）。

### 手動での動作確認

プルリクエストを作成すると、ビルドジョブが自動的に実行され、JARファイルが正しくビルドされることを確認できます。

## Development

### Requirements
- Java 21
- Gradle 8.x

### Building
```bash
./gradlew build
```

ビルドされたJARは `build/libs/` ディレクトリに生成されます。
