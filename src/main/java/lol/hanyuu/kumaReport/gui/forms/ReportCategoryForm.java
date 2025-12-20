package lol.hanyuu.kumaReport.gui.forms;

import lol.hanyuu.kumaReport.KumaReport;
import lol.hanyuu.kumaReport.model.ReportType;
import org.bukkit.entity.Player;
import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.floodgate.api.FloodgateApi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 通報理由カテゴリ選択 Form（Bedrock版用）
 * SimpleForm で 5 つのカテゴリを表示
 */
public class ReportCategoryForm {
    private final KumaReport plugin;

    public ReportCategoryForm(KumaReport plugin) {
        this.plugin = plugin;
    }

    /**
     * Form を開く
     */
    public void open(Player player, Map<String, Object> playerData) {
        try {
            List<String> buttons = new ArrayList<>();

            // カテゴリをボタンで追加
            buttons.add("チート使用");
            buttons.add("迷惑行為");
            buttons.add("不適切発言");
            buttons.add("荒らし行為");
            buttons.add("その他");
            buttons.add("戻る");
            buttons.add("キャンセル");

            sendSimpleForm(player, "通報理由を選択", "被通報者への通報理由を選択してください", buttons,
                    (clickedButton) -> {
                        if (clickedButton == -1) {
                            // キャンセル
                            return;
                        }

                        String buttonText = buttons.get(clickedButton);

                        if (buttonText.equals("戻る")) {
                            new ReportPlayerForm(plugin).open(player);
                            return;
                        }

                        if (buttonText.equals("キャンセル")) {
                            player.sendMessage(plugin.getMessageManager().getMessage("report.reason-cancelled"));
                            return;
                        }

                        // カテゴリを決定
                        ReportType reportType = getReportTypeFromButton(buttonText);

                        if (reportType == ReportType.OTHER) {
                            // 「その他」の場合：テキスト入力 Form へ
                            new ReportReasonInputForm(plugin).open(player, playerData);
                        } else {
                            // 他のカテゴリ：そのまま通報を送信
                            submitReport(player, reportType, "", playerData);
                        }
                    });

        } catch (Exception e) {
            plugin.getLogger().warning("ReportCategoryForm エラー: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * ボタンテキストから ReportType を取得
     */
    private ReportType getReportTypeFromButton(String buttonText) {
        return switch (buttonText) {
            case "チート使用" -> ReportType.CHEAT;
            case "迷惑行為" -> ReportType.HARASSMENT;
            case "不適切発言" -> ReportType.CHAT_ABUSE;
            case "荒らし行為" -> ReportType.GRIEFING;
            default -> ReportType.OTHER;
        };
    }

    /**
     * 通報を送信
     */
    private void submitReport(Player reporter, ReportType reportType, String customReason, Map<String, Object> data) {
        Player targetPlayer = (Player) data.get("targetPlayer");
        if (targetPlayer == null || !targetPlayer.isOnline()) {
            reporter.sendMessage(plugin.getMessageManager().getMessage("common.player-not-found",
                    java.util.Map.of("player", (String) data.getOrDefault("targetName", "不明"))));
            return;
        }

        String reason = customReason.isEmpty() ? reportType.name() : customReason;

        // ReportCategoryGUI と同じロジックを実行
        org.bukkit.Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                lol.hanyuu.kumaReport.model.Report report = new lol.hanyuu.kumaReport.model.Report(
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
                java.util.Map<String, String> placeholders = java.util.Map.of("player", targetPlayer.getName());
                org.bukkit.Bukkit.getScheduler().runTask(plugin, () ->
                        reporter.sendMessage(plugin.getMessageManager().getMessage("report.success", placeholders))
                );

                // スタッフに通知
                notifyStaff(reporter.getName(), targetPlayer.getName(), reason);

                // Discord通知
                report.setId(reportId);
                plugin.getDiscordWebhookManager().notifyNewReport(report);

            } catch (Exception e) {
                plugin.getLogger().severe("通報の保存に失敗しました: " + e.getMessage());
                org.bukkit.Bukkit.getScheduler().runTask(plugin, () ->
                        reporter.sendMessage(plugin.getMessageManager().getMessage("common.command-error"))
                );
            }
        });

        reporter.closeInventory();
    }

    /**
     * スタッフに通知
     */
    private void notifyStaff(String reporterName, String reportedName, String reason) {
        if (!plugin.getConfigManager().isStaffNotificationEnabled()) {
            return;
        }

        java.util.Map<String, String> placeholders = java.util.Map.of(
                "reporter", reporterName,
                "reported", reportedName,
                "reason", reason
        );
        String message = plugin.getMessageManager().getMessage("staff.new-report", placeholders);

        org.bukkit.Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.hasPermission("kumareport.notify"))
                .forEach(p -> p.sendMessage(message));
    }

    /**
     * SimpleForm を送信
     */
    private void sendSimpleForm(Player player, String title, String content, List<String> buttons,
                                java.util.function.Consumer<Integer> responseHandler) {
        try {
            // Floodgate API で Bedrock プレイヤーを確認
            FloodgateApi floodgateApi = FloodgateApi.getInstance();
            if (!floodgateApi.isFloodgatePlayer(player.getUniqueId())) {
                // Java版プレイヤー：GUI を使用
                plugin.getLogger().info("Java Edition プレイヤー: " + player.getName() + " のため GUI を使用します");
                // Java版には別のGUIを使用
                return;
            }

            // Cumulus SimpleForm を構築
            SimpleForm.Builder formBuilder = SimpleForm.builder()
                    .title(title)
                    .content(content);

            // ボタンを追加
            for (String buttonText : buttons) {
                formBuilder.button(buttonText);
            }

            // フォーム応答ハンドラを設定
            formBuilder.validResultHandler(response -> {
                // SimpleFormResponse.clickedButtonId() で押されたボタンのインデックスを取得
                int clickedButton = response.clickedButtonId();
                responseHandler.accept(clickedButton);
            });

            // フォーム送信エラーハンドラ
            formBuilder.invalidResultHandler(response -> {
                plugin.getLogger().warning("Form の応答が無効です: " + player.getName());
            });

            // フォームを送信
            SimpleForm form = formBuilder.build();
            floodgateApi.sendForm(player.getUniqueId(), form);

        } catch (NoClassDefFoundError e) {
            // Floodgate/Cumulus が導入されていない
            plugin.getLogger().warning("Bedrock Form API (Floodgate/Cumulus) が見つかりません。");
        } catch (Exception e) {
            plugin.getLogger().warning("Form 送信エラー (" + player.getName() + "): " + e.getMessage());
            e.printStackTrace();
        }
    }
}
