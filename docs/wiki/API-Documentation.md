# API ドキュメント

このページでは、Kuma-Reportのプラグイン API について説明します。他のプラグインから Kuma-Report の機能を利用する方法を解説します。

## 目次

1. [概要](#概要)
2. [プラグインの取得](#プラグインの取得)
3. [通報の作成](#通報の作成)
4. [バグレポートの作成](#バグレポートの作成)
5. [通報の取得](#通報の取得)
6. [通報の更新](#通報の更新)
7. [統計情報の取得](#統計情報の取得)
8. [イベントシステム](#イベントシステム)
9. [使用例](#使用例)

---

## 概要

Kuma-Report は、他のプラグインから機能を利用できるように API を提供しています。

### 対応バージョン

- Kuma-Report 1.0.0以降

### 依存関係の追加

#### Maven

```xml
<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>

<dependency>
    <groupId>com.github.humisan</groupId>
    <artifactId>Kuma-Report</artifactId>
    <version>1.0.0</version>
    <scope>provided</scope>
</dependency>
```

#### Gradle

```gradle
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    compileOnly 'com.github.humisan:Kuma-Report:1.0.0'
}
```

#### plugin.yml

```yaml
depend: [Kuma-Report]
# または
softdepend: [Kuma-Report]
```

---

## プラグインの取得

### KumaReportインスタンスの取得

```java
import lol.hanyuu.kumaReport.KumaReport;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class YourPlugin extends JavaPlugin {
    private KumaReport kumaReport;

    @Override
    public void onEnable() {
        // プラグインインスタンスを取得
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Kuma-Report");

        if (plugin instanceof KumaReport) {
            kumaReport = (KumaReport) plugin;
            getLogger().info("Kuma-Report API を有効化しました");
        } else {
            getLogger().warning("Kuma-Report が見つかりません");
            // プラグインを無効化するか、機能を制限する
        }
    }

    public KumaReport getKumaReport() {
        return kumaReport;
    }
}
```

---

## 通報の作成

### プレイヤー通報を作成

```java
import lol.hanyuu.kumaReport.model.Report;
import lol.hanyuu.kumaReport.model.ReportType;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.UUID;

public void createReport(Player reporter, Player reported, String reason) {
    KumaReport kumaReport = getKumaReport();

    // 通報オブジェクトを作成
    Report report = new Report(
        reporter.getUniqueId(),      // 通報者UUID
        reporter.getName(),           // 通報者名
        reported.getUniqueId(),       // 被通報者UUID
        reported.getName(),           // 被通報者名
        reason,                       // 通報理由
        ReportType.OTHER              // 通報タイプ
    );

    // 非同期でデータベースに保存
    Bukkit.getScheduler().runTaskAsynchronously(yourPlugin, () -> {
        try {
            // データベースに保存
            int reportId = kumaReport.getDatabaseManager()
                .getReportDAO()
                .createReport(report);

            // 成功メッセージ
            reporter.sendMessage("通報を送信しました（ID: #" + reportId + "）");

            // スタッフに通知（メインスレッドで実行）
            Bukkit.getScheduler().runTask(yourPlugin, () -> {
                notifyStaff(reportId, reporter, reported, reason);
            });

        } catch (SQLException e) {
            getLogger().severe("通報の保存に失敗: " + e.getMessage());
            reporter.sendMessage("通報の送信に失敗しました");
        }
    });
}
```

### 通報タイプ

```java
public enum ReportType {
    CHEAT,          // チート使用
    HARASSMENT,     // 迷惑行為
    CHAT_ABUSE,     // 不適切発言
    GRIEFING,       // 荒らし行為
    OTHER           // その他
}
```

---

## バグレポートの作成

```java
import lol.hanyuu.kumaReport.model.BugReport;

public void createBugReport(Player reporter, String description) {
    KumaReport kumaReport = getKumaReport();

    // 座標情報を取得
    String location = String.format("%s: X=%d, Y=%d, Z=%d",
        reporter.getWorld().getName(),
        reporter.getLocation().getBlockX(),
        reporter.getLocation().getBlockY(),
        reporter.getLocation().getBlockZ()
    );

    // バグレポートオブジェクトを作成
    BugReport bugReport = new BugReport(
        reporter.getUniqueId(),      // 報告者UUID
        reporter.getName(),           // 報告者名
        description,                  // バグの説明
        location                      // 座標情報
    );

    // 非同期でデータベースに保存
    Bukkit.getScheduler().runTaskAsynchronously(yourPlugin, () -> {
        try {
            int reportId = kumaReport.getDatabaseManager()
                .getBugReportDAO()
                .createBugReport(bugReport);

            reporter.sendMessage("バグレポートを送信しました（ID: #" + reportId + "）");

        } catch (SQLException e) {
            getLogger().severe("バグレポートの保存に失敗: " + e.getMessage());
        }
    });
}
```

---

## 通報の取得

### 通報IDで取得

```java
import lol.hanyuu.kumaReport.model.Report;

public void getReportById(int reportId) {
    KumaReport kumaReport = getKumaReport();

    Bukkit.getScheduler().runTaskAsynchronously(yourPlugin, () -> {
        try {
            Report report = kumaReport.getDatabaseManager()
                .getReportDAO()
                .getReportById(reportId);

            if (report != null) {
                // 通報が見つかった
                getLogger().info("通報者: " + report.getReporterName());
                getLogger().info("被通報者: " + report.getReportedName());
                getLogger().info("理由: " + report.getReason());
            } else {
                // 通報が見つからない
                getLogger().warning("通報 #" + reportId + " が見つかりません");
            }

        } catch (SQLException e) {
            getLogger().severe("通報の取得に失敗: " + e.getMessage());
        }
    });
}
```

### すべての通報を取得

```java
import java.util.List;

public void getAllReports() {
    KumaReport kumaReport = getKumaReport();

    Bukkit.getScheduler().runTaskAsynchronously(yourPlugin, () -> {
        try {
            List<Report> reports = kumaReport.getDatabaseManager()
                .getReportDAO()
                .getAllReports();

            getLogger().info("総通報数: " + reports.size());

            for (Report report : reports) {
                getLogger().info("ID: #" + report.getId() +
                    " - " + report.getReporterName() +
                    " → " + report.getReportedName());
            }

        } catch (SQLException e) {
            getLogger().severe("通報の取得に失敗: " + e.getMessage());
        }
    });
}
```

### ページ単位で取得

```java
public void getReportsPaginated(int page, int itemsPerPage) {
    KumaReport kumaReport = getKumaReport();

    Bukkit.getScheduler().runTaskAsynchronously(yourPlugin, () -> {
        try {
            List<Report> reports = kumaReport.getDatabaseManager()
                .getReportDAO()
                .getReportsPaginated(page, itemsPerPage);

            getLogger().info("ページ " + page + " の通報数: " + reports.size());

        } catch (SQLException e) {
            getLogger().severe("通報の取得に失敗: " + e.getMessage());
        }
    });
}
```

### プレイヤー別に取得

```java
public void getReportsByPlayer(UUID playerUUID) {
    KumaReport kumaReport = getKumaReport();

    Bukkit.getScheduler().runTaskAsynchronously(yourPlugin, () -> {
        try {
            // 被通報者として
            List<Report> reportsAsReported = kumaReport.getDatabaseManager()
                .getReportDAO()
                .getReportsByReportedPlayer(playerUUID);

            // 通報者として
            List<Report> reportsAsReporter = kumaReport.getDatabaseManager()
                .getReportDAO()
                .getReportsByReporter(playerUUID);

            getLogger().info("被通報回数: " + reportsAsReported.size());
            getLogger().info("通報回数: " + reportsAsReporter.size());

        } catch (SQLException e) {
            getLogger().severe("通報の取得に失敗: " + e.getMessage());
        }
    });
}
```

---

## 通報の更新

### ステータスの更新

```java
import lol.hanyuu.kumaReport.model.ReportStatus;

public void updateReportStatus(int reportId, ReportStatus status,
                                UUID handlerUUID, String handlerName, String note) {
    KumaReport kumaReport = getKumaReport();

    Bukkit.getScheduler().runTaskAsynchronously(yourPlugin, () -> {
        try {
            boolean success = kumaReport.getDatabaseManager()
                .getReportDAO()
                .updateReportStatus(reportId, status, handlerUUID, handlerName, note);

            if (success) {
                getLogger().info("通報 #" + reportId + " を更新しました");
            } else {
                getLogger().warning("通報 #" + reportId + " が見つかりません");
            }

        } catch (SQLException e) {
            getLogger().severe("通報の更新に失敗: " + e.getMessage());
        }
    });
}
```

### 通報ステータス

```java
public enum ReportStatus {
    PENDING,        // 未処理
    IN_PROGRESS,    // 処理中
    ACCEPTED,       // 承認済み
    DENIED          // 却下済み
}
```

---

## 統計情報の取得

```java
import lol.hanyuu.kumaReport.database.ReportDAO;

public void getStatistics() {
    KumaReport kumaReport = getKumaReport();

    Bukkit.getScheduler().runTaskAsynchronously(yourPlugin, () -> {
        try {
            ReportDAO.ReportStatistics stats = kumaReport.getDatabaseManager()
                .getReportDAO()
                .getStatistics();

            getLogger().info("===== 通報統計 =====");
            getLogger().info("総通報数: " + stats.total);
            getLogger().info("未処理: " + stats.pending);
            getLogger().info("承認済み: " + stats.accepted);
            getLogger().info("却下済み: " + stats.denied);

        } catch (SQLException e) {
            getLogger().severe("統計の取得に失敗: " + e.getMessage());
        }
    });
}
```

---

## イベントシステム

Kuma-Report は、通報やバグレポートが作成されたときにカスタムイベントを発火します（将来のバージョンで実装予定）。

### イベントのリスン（予定）

```java
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import lol.hanyuu.kumaReport.event.ReportCreateEvent;

public class YourListener implements Listener {

    @EventHandler
    public void onReportCreate(ReportCreateEvent event) {
        Report report = event.getReport();

        // 通報が作成されたときの処理
        getLogger().info("新しい通報: #" + report.getId());

        // イベントをキャンセルすることも可能
        if (shouldCancelReport(report)) {
            event.setCancelled(true);
        }
    }
}
```

**注意:** イベントシステムは現在開発中です。将来のバージョンで実装予定です。

---

## 使用例

### 例1: カスタム通報システム

```java
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CustomReportCommand implements CommandExecutor {
    private KumaReport kumaReport;

    public CustomReportCommand(KumaReport kumaReport) {
        this.kumaReport = kumaReport;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command,
                             String label, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;

        // カスタムロジック: 特定の条件でのみ通報を許可
        if (!canReport(player)) {
            player.sendMessage("通報する権限がありません");
            return true;
        }

        // 通報を作成
        if (args.length >= 2) {
            Player target = Bukkit.getPlayer(args[0]);
            if (target != null) {
                String reason = String.join(" ",
                    Arrays.copyOfRange(args, 1, args.length));

                Report report = new Report(
                    player.getUniqueId(),
                    player.getName(),
                    target.getUniqueId(),
                    target.getName(),
                    reason,
                    ReportType.OTHER
                );

                // 保存
                saveReport(report);
            }
        }

        return true;
    }

    private void saveReport(Report report) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                int id = kumaReport.getDatabaseManager()
                    .getReportDAO()
                    .createReport(report);

                // 成功
            } catch (SQLException e) {
                // エラー処理
            }
        });
    }

    private boolean canReport(Player player) {
        // カスタムロジック
        return true;
    }
}
```

---

### 例2: 通報ダッシュボード

```java
public class ReportDashboard {
    private KumaReport kumaReport;

    public void showDashboard(Player admin) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                // 統計情報を取得
                ReportDAO.ReportStatistics stats = kumaReport
                    .getDatabaseManager()
                    .getReportDAO()
                    .getStatistics();

                // 未処理の通報を取得
                List<Report> pendingReports = kumaReport
                    .getDatabaseManager()
                    .getReportDAO()
                    .getReportsByStatus(ReportStatus.PENDING);

                // メインスレッドで表示
                Bukkit.getScheduler().runTask(plugin, () -> {
                    admin.sendMessage("===== 通報ダッシュボード =====");
                    admin.sendMessage("総通報数: " + stats.total);
                    admin.sendMessage("未処理: " + stats.pending);
                    admin.sendMessage("");
                    admin.sendMessage("最新の未処理通報:");

                    for (int i = 0; i < Math.min(5, pendingReports.size()); i++) {
                        Report report = pendingReports.get(i);
                        admin.sendMessage("#" + report.getId() +
                            " - " + report.getReporterName() +
                            " → " + report.getReportedName());
                    }
                });

            } catch (SQLException e) {
                getLogger().severe("ダッシュボードの表示に失敗: " + e.getMessage());
            }
        });
    }
}
```

---

### 例3: 自動通報システム

```java
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class AutoReportListener implements Listener {
    private KumaReport kumaReport;

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        // カスタムロジック: 不正な動きを検知
        if (detectCheating(player)) {
            // 自動的に通報を作成
            createAutoReport(player, "自動検知: 不正な移動");
        }
    }

    private void createAutoReport(Player cheater, String reason) {
        // システムユーザーとして通報
        UUID systemUUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
        String systemName = "[System]";

        Report report = new Report(
            systemUUID,
            systemName,
            cheater.getUniqueId(),
            cheater.getName(),
            reason,
            ReportType.CHEAT
        );

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                kumaReport.getDatabaseManager()
                    .getReportDAO()
                    .createReport(report);
            } catch (SQLException e) {
                getLogger().severe("自動通報の作成に失敗: " + e.getMessage());
            }
        });
    }

    private boolean detectCheating(Player player) {
        // チート検知ロジック
        return false;
    }
}
```

---

## ベストプラクティス

### 1. 非同期処理を使用

データベース操作は必ず非同期で実行してください。

```java
// 良い例
Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
    // データベース操作
});

// 悪い例（メインスレッドをブロック）
Report report = kumaReport.getDatabaseManager().getReportDAO().getReportById(1);
```

---

### 2. エラー処理

必ずエラー処理を実装してください。

```java
try {
    // データベース操作
} catch (SQLException e) {
    getLogger().severe("エラーが発生しました: " + e.getMessage());
    // ユーザーにメッセージを送信
}
```

---

### 3. null チェック

取得したデータが null でないか確認してください。

```java
Report report = dao.getReportById(id);
if (report != null) {
    // 処理
} else {
    // 通報が見つからない場合の処理
}
```

---

## 参考情報

### Javadoc

完全な API ドキュメントは、GitHubリポジトリの Javadoc を参照してください:
https://github.com/humisan/Kuma-Report/tree/main/docs/javadoc

### ソースコード

ソースコードを直接確認することもできます:
https://github.com/humisan/Kuma-Report/tree/main/src

---

前のページ: [トラブルシューティング](Troubleshooting.md) | 次のページ: [ホーム](Home.md)
