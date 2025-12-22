package lol.hanyuu.kumaReport.gui.forms;

import lol.hanyuu.kumaReport.KumaReport;
import lol.hanyuu.kumaReport.model.Report;
import lol.hanyuu.kumaReport.model.ReportType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.geysermc.cumulus.form.CustomForm;
import org.geysermc.floodgate.api.FloodgateApi;

import java.util.Map;
import java.util.UUID;

/**
 * 通報理由入力 Form（詳細入力用）
 * CustomForm でテキスト入力フィールドを提供
 */
public class ReportReasonInputForm {
    private final KumaReport plugin;

    public ReportReasonInputForm(KumaReport plugin) {
        this.plugin = plugin;
    }

    /**
     * Form を開く
     */
    public void open(Player reporter, Map<String, Object> playerData) {
        try {
            // Floodgate API で Bedrock プレイヤーを確認
            FloodgateApi floodgateApi = FloodgateApi.getInstance();
            if (!floodgateApi.isFloodgatePlayer(reporter.getUniqueId())) {
                // Java版プレイヤー：チャット入力
                reporter.sendMessage(plugin.getMessageManager().getMessage("report.detail-input"));
                return;
            }

            // ReportTypeを取得
            ReportType reportType = (ReportType) playerData.getOrDefault("reportType", ReportType.OTHER);
            String categoryName = plugin.getMessageManager().getMessage("type." + reportType.name());
            if (categoryName == null || categoryName.startsWith("type.")) {
                categoryName = reportType.name();
            }

            // Cumulus CustomForm を構築
            CustomForm.Builder formBuilder = CustomForm.builder()
                    .title("通報詳細入力")
                    .label("被通報者: " + playerData.getOrDefault("targetName", "不明"))
                    .label("カテゴリ: " + categoryName)
                    .input("詳細を入力してください（省略可）", "詳細（0～500文字、省略可能）");

            // フォーム応答ハンドラを設定
            formBuilder.validResultHandler(response -> {
                // CustomFormResponse.asInput(2) で3番目のフィールド（詳細）を取得
                String detail = response.asInput(2);

                // 詳細の処理（空でもOK）
                if (detail != null && detail.length() > 500) {
                    reporter.sendMessage(plugin.getMessageManager().getMessage("report.invalid-reason-length"));
                    return;
                }

                // 通報を送信
                submitReport(reporter, reportType, detail, playerData);
            });

            // フォーム送信エラーハンドラ
            formBuilder.invalidResultHandler(response -> {
                plugin.getLogger().warning("Form の応答が無効です: " + reporter.getName());
            });

            // フォームを送信
            CustomForm form = formBuilder.build();
            floodgateApi.sendForm(reporter.getUniqueId(), form);

        } catch (NoClassDefFoundError e) {
            // Floodgate/Cumulus が導入されていない
            plugin.getLogger().warning("Bedrock Form API (Floodgate/Cumulus) が見つかりません。");
        } catch (Exception e) {
            plugin.getLogger().warning("ReportReasonInputForm エラー (" + reporter.getName() + "): " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 通報を送信
     */
    private void submitReport(Player reporter, ReportType reportType, String detail, Map<String, Object> data) {
        Player targetPlayer = (Player) data.get("targetPlayer");
        if (targetPlayer == null || !targetPlayer.isOnline()) {
            reporter.sendMessage(plugin.getMessageManager().getMessage("common.player-not-found",
                    Map.of("player", (String) data.getOrDefault("targetName", "不明"))));
            return;
        }

        // 詳細が空の場合はカテゴリ名のみを使用
        String reason;
        if (detail == null || detail.trim().isEmpty()) {
            reason = reportType.name();
        } else {
            reason = reportType.name() + ": " + detail;
        }

        // ReportCategoryGUI と同じロジックを実行
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                Report report = new Report(
                        reporter.getUniqueId(),
                        reporter.getName(),
                        targetPlayer.getUniqueId(),
                        targetPlayer.getName(),
                        reason,
                        reportType
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
                notifyStaff(reportId, reporter.getName(), targetPlayer.getName(), reason);

                // Discord通知
                report.setId(reportId);
                plugin.getDiscordWebhookManager().notifyNewReport(report);

            } catch (Exception e) {
                plugin.getLogger().severe("通報の保存に失敗しました: " + e.getMessage());
                Bukkit.getScheduler().runTask(plugin, () ->
                        reporter.sendMessage(plugin.getMessageManager().getMessage("common.command-error"))
                );
            }
        });

        reporter.closeInventory();
    }

    /**
     * スタッフに通知
     */
    private void notifyStaff(int reportId, String reporterName, String reportedName, String reason) {
        if (!plugin.getConfigManager().isStaffNotificationEnabled()) {
            return;
        }

        Map<String, String> placeholders = Map.of(
                "id", String.valueOf(reportId),
                "reporter", reporterName,
                "reported", reportedName,
                "reason", reason
        );
        String message = plugin.getMessageManager().getMessage("staff.new-report", placeholders);

        Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.hasPermission("kumareport.notify"))
                .forEach(p -> {
                    p.sendMessage(message);
                    plugin.playStaffNotificationSound(p);
                });
    }
}
