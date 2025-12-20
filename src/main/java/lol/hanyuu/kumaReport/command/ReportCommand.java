package lol.hanyuu.kumaReport.command;

import lol.hanyuu.kumaReport.KumaReport;
import lol.hanyuu.kumaReport.gui.forms.FormManager;
import lol.hanyuu.kumaReport.model.Report;
import lol.hanyuu.kumaReport.model.ReportType;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * /report コマンドハンドラ
 * `/report` - GUIで通報
 * `/report <プレイヤー名> <理由>` - テキストで通報
 */
public class ReportCommand implements CommandExecutor, TabCompleter {
    private final KumaReport plugin;

    public ReportCommand(KumaReport plugin) {
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
        if (!player.hasPermission("kumareport.use")) {
            player.sendMessage(plugin.getMessageManager().getMessage("common.no-permission"));
            return true;
        }

        // 引数なし → GUI起動
        if (args.length == 0) {
            return openReportGUI(player);
        }

        // 引数2個以上 → テキスト通報
        if (args.length >= 2) {
            return reportPlayerText(player, args);
        }

        // 不正な使用方法
        player.sendMessage(plugin.getMessageManager().getMessage("report.usage"));
        return true;
    }

    /**
     * GUIで通報を開く（Java/Bedrock版自動判定）
     */
    private boolean openReportGUI(Player player) {
        FormManager formManager = new FormManager(plugin);
        formManager.openReportGUI(player);
        return true;
    }

    /**
     * テキストで直接プレイヤーを通報
     */
    private boolean reportPlayerText(Player reporter, String[] args) {
        String targetName = args[0];
        String reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        // クールダウンチェック
        if (!reporter.hasPermission("kumareport.bypass.cooldown") &&
                plugin.getCooldownManager().isReportOnCooldown(reporter.getUniqueId())) {
            long remaining = plugin.getCooldownManager().getReportCooldownRemaining(reporter.getUniqueId());
            Map<String, String> placeholders = Map.of("time", String.valueOf(remaining));
            reporter.sendMessage(plugin.getMessageManager().getMessage("report.cooldown", placeholders));
            return true;
        }

        // プレイヤー検索
        Player targetPlayer = Bukkit.getPlayer(targetName);
        if (targetPlayer == null) {
            Map<String, String> placeholders = Map.of("player", targetName);
            reporter.sendMessage(plugin.getMessageManager().getMessage("common.player-not-found", placeholders));
            return true;
        }

        // 自己通報防止
        if (reporter.getUniqueId().equals(targetPlayer.getUniqueId())) {
            reporter.sendMessage(plugin.getMessageManager().getMessage("report.self-report"));
            return true;
        }

        // 理由の長さチェック
        if (reason.length() < 5 || reason.length() > 500) {
            reporter.sendMessage(plugin.getMessageManager().getMessage("report.invalid-reason-length"));
            return true;
        }

        // 非同期でデータベースに保存
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                Report report = new Report(
                        reporter.getUniqueId(),
                        reporter.getName(),
                        targetPlayer.getUniqueId(),
                        targetPlayer.getName(),
                        reason,
                        ReportType.OTHER  // テキスト通報はOTHERタイプ
                );

                int reportId = plugin.getDatabaseManager().getReportDAO().createReport(report);

                // クールダウン設定
                plugin.getCooldownManager().setReportCooldown(reporter.getUniqueId());

                // 完了メッセージ
                Map<String, String> placeholders = Map.of("player", targetPlayer.getName());
                Bukkit.getScheduler().runTask(plugin, () ->
                        reporter.sendMessage(plugin.getMessageManager().getMessage("report.success", placeholders))
                );

                // スタッフに通知
                notifyStaff(reporter.getName(), targetPlayer.getName(), reason);

                // Discord通知
                report.setId(reportId);
                plugin.getDiscordWebhookManager().notifyNewReport(report);

            } catch (SQLException e) {
                plugin.getLogger().severe("通報の保存に失敗しました: " + e.getMessage());
                Bukkit.getScheduler().runTask(plugin, () ->
                        reporter.sendMessage(plugin.getMessageManager().getMessage("common.command-error"))
                );
            }
        });

        return true;
    }

    /**
     * スタッフに通知
     */
    private void notifyStaff(String reporterName, String reportedName, String reason) {
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

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return new ArrayList<>();
        }

        Player player = (Player) sender;

        if (!sender.hasPermission("kumareport.use")) {
            return new ArrayList<>();
        }

        // 最初の引数 → オンラインプレイヤー名補完
        if (args.length == 1) {
            String partialName = args[0];
            return Bukkit.getOnlinePlayers().stream()
                    .filter(p -> !p.getUniqueId().equals(player.getUniqueId())) // 自分自身を除外
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(partialName.toLowerCase()))
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
}
