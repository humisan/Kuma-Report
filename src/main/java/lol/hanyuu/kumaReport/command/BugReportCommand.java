package lol.hanyuu.kumaReport.command;

import lol.hanyuu.kumaReport.KumaReport;
import lol.hanyuu.kumaReport.gui.BugReportGUI;
import lol.hanyuu.kumaReport.model.BugReport;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.*;

/**
 * /bugreport コマンドハンドラ
 * `/bugreport <説明>` - バグを報告
 */
public class BugReportCommand implements CommandExecutor, TabCompleter {
    private final KumaReport plugin;

    public BugReportCommand(KumaReport plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessageManager().getMessage("common.player-only"));
            return true;
        }

        Player player = (Player) sender;

        // 権限チェック
        if (!player.hasPermission("kumareport.bugreport")) {
            player.sendMessage(plugin.getMessageManager().getMessage("common.no-permission"));
            return true;
        }

        // 引数なし → GUI起動
        if (args.length == 0) {
            return openBugReportGUI(player);
        }

        // 引数あり → テキストで直接報告
        return submitBugReportText(player, args);
    }

    /**
     * GUIでバグレポートを開く
     */
    private boolean openBugReportGUI(Player player) {
        BugReportGUI gui = new BugReportGUI(plugin);
        gui.open(player);
        return true;
    }

    /**
     * テキストでバグを報告
     */
    private boolean submitBugReportText(Player player, String[] args) {
        // 説明を結合
        String description = String.join(" ", args);

        // 最小文字数チェック
        if (description.length() < 10) {
            player.sendMessage(plugin.getMessageManager().getMessage("bugreport.too-short"));
            return true;
        }

        // 最大文字数チェック
        if (description.length() > 1000) {
            player.sendMessage(plugin.getMessageManager().getMessage("bugreport.too-long"));
            return true;
        }

        // クールダウンチェック
        if (!player.hasPermission("kumareport.bypass.cooldown") &&
                plugin.getCooldownManager().isBugReportOnCooldown(player.getUniqueId())) {
            long remaining = plugin.getCooldownManager().getBugReportCooldownRemaining(player.getUniqueId());
            Map<String, String> placeholders = Map.of("time", String.valueOf(remaining));
            player.sendMessage(plugin.getMessageManager().getMessage("bugreport.cooldown", placeholders));
            return true;
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
                notifyStaff(player.getName(), description, location);

            } catch (SQLException e) {
                plugin.getLogger().severe("バグレポートの保存に失敗しました: " + e.getMessage());
                Bukkit.getScheduler().runTask(plugin, () ->
                        player.sendMessage(plugin.getMessageManager().getMessage("common.command-error"))
                );
            }
        });

        return true;
    }

    /**
     * スタッフに通知
     */
    private void notifyStaff(String reporterName, String description, String location) {
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

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return new ArrayList<>();
        }

        if (!sender.hasPermission("kumareport.bugreport")) {
            return new ArrayList<>();
        }

        // 引数がない場合はサジェストしない（GUIでバグレポート）
        if (args.length == 0) {
            return new ArrayList<>();
        }

        // バグの説明についてのサジェスト
        if (args.length == 1) {
            return List.of(
                    "<バグの説明を入力してください>",
                    "例：", "ブロックが表示されない",
                    "例：", "アイテムが消える",
                    "例：", "クラッシュする"
            );
        }

        return new ArrayList<>();
    }
}
