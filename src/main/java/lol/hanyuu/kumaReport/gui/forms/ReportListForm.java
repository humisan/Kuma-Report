package lol.hanyuu.kumaReport.gui.forms;

import lol.hanyuu.kumaReport.KumaReport;
import lol.hanyuu.kumaReport.model.Report;
import lol.hanyuu.kumaReport.model.ReportStatus;
import org.bukkit.entity.Player;
import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.floodgate.api.FloodgateApi;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 通報一覧 Form（Bedrock版スタッフ用）
 * SimpleForm でページネーション付きの通報一覧を表示
 */
public class ReportListForm {
    private final KumaReport plugin;
    private static final int REPORTS_PER_PAGE = 10;

    public ReportListForm(KumaReport plugin) {
        this.plugin = plugin;
    }

    /**
     * Form を開く
     */
    public void open(Player player) {
        open(player, 1, ReportStatus.PENDING);
    }

    /**
     * Form を開く（ページ・ステータス指定）
     */
    public void open(Player player, int page, ReportStatus filterStatus) {
        try {
            List<Report> allReports = plugin.getDatabaseManager().getReportDAO()
                    .getReportsByStatus(filterStatus);

            int totalPages = (allReports.size() + REPORTS_PER_PAGE - 1) / REPORTS_PER_PAGE;
            if (page < 1) page = 1;
            if (page > totalPages) page = totalPages;
            final int currentPage = page;
            final int finalTotalPages = totalPages;

            List<String> buttons = new ArrayList<>();

            // このページの通報を追加
            int startIndex = (page - 1) * REPORTS_PER_PAGE;
            int endIndex = Math.min(startIndex + REPORTS_PER_PAGE, allReports.size());

            for (int i = startIndex; i < endIndex; i++) {
                Report report = allReports.get(i);
                String statusDisplay = plugin.getMessageManager().getMessage("status." + report.getStatus().name());
                if (statusDisplay == null || statusDisplay.startsWith("status.")) {
                    statusDisplay = report.getStatus().name();
                }
                // カラーコードを削除
                statusDisplay = stripColorCodes(statusDisplay);

                buttons.add("#" + report.getId() + " " + report.getReportedName() + " (" + statusDisplay + ")");
            }

            // ナビゲーション
            if (currentPage > 1) {
                buttons.add("◄ 前へ");
            }
            if (currentPage < finalTotalPages) {
                buttons.add("次へ ►");
            }
            buttons.add("戻る");

            String statusDisplay = plugin.getMessageManager().getMessage("status." + filterStatus.name());
            if (statusDisplay == null || statusDisplay.startsWith("status.")) {
                statusDisplay = filterStatus.name();
            }
            statusDisplay = stripColorCodes(statusDisplay);
            final String finalStatusDisplay = statusDisplay;

            sendSimpleForm(player, "通報一覧 - " + finalStatusDisplay, "ページ " + currentPage + "/" + finalTotalPages, buttons,
                    (clickedButton) -> {
                        if (clickedButton == -1) {
                            return;
                        }

                        String buttonText = buttons.get(clickedButton);

                        if (buttonText.equals("◄ 前へ")) {
                            open(player, currentPage - 1, filterStatus);
                            return;
                        }

                        if (buttonText.equals("次へ ►")) {
                            open(player, currentPage + 1, filterStatus);
                            return;
                        }

                        if (buttonText.equals("戻る")) {
                            return;
                        }

                        // 通報を選択
                        try {
                            int reportId = Integer.parseInt(buttonText.split(" ")[0].substring(1));
                            new ReportDetailForm(plugin).open(player, reportId);
                        } catch (Exception e) {
                            plugin.getLogger().warning("通報選択エラー: " + e.getMessage());
                        }
                    });

        } catch (SQLException e) {
            plugin.getLogger().severe("通報一覧取得エラー: " + e.getMessage());
            player.sendMessage(plugin.getMessageManager().getMessage("common.command-error"));
        }
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
