package lol.hanyuu.kumaReport.listener;

import lol.hanyuu.kumaReport.KumaReport;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.sql.SQLException;
import java.util.Map;

/**
 * プレイヤー参加イベントリスナー
 * ログイン時に権限に応じた通知を表示
 */
public class PlayerJoinListener implements Listener {
    private final KumaReport plugin;

    public PlayerJoinListener(KumaReport plugin) {
        this.plugin = plugin;
    }

    /**
     * プレイヤー参加時の処理
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // スタッフに未処理通報数を通知
        if (player.hasPermission("kumareport.admin")) {
            notifyStaffPendingReports(player);
        }

        // TODO: 通報者への処理完了通知
    }

    /**
     * スタッフに未処理通報数を通知
     */
    private void notifyStaffPendingReports(Player player) {
        // 非同期でデータベースアクセス
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                int pendingCount = plugin.getDatabaseManager()
                        .getReportDAO()
                        .getPendingReportCount();

                if (pendingCount > 0) {
                    Map<String, String> placeholders = Map.of(
                            "count", String.valueOf(pendingCount)
                    );

                    String message = plugin.getMessageManager()
                            .getMessage("staff.pending-reports", placeholders);

                    // メインスレッドで送信
                    plugin.getServer().getScheduler().runTask(plugin, () ->
                            player.sendMessage(message)
                    );
                }
            } catch (SQLException e) {
                plugin.getLogger().warning("未処理通報数の取得に失敗: " + e.getMessage());
            }
        });
    }
}
