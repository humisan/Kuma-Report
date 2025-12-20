package lol.hanyuu.kumaReport.gui.forms;

import lol.hanyuu.kumaReport.KumaReport;
import lol.hanyuu.kumaReport.model.Report;
import lol.hanyuu.kumaReport.model.ReportStatus;
import org.bukkit.entity.Player;
import org.geysermc.cumulus.form.ModalForm;
import org.geysermc.floodgate.api.FloodgateApi;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 通報詳細・処理 Form（Bedrock版スタッフ用）
 * ModalForm で承認/却下ボタンを表示
 */
public class ReportDetailForm {
    private final KumaReport plugin;

    public ReportDetailForm(KumaReport plugin) {
        this.plugin = plugin;
    }

    /**
     * Form を開く
     */
    public void open(Player player, int reportId) {
        try {
            Report report = plugin.getDatabaseManager().getReportDAO().getReportById(reportId);
            if (report == null) {
                player.sendMessage(plugin.getMessageManager().getMessage("admin.report-not-found",
                        Map.of("id", String.valueOf(reportId))));
                return;
            }

            // 詳細情報を構築
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            String typeKey = "type." + report.getReportType().name();
            String typeDisplay = plugin.getMessageManager().getMessage(typeKey);
            if (typeDisplay == null || typeDisplay.startsWith("type.")) {
                typeDisplay = report.getReportType().name();
            }
            typeDisplay = stripColorCodes(typeDisplay);

            String statusKey = "status." + report.getStatus().name();
            String statusDisplay = plugin.getMessageManager().getMessage(statusKey);
            if (statusDisplay == null || statusDisplay.startsWith("status.")) {
                statusDisplay = report.getStatus().name();
            }
            statusDisplay = stripColorCodes(statusDisplay);

            StringBuilder content = new StringBuilder();
            content.append("通報ID: #").append(report.getId()).append("\n");
            content.append("通報者: ").append(report.getReporterName()).append("\n");
            content.append("被通報者: ").append(report.getReportedName()).append("\n");
            content.append("理由: ").append(report.getReason()).append("\n");
            content.append("種類: ").append(typeDisplay).append("\n");
            content.append("ステータス: ").append(statusDisplay).append("\n");
            content.append("日時: ").append(sdf.format(report.getCreatedAt())).append("\n");

            if (report.getHandlerName() != null) {
                content.append("処理者: ").append(report.getHandlerName()).append("\n");
                if (report.getHandledAt() != null) {
                    content.append("処理日時: ").append(sdf.format(report.getHandledAt())).append("\n");
                }
            }

            // ボタンを準備
            List<String> buttons = new ArrayList<>();

            if (report.getStatus() == ReportStatus.PENDING || report.getStatus() == ReportStatus.IN_PROGRESS) {
                buttons.add("✓ 承認");
                buttons.add("✗ 却下");
            } else {
                buttons.add("戻る");
            }

            sendModalForm(player, "通報詳細", content.toString(), buttons,
                    (clickedButton) -> {
                        if (clickedButton == -1) {
                            return;
                        }

                        String buttonText = buttons.get(clickedButton);

                        if (buttonText.equals("✓ 承認")) {
                            handleApprove(player, reportId);
                            return;
                        }

                        if (buttonText.equals("✗ 却下")) {
                            handleDeny(player, reportId);
                            return;
                        }

                        if (buttonText.equals("戻る")) {
                            new ReportListForm(plugin).open(player);
                        }
                    });

        } catch (SQLException e) {
            plugin.getLogger().severe("通報詳細取得エラー: " + e.getMessage());
            player.sendMessage(plugin.getMessageManager().getMessage("common.command-error"));
        }
    }

    /**
     * 承認処理
     */
    private void handleApprove(Player player, int reportId) {
        org.bukkit.Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                Report reportBefore = plugin.getDatabaseManager().getReportDAO().getReportById(reportId);

                boolean success = plugin.getDatabaseManager().getReportDAO()
                        .updateReportStatus(reportId, ReportStatus.ACCEPTED,
                                player.getUniqueId(), player.getName(), "");

                if (success && reportBefore != null) {
                    Report reportAfter = plugin.getDatabaseManager().getReportDAO().getReportById(reportId);
                    if (reportAfter != null) {
                        plugin.getDiscordWebhookManager().notifyReportApproved(reportAfter);
                    }

                    Map<String, String> placeholders = Map.of("id", String.valueOf(reportId));
                    org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> {
                        player.sendMessage(plugin.getMessageManager().getMessage("admin.accepted", placeholders));
                        open(player, reportId);
                    });
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("通報承認エラー: " + e.getMessage());
            }
        });
    }

    /**
     * 却下処理
     */
    private void handleDeny(Player player, int reportId) {
        org.bukkit.Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                Report reportBefore = plugin.getDatabaseManager().getReportDAO().getReportById(reportId);

                boolean success = plugin.getDatabaseManager().getReportDAO()
                        .updateReportStatus(reportId, ReportStatus.DENIED,
                                player.getUniqueId(), player.getName(), "");

                if (success && reportBefore != null) {
                    Report reportAfter = plugin.getDatabaseManager().getReportDAO().getReportById(reportId);
                    if (reportAfter != null) {
                        plugin.getDiscordWebhookManager().notifyReportDenied(reportAfter);
                    }

                    Map<String, String> placeholders = Map.of("id", String.valueOf(reportId));
                    org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> {
                        player.sendMessage(plugin.getMessageManager().getMessage("admin.denied", placeholders));
                        open(player, reportId);
                    });
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("通報却下エラー: " + e.getMessage());
            }
        });
    }

    /**
     * Minecraft カラーコードを削除
     */
    private String stripColorCodes(String str) {
        if (str == null) {
            return "";
        }
        return str.replaceAll("(?i)&[0-9A-F]", "")
                .replaceAll("(?i)&#[0-9A-F]{6}", "")
                .replaceAll("§x", "")
                .replaceAll("(?i)§[0-9A-F]", "");
    }

    /**
     * ModalForm を送信
     */
    private void sendModalForm(Player player, String title, String content, List<String> buttons,
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

            // Cumulus ModalForm を構築
            // ModalForm は左右2つのボタンを表示するため、buttons.get(0) と buttons.get(1) を使用
            String leftButton = buttons.size() > 0 ? buttons.get(0) : "キャンセル";
            String rightButton = buttons.size() > 1 ? buttons.get(1) : "戻る";

            ModalForm.Builder formBuilder = ModalForm.builder()
                    .title(title)
                    .content(content)
                    .button1(leftButton)
                    .button2(rightButton);

            // フォーム応答ハンドラを設定
            formBuilder.validResultHandler(response -> {
                // ModalFormResponse.clickedButtonId() で 0=左ボタン, 1=右ボタン
                int clickedButton = response.clickedButtonId();
                responseHandler.accept(clickedButton);
            });

            // フォーム送信エラーハンドラ
            formBuilder.invalidResultHandler(response -> {
                plugin.getLogger().warning("Form の応答が無効です: " + player.getName());
            });

            // フォームを送信
            ModalForm form = formBuilder.build();
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
