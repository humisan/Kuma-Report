package lol.hanyuu.kumaReport.listener;

import lol.hanyuu.kumaReport.KumaReport;
import lol.hanyuu.kumaReport.model.BugReport;
import lol.hanyuu.kumaReport.model.Report;
import lol.hanyuu.kumaReport.model.ReportType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * チャット入力イベントハンドラ
 * バグレポートや通報の詳細入力を処理
 */
public class ChatListener implements Listener {
    private final KumaReport plugin;

    // チャット入力待機状態: Player UUID → 入力モード
    private final Map<UUID, ChatInputMode> waitingForInput = new HashMap<>();
    
    // チャット入力データ: Player UUID → データ
    private final Map<UUID, Map<String, Object>> inputData = new HashMap<>();

    public ChatListener(KumaReport plugin) {
        this.plugin = plugin;
    }

    /**
     * チャット入力イベント
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        // 入力待機中かチェック
        if (!waitingForInput.containsKey(playerUUID)) {
            return;
        }

        // チャットをキャンセル（通常のチャットとして表示しない）
        event.setCancelled(true);

        ChatInputMode mode = waitingForInput.get(playerUUID);
        String input = event.getMessage();

        // モードに応じた処理
        switch (mode) {
            case BUG_REPORT_DESCRIPTION -> handleBugReportInput(player, input);
            case REPORT_DETAIL -> handleReportDetailInput(player, input);
        }

        // 入力待機状態をクリア
        waitingForInput.remove(playerUUID);
        inputData.remove(playerUUID);
    }

    /**
     * バグレポートの説明入力を処理
     */
    private void handleBugReportInput(Player player, String description) {
        // 最小文字数チェック
        if (description.length() < 10) {
            player.sendMessage(plugin.getMessageManager().getMessage("bugreport.too-short"));
            return;
        }

        // 最大文字数チェック
        if (description.length() > 1000) {
            player.sendMessage(plugin.getMessageManager().getMessage("bugreport.too-long"));
            return;
        }

        // クールダウンチェック
        if (!player.hasPermission("kumareport.bypass.cooldown") &&
                plugin.getCooldownManager().isBugReportOnCooldown(player.getUniqueId())) {
            long remaining = plugin.getCooldownManager().getBugReportCooldownRemaining(player.getUniqueId());
            Map<String, String> placeholders = Map.of("time", String.valueOf(remaining));
            player.sendMessage(plugin.getMessageManager().getMessage("bugreport.cooldown", placeholders));
            return;
        }

        // 座標情報を取得
        String location = String.format("%s: X=%d, Y=%d, Z=%d",
                player.getWorld().getName(),
                player.getLocation().getBlockX(),
                player.getLocation().getBlockY(),
                player.getLocation().getBlockZ());

        // 非同期でデータベースに保存
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                BugReport bugReport = new BugReport(
                        player.getUniqueId(),
                        player.getName(),
                        description,
                        location
                );

                int reportId = plugin.getDatabaseManager().getBugReportDAO().createBugReport(bugReport);

                // クールダウン設定
                plugin.getCooldownManager().setBugReportCooldown(player.getUniqueId());

                // 完了メッセージ
                Bukkit.getScheduler().runTask(plugin, () ->
                        player.sendMessage(plugin.getMessageManager().getMessage("bugreport.success"))
                );

                // スタッフに通知
                notifyStaffBugReport(player.getName(), description, location);

                // Discord通知
                bugReport.setId(reportId);
                plugin.getDiscordWebhookManager().notifyNewBugReport(bugReport);

            } catch (SQLException e) {
                plugin.getLogger().severe("バグレポートの保存に失敗しました: " + e.getMessage());
                Bukkit.getScheduler().runTask(plugin, () ->
                        player.sendMessage(plugin.getMessageManager().getMessage("common.command-error"))
                );
            }
        });
    }

    /**
     * 通報の詳細入力を処理
     */
    private void handleReportDetailInput(Player player, String detail) {
        Map<String, Object> data = inputData.get(player.getUniqueId());
        if (data == null) {
            return;
        }

        Player targetPlayer = (Player) data.get("targetPlayer");
        ReportType reportType = (ReportType) data.get("reportType");

        if (targetPlayer == null || reportType == null) {
            player.sendMessage(plugin.getMessageManager().getMessage("common.command-error"));
            return;
        }

        // 詳細が空の場合はカテゴリ名のみを使用
        String reason;
        if (detail.trim().isEmpty() || detail.equalsIgnoreCase("skip") || detail.equalsIgnoreCase("なし")) {
            reason = reportType.name();
        } else {
            // 詳細の長さチェック
            if (detail.length() > 500) {
                player.sendMessage(plugin.getMessageManager().getMessage("report.invalid-reason-length"));
                return;
            }
            reason = reportType.name() + ": " + detail;
        }

        // クールダウンチェック
        if (!player.hasPermission("kumareport.bypass.cooldown") &&
                plugin.getCooldownManager().isReportOnCooldown(player.getUniqueId())) {
            long remaining = plugin.getCooldownManager().getReportCooldownRemaining(player.getUniqueId());
            Map<String, String> placeholders = Map.of("time", String.valueOf(remaining));
            player.sendMessage(plugin.getMessageManager().getMessage("report.cooldown", placeholders));
            return;
        }

        // 非同期でデータベースに保存
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                Report report = new Report(
                        player.getUniqueId(),
                        player.getName(),
                        targetPlayer.getUniqueId(),
                        targetPlayer.getName(),
                        reason,
                        reportType
                );

                int reportId = plugin.getDatabaseManager().getReportDAO().createReport(report);

                // クールダウン設定
                plugin.getCooldownManager().setReportCooldown(player.getUniqueId());

                // 完了メッセージ
                Map<String, String> placeholders = Map.of("player", targetPlayer.getName());
                Bukkit.getScheduler().runTask(plugin, () ->
                        player.sendMessage(plugin.getMessageManager().getMessage("report.success", placeholders))
                );

                // スタッフに通知
                notifyStaffReport(player.getName(), targetPlayer.getName(), reason);

                // Discord通知
                report.setId(reportId);
                plugin.getDiscordWebhookManager().notifyNewReport(report);

            } catch (Exception e) {
                plugin.getLogger().severe("通報の保存に失敗しました: " + e.getMessage());
                Bukkit.getScheduler().runTask(plugin, () ->
                        player.sendMessage(plugin.getMessageManager().getMessage("common.command-error"))
                );
            }
        });
    }

    /**
     * スタッフに通報を通知
     */
    private void notifyStaffReport(String reporterName, String reportedName, String reason) {
        if (!plugin.getConfigManager().isStaffNotificationEnabled()) {
            return;
        }

        Map<String, String> placeholders = Map.of(
                "reporter", reporterName,
                "reported", reportedName,
                "reason", reason
        );
        String message = plugin.getMessageManager().getMessage("staff.new-report", placeholders);

        Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.hasPermission("kumareport.notify"))
                .forEach(p -> p.sendMessage(message));
    }

    /**
     * スタッフにバグレポートを通知
     */
    private void notifyStaffBugReport(String reporterName, String description, String location) {
        if (!plugin.getConfigManager().isStaffNotificationEnabled()) {
            return;
        }

        Map<String, String> placeholders = Map.of(
                "reporter", reporterName,
                "description", description
        );
        String message = plugin.getMessageManager().getMessage("staff.new-bugreport", placeholders);

        Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.hasPermission("kumareport.notify"))
                .forEach(p -> p.sendMessage(message));
    }

    /**
     * バグレポートの説明入力を開始
     */
    public void startBugReportInput(Player player) {
        waitingForInput.put(player.getUniqueId(), ChatInputMode.BUG_REPORT_DESCRIPTION);
    }

    /**
     * 通報の詳細入力を開始
     */
    public void startReportDetailInput(Player player, Player targetPlayer, ReportType reportType) {
        waitingForInput.put(player.getUniqueId(), ChatInputMode.REPORT_DETAIL);
        
        Map<String, Object> data = new HashMap<>();
        data.put("targetPlayer", targetPlayer);
        data.put("reportType", reportType);
        inputData.put(player.getUniqueId(), data);
    }

    /**
     * 入力待機状態をクリア
     */
    public void clearInputState(Player player) {
        waitingForInput.remove(player.getUniqueId());
        inputData.remove(player.getUniqueId());
    }

    /**
     * チャット入力モード
     */
    private enum ChatInputMode {
        BUG_REPORT_DESCRIPTION,
        REPORT_DETAIL
    }
}
