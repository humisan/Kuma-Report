package lol.hanyuu.kumaReport.manager;

import lol.hanyuu.kumaReport.KumaReport;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * クールダウン管理クラス
 * スパム防止のためプレイヤーの通報時間を追跡
 */
public class CooldownManager {
    private final KumaReport plugin;
    private final Map<UUID, Long> reportCooldowns;
    private final Map<UUID, Long> bugReportCooldowns;

    public CooldownManager(KumaReport plugin) {
        this.plugin = plugin;
        this.reportCooldowns = new HashMap<>();
        this.bugReportCooldowns = new HashMap<>();

        // 定期的にクールダウンをクリーンアップ
        startCleanupTask();
    }

    /**
     * プレイヤー通報のクールダウンをチェック
     */
    public boolean isReportOnCooldown(UUID playerUuid) {
        if (!reportCooldowns.containsKey(playerUuid)) {
            return false;
        }

        long lastReportTime = reportCooldowns.get(playerUuid);
        long cooldownSeconds = plugin.getConfigManager().getReportCooldown();
        long elapsed = (System.currentTimeMillis() - lastReportTime) / 1000;

        return elapsed < cooldownSeconds;
    }

    /**
     * プレイヤー通報の残りクールダウン時間を取得（秒）
     */
    public long getReportCooldownRemaining(UUID playerUuid) {
        if (!reportCooldowns.containsKey(playerUuid)) {
            return 0;
        }

        long lastReportTime = reportCooldowns.get(playerUuid);
        long cooldownSeconds = plugin.getConfigManager().getReportCooldown();
        long elapsed = (System.currentTimeMillis() - lastReportTime) / 1000;
        long remaining = cooldownSeconds - elapsed;

        return Math.max(0, remaining);
    }

    /**
     * プレイヤー通報のクールダウンを設定
     */
    public void setReportCooldown(UUID playerUuid) {
        reportCooldowns.put(playerUuid, System.currentTimeMillis());
    }

    /**
     * バグレポートのクールダウンをチェック
     */
    public boolean isBugReportOnCooldown(UUID playerUuid) {
        if (!bugReportCooldowns.containsKey(playerUuid)) {
            return false;
        }

        long lastReportTime = bugReportCooldowns.get(playerUuid);
        long cooldownSeconds = plugin.getConfigManager().getBugReportCooldown();
        long elapsed = (System.currentTimeMillis() - lastReportTime) / 1000;

        return elapsed < cooldownSeconds;
    }

    /**
     * バグレポートの残りクールダウン時間を取得（秒）
     */
    public long getBugReportCooldownRemaining(UUID playerUuid) {
        if (!bugReportCooldowns.containsKey(playerUuid)) {
            return 0;
        }

        long lastReportTime = bugReportCooldowns.get(playerUuid);
        long cooldownSeconds = plugin.getConfigManager().getBugReportCooldown();
        long elapsed = (System.currentTimeMillis() - lastReportTime) / 1000;
        long remaining = cooldownSeconds - elapsed;

        return Math.max(0, remaining);
    }

    /**
     * バグレポートのクールダウンを設定
     */
    public void setBugReportCooldown(UUID playerUuid) {
        bugReportCooldowns.put(playerUuid, System.currentTimeMillis());
    }

    /**
     * クールダウンをバイパスできるか確認
     */
    public boolean canBypassCooldown(Player player) {
        return player.hasPermission("kumareport.bypass.cooldown");
    }

    /**
     * 定期クリーンアップタスクを開始
     * 古いクールダウン情報を削除
     */
    private void startCleanupTask() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            long currentTime = System.currentTimeMillis();
            long maxCooldown = Math.max(
                    plugin.getConfigManager().getReportCooldown(),
                    plugin.getConfigManager().getBugReportCooldown()
            ) * 1000L;

            // 期限切れのクールダウンを削除
            reportCooldowns.entrySet().removeIf(entry ->
                    (currentTime - entry.getValue()) > maxCooldown * 2
            );

            bugReportCooldowns.entrySet().removeIf(entry ->
                    (currentTime - entry.getValue()) > maxCooldown * 2
            );
        }, 20 * 60, 20 * 60); // 1分ごとに実行
    }

    /**
     * クールダウンをクリア（テスト用）
     */
    public void clearAllCooldowns() {
        reportCooldowns.clear();
        bugReportCooldowns.clear();
    }
}
