package lol.hanyuu.kumaReport.gui.forms;

import lol.hanyuu.kumaReport.KumaReport;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.floodgate.api.FloodgateApi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * プレイヤー選択 Form（Bedrock版用）
 * SimpleForm でオンラインプレイヤーをボタンで表示
 */
public class ReportPlayerForm {
    private final KumaReport plugin;
    private static final int PLAYERS_PER_PAGE = 10;

    public ReportPlayerForm(KumaReport plugin) {
        this.plugin = plugin;
    }

    /**
     * Form を開く
     */
    public void open(Player player) {
        open(player, 1);
    }

    /**
     * Form を開く（ページ指定）
     */
    public void open(Player player, int page) {
        try {
            List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
            // 自分自身を除外
            onlinePlayers.removeIf(p -> p.getUniqueId().equals(player.getUniqueId()));

            // プレイヤーがいない場合の処理
            if (onlinePlayers.isEmpty()) {
                player.sendMessage(plugin.getMessageManager().getMessage("common.player-not-found",
                        Map.of("player", "other")));
                return;
            }

            int totalPages = (onlinePlayers.size() + PLAYERS_PER_PAGE - 1) / PLAYERS_PER_PAGE;
            if (page < 1) page = 1;
            if (page > totalPages) page = totalPages;
            final int currentPage = page;
            final int finalTotalPages = totalPages;

            // SimpleForm を構築
            StringBuilder formContent = new StringBuilder();
            formContent.append("プレイヤーを選択してください\n\n");

            List<String> buttons = new ArrayList<>();

            // このページのプレイヤーを追加
            int startIndex = (currentPage - 1) * PLAYERS_PER_PAGE;
            int endIndex = Math.min(startIndex + PLAYERS_PER_PAGE, onlinePlayers.size());

            for (int i = startIndex; i < endIndex; i++) {
                Player targetPlayer = onlinePlayers.get(i);
                buttons.add(targetPlayer.getName());
            }

            // ナビゲーション
            if (currentPage > 1) {
                buttons.add("◄ 前へ");
            }
            if (currentPage < finalTotalPages) {
                buttons.add("次へ ►");
            }
            buttons.add("キャンセル");

            // Cumulus API で Form を送信
            sendSimpleForm(player, "プレイヤーを選択", formContent.toString(), buttons,
                    (clickedButton) -> {
                        if (clickedButton == -1) {
                            // キャンセル
                            return;
                        }

                        String buttonText = buttons.get(clickedButton);

                        if (buttonText.equals("◄ 前へ")) {
                            open(player, currentPage - 1);
                            return;
                        }

                        if (buttonText.equals("次へ ►")) {
                            open(player, currentPage + 1);
                            return;
                        }

                        if (buttonText.equals("キャンセル")) {
                            player.sendMessage(plugin.getMessageManager().getMessage("report.reason-cancelled"));
                            return;
                        }

                        // プレイヤー選択
                        Player targetPlayer = Bukkit.getPlayer(buttonText);
                        if (targetPlayer != null && targetPlayer.isOnline()) {
                            Map<String, Object> data = new HashMap<>();
                            data.put("targetPlayer", targetPlayer);
                            data.put("targetUUID", targetPlayer.getUniqueId());
                            data.put("targetName", targetPlayer.getName());

                            // 次の Form へ
                            new ReportCategoryForm(plugin).open(player, data);
                        }
                    });

        } catch (Exception e) {
            plugin.getLogger().warning("ReportPlayerForm エラー: " + e.getMessage());
            e.printStackTrace();
        }
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
