package lol.hanyuu.kumaReport.command;

import lol.hanyuu.kumaReport.KumaReport;
import lol.hanyuu.kumaReport.database.ReportDAO;
import lol.hanyuu.kumaReport.model.Report;
import lol.hanyuu.kumaReport.model.ReportStatus;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * /reportadmin コマンドハンドラ
 * 通報管理用のコマンド
 */
public class ReportAdminCommand implements CommandExecutor, TabCompleter {
    private final KumaReport plugin;

    public ReportAdminCommand(KumaReport plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // 権限チェック
        if (!sender.hasPermission("kumareport.admin")) {
            sender.sendMessage(plugin.getMessageManager().getMessage("common.no-permission"));
            return true;
        }

        // サブコマンド処理
        if (args.length == 0) {
            sender.sendMessage(plugin.getMessageManager().getMessage("admin.usage"));
            return true;
        }

        String subcommand = args[0].toLowerCase();

        try {
            switch (subcommand) {
                case "list" -> handleList(sender, args);
                case "view" -> handleView(sender, args);
                case "accept" -> handleAccept(sender, args);
                case "deny" -> handleDeny(sender, args);
                case "search" -> handleSearch(sender, args);
                case "stats" -> handleStats(sender, args);
                case "reload" -> handleReload(sender);
                default -> sender.sendMessage(plugin.getMessageManager().getMessage("admin.unknown-subcommand", Map.of("subcommand", subcommand)));
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("コマンド実行エラーが発生しました: " + e.getMessage());
            sender.sendMessage(plugin.getMessageManager().getMessage("common.command-error"));
        }

        return true;
    }

    /**
     * list サブコマンド - 通報一覧
     */
    private void handleList(CommandSender sender, String[] args) throws SQLException {
        int page = 1;
        if (args.length > 1) {
            try {
                page = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage(plugin.getMessageManager().getMessage("admin.invalid-page-number"));
                return;
            }
        }

        ReportDAO reportDAO = plugin.getDatabaseManager().getReportDAO();
        int itemsPerPage = plugin.getConfigManager().getItemsPerPage();
        int totalPages = reportDAO.getTotalPages(itemsPerPage);

        if (page < 1 || page > totalPages) {
            sender.sendMessage(plugin.getMessageManager().getMessage("admin.page-out-of-range"));
            return;
        }

        List<Report> reports = reportDAO.getReportsPaginated(page, itemsPerPage);

        if (reports.isEmpty()) {
            sender.sendMessage(plugin.getMessageManager().getMessage("admin.no-reports"));
            return;
        }

        // ヘッダー表示
        Map<String, String> placeholders = Map.of(
                "page", String.valueOf(page),
                "maxpage", String.valueOf(totalPages)
        );
        sender.sendMessage(plugin.getMessageManager().getMessage("admin.list-header", placeholders));

        // 通報一覧
        for (Report report : reports) {
            // messages.yml の status.<STATUS> を使用してローカライズされた表示（カラーコード含む）を取得
            String statusKey = "status." + report.getStatus().name();
            String statusText = plugin.getMessageManager().getMessage(statusKey);
            // messages.yml にキーが無い場合、getMessage はキー自体を返すため、その場合はステータスの英語名をフォールバックとして使う
            if (statusText == null || statusText.equals(statusKey)) {
                statusText = report.getStatus().name();
            }
            sender.sendMessage(String.format("§e#%d §7- §f%s §7→ §f%s §7(%s)",
                    report.getId(),
                    report.getReporterName(),
                    report.getReportedName(),
                    statusText));
        }

        // フッター
        if (page < totalPages) {
            placeholders = Map.of("nextpage", String.valueOf(page + 1));
            sender.sendMessage(plugin.getMessageManager().getMessage("admin.list-footer", placeholders));
        }
    }

    /**
     * view サブコマンド - 通報詳細表示
     */
    private void handleView(CommandSender sender, String[] args) throws SQLException {
        if (args.length < 2) {
            sender.sendMessage(plugin.getMessageManager().getMessage("admin.view-usage"));
            return;
        }

        int reportId;
        try {
            reportId = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage(plugin.getMessageManager().getMessage("admin.invalid-report-id"));
            return;
        }

        Report report = plugin.getDatabaseManager().getReportDAO().getReportById(reportId);
        if (report == null) {
            Map<String, String> placeholders = Map.of("id", String.valueOf(reportId));
            sender.sendMessage(plugin.getMessageManager().getMessage("admin.report-not-found", placeholders));
            return;
        }

        // 詳細表示
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Map<String, String> placeholders = Map.of("id", String.valueOf(report.getId()));
        sender.sendMessage(plugin.getMessageManager().getMessage("admin.view-header", placeholders));

        placeholders = Map.of("reporter", report.getReporterName());
        sender.sendMessage(plugin.getMessageManager().getMessage("admin.view-reporter", placeholders));

        placeholders = Map.of("reported", report.getReportedName());
        sender.sendMessage(plugin.getMessageManager().getMessage("admin.view-reported", placeholders));

        placeholders = Map.of("reason", report.getReason());
        sender.sendMessage(plugin.getMessageManager().getMessage("admin.view-reason", placeholders));

        placeholders = Map.of("type", report.getReportType().name());
        sender.sendMessage(plugin.getMessageManager().getMessage("admin.view-type", placeholders));

        // 表示用は messages.yml の status.<STATUS> を利用してローカライズされた文字列を埋め込む
        String statusKey = "status." + report.getStatus().name();
        String statusText = plugin.getMessageManager().getMessage(statusKey);
        if (statusText == null || statusText.equals(statusKey)) {
            statusText = report.getStatus().name();
        }
        placeholders = Map.of("status", statusText);
        sender.sendMessage(plugin.getMessageManager().getMessage("admin.view-status", placeholders));

        placeholders = Map.of("date", sdf.format(report.getCreatedAt()));
        sender.sendMessage(plugin.getMessageManager().getMessage("admin.view-date", placeholders));

        if (report.getHandlerName() != null) {
            placeholders = Map.of("handler", report.getHandlerName());
            sender.sendMessage(plugin.getMessageManager().getMessage("admin.view-handler", placeholders));

            if (report.getHandledAt() != null) {
                placeholders = Map.of("date", sdf.format(report.getHandledAt()));
                sender.sendMessage(plugin.getMessageManager().getMessage("admin.view-handled-date", placeholders));
            }

            if (report.getHandlerNote() != null && !report.getHandlerNote().isEmpty()) {
                placeholders = Map.of("note", report.getHandlerNote());
                sender.sendMessage(plugin.getMessageManager().getMessage("admin.view-note", placeholders));
            }
        }

        sender.sendMessage(plugin.getMessageManager().getMessage("admin.view-footer"));
    }

    /**
     * accept サブコマンド - 通報承認
     */
    private void handleAccept(CommandSender sender, String[] args) throws SQLException {
        if (args.length < 2) {
            sender.sendMessage(plugin.getMessageManager().getMessage("admin.accept-usage"));
            return;
        }

        int reportId;
        try {
            reportId = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage(plugin.getMessageManager().getMessage("admin.invalid-report-id"));
            return;
        }

        String note = args.length > 2 ? String.join(" ", Arrays.copyOfRange(args, 2, args.length)) : "";
        UUID handlerUuid = sender instanceof Player ? ((Player) sender).getUniqueId() : null;
        String handlerName = sender.getName();

        boolean success = plugin.getDatabaseManager().getReportDAO()
                .updateReportStatus(reportId, ReportStatus.ACCEPTED, handlerUuid, handlerName, note);

        if (success) {
            Map<String, String> placeholders = Map.of("id", String.valueOf(reportId));
            sender.sendMessage(plugin.getMessageManager().getMessage("admin.accepted", placeholders));
        } else {
            Map<String, String> placeholders = Map.of("id", String.valueOf(reportId));
            sender.sendMessage(plugin.getMessageManager().getMessage("admin.report-not-found", placeholders));
        }
    }

    /**
     * deny サブコマンド - 通報却下
     */
    private void handleDeny(CommandSender sender, String[] args) throws SQLException {
        if (args.length < 2) {
            sender.sendMessage(plugin.getMessageManager().getMessage("admin.deny-usage"));
            return;
        }

        int reportId;
        try {
            reportId = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage(plugin.getMessageManager().getMessage("admin.invalid-report-id"));
            return;
        }

        String note = args.length > 2 ? String.join(" ", Arrays.copyOfRange(args, 2, args.length)) : "";
        UUID handlerUuid = sender instanceof Player ? ((Player) sender).getUniqueId() : null;
        String handlerName = sender.getName();

        boolean success = plugin.getDatabaseManager().getReportDAO()
                .updateReportStatus(reportId, ReportStatus.DENIED, handlerUuid, handlerName, note);

        if (success) {
            Map<String, String> placeholders = Map.of("id", String.valueOf(reportId));
            sender.sendMessage(plugin.getMessageManager().getMessage("admin.denied", placeholders));
        } else {
            Map<String, String> placeholders = Map.of("id", String.valueOf(reportId));
            sender.sendMessage(plugin.getMessageManager().getMessage("admin.report-not-found", placeholders));
        }
    }

    /**
     * search サブコマンド - プレイヤー検索
     */
    private void handleSearch(CommandSender sender, String[] args) throws SQLException {
        if (args.length < 2) {
            sender.sendMessage(plugin.getMessageManager().getMessage("admin.search-usage"));
            return;
        }

        String playerName = args[1];
        List<Report> reports = plugin.getDatabaseManager().getReportDAO()
                .getReportsByReportedPlayer(java.util.UUID.fromString("00000000-0000-0000-0000-000000000000")); // TODO: UUIDで検索

        sender.sendMessage(plugin.getMessageManager().getMessage("admin.search-header", Map.of("player", playerName)));
        sender.sendMessage(plugin.getMessageManager().getMessage("admin.search-in-progress"));
    }

    /**
     * stats サブコマンド - 統計表示
     */
    private void handleStats(CommandSender sender, String[] args) throws SQLException {
        ReportDAO.ReportStatistics stats = plugin.getDatabaseManager().getReportDAO().getStatistics();

        sender.sendMessage(plugin.getMessageManager().getMessage("admin.stats-header"));
        sender.sendMessage(plugin.getMessageManager().getMessage("admin.stats-total", Map.of("total", String.valueOf(stats.total))));
        sender.sendMessage(plugin.getMessageManager().getMessage("admin.stats-pending", Map.of("pending", String.valueOf(stats.pending))));
        sender.sendMessage(plugin.getMessageManager().getMessage("admin.stats-accepted", Map.of("accepted", String.valueOf(stats.accepted))));
        sender.sendMessage(plugin.getMessageManager().getMessage("admin.stats-denied", Map.of("denied", String.valueOf(stats.denied))));
        sender.sendMessage(plugin.getMessageManager().getMessage("admin.stats-footer"));
    }

    /**
     * reload サブコマンド - 設定リロード
     */
    private void handleReload(CommandSender sender) {
        plugin.getConfigManager().reloadConfig();
        plugin.getMessageManager().reload();
        sender.sendMessage(plugin.getMessageManager().getMessage("common.config-reloaded"));
    }

    /**
     * ステータスに応じた色を取得
     */
    private String getStatusColor(ReportStatus status) {
        // 互換性維持のために残すが、現在は messages.yml の status.<STATUS> を直接利用することを推奨
        String msg = plugin.getMessageManager().getMessage("status." + status.name());
        if (msg == null) {
            return "§7"; // フォールバック
        }

        // 先頭が § で始まる場合、連続するカラーコード (例: §a または §x§1§2§3§4§5§6) を抽出して返す
        if (msg.startsWith("§")) {
            StringBuilder sb = new StringBuilder();
            int i = 0;
            while (i < msg.length() - 1) {
                if (msg.charAt(i) != '§') break;
                // '§' と次の文字を追加
                sb.append('§').append(msg.charAt(i + 1));
                i += 2;
            }
            if (sb.length() > 0) {
                return sb.toString();
            }
        }

        // フォールバック
        return "§7";
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("kumareport.admin")) {
            return new ArrayList<>();
        }

        if (args.length == 1) {
            return List.of("list", "view", "accept", "deny", "search", "stats", "reload").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
}
