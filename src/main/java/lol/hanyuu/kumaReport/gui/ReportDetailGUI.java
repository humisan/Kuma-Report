package lol.hanyuu.kumaReport.gui;

import lol.hanyuu.kumaReport.KumaReport;
import lol.hanyuu.kumaReport.listener.GUIListener;
import lol.hanyuu.kumaReport.model.Report;
import lol.hanyuu.kumaReport.model.ReportStatus;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 通報詳細・処理GUI（スタッフ用）
 * 27スロット（3行）で通報情報と処理ボタンを表示
 */
public class ReportDetailGUI {
    private final KumaReport plugin;
    private static final int INVENTORY_SIZE = 27;

    public ReportDetailGUI(KumaReport plugin) {
        this.plugin = plugin;
    }

    /**
     * GUIを開く
     */
    public void open(Player player, int reportId) {
        try {
            Report report = plugin.getDatabaseManager().getReportDAO().getReportById(reportId);
            if (report == null) {
                player.sendMessage(plugin.getMessageManager().getMessage("admin.report-not-found",
                        Map.of("id", String.valueOf(reportId))));
                return;
            }

            String title = "通報 #" + reportId;
            Inventory inventory = Bukkit.createInventory(null, INVENTORY_SIZE, title);

            // 情報表示アイテム
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            // 1行目: 通報者, 被通報者, ステータス
            inventory.setItem(0, createInfoItem("通報者", report.getReporterName(), Material.PLAYER_HEAD));
            inventory.setItem(1, createInfoItem("被通報者", report.getReportedName(), Material.PLAYER_HEAD));

            String statusKey = "status." + report.getStatus().name();
            String statusText = plugin.getMessageManager().getMessage(statusKey);
            if (statusText == null || statusText.equals(statusKey)) {
                statusText = report.getStatus().name();
            }
            inventory.setItem(2, createInfoItem("ステータス", statusText, Material.DIAMOND));

            // 2行目: 理由、種類、日時
            inventory.setItem(9, createInfoItem("理由", report.getReason(), Material.BOOK));

            String typeKey = "type." + report.getReportType().name();
            String typeText = plugin.getMessageManager().getMessage(typeKey);
            if (typeText == null || typeText.equals(typeKey)) {
                typeText = report.getReportType().name();
            }
            inventory.setItem(10, createInfoItem("種類", typeText, Material.REDSTONE));
            inventory.setItem(11, createInfoItem("日時", sdf.format(report.getCreatedAt()), Material.CLOCK));

            // 背景をガラスで埋める
            fillWithGlass(inventory);

            // 3行目: ボタン
            if (report.getStatus() == ReportStatus.PENDING || report.getStatus() == ReportStatus.IN_PROGRESS) {
                inventory.setItem(18, createActionButton("✓ 承認", Material.LIME_WOOL));
                inventory.setItem(19, createActionButton("✗ 却下", Material.RED_WOOL));
            } else {
                inventory.setItem(18, createDisabledButton("承認済み"));
                inventory.setItem(19, createDisabledButton("却下済み"));
            }

            inventory.setItem(26, createBackButton());

            player.openInventory(inventory);

            // GUI状態を設定
            GUIListener listener = getGUIListener();
            if (listener != null) {
                Map<String, Object> data = new HashMap<>();
                data.put("reportId", reportId);
                data.put("report", report);
                listener.setGUIState(player, "REPORT_DETAIL");
                listener.setGUIData(player, data);
            }

        } catch (SQLException e) {
            plugin.getLogger().severe("通報詳細取得エラー: " + e.getMessage());
            player.sendMessage(plugin.getMessageManager().getMessage("common.command-error"));
        }
    }

    /**
     * クリック処理
     */
    public void handleClick(Player player, int slot, Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            return;
        }

        int reportId = (int) data.getOrDefault("reportId", -1);
        if (reportId == -1) {
            return;
        }

        // 承認ボタン
        if (slot == 18) {
            handleApprove(player, reportId);
            return;
        }

        // 却下ボタン
        if (slot == 19) {
            handleDeny(player, reportId);
            return;
        }

        // 戻るボタン
        if (slot == 26) {
            ReportListGUI listGUI = new ReportListGUI(plugin);
            listGUI.open(player);
            return;
        }
    }

    /**
     * 承認処理
     */
    private void handleApprove(Player player, int reportId) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                // 承認前のレポートを取得
                Report reportBefore = plugin.getDatabaseManager().getReportDAO().getReportById(reportId);

                String note = ""; // GUIからのメモは簡易実装のため未実装
                boolean success = plugin.getDatabaseManager().getReportDAO()
                        .updateReportStatus(reportId, ReportStatus.ACCEPTED,
                                player.getUniqueId(), player.getName(), note);

                if (success) {
                    // Discord通知
                    if (reportBefore != null) {
                        Report reportAfter = plugin.getDatabaseManager().getReportDAO().getReportById(reportId);
                        if (reportAfter != null) {
                            plugin.getDiscordWebhookManager().notifyReportApproved(reportAfter);
                        }
                    }

                    Map<String, String> placeholders = Map.of("id", String.valueOf(reportId));
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        player.sendMessage(plugin.getMessageManager().getMessage("admin.accepted", placeholders));
                        open(player, reportId); // GUIを更新
                    });
                } else {
                    Bukkit.getScheduler().runTask(plugin, () ->
                            player.sendMessage(plugin.getMessageManager().getMessage("admin.report-not-found",
                                    Map.of("id", String.valueOf(reportId))))
                    );
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("通報承認エラー: " + e.getMessage());
                Bukkit.getScheduler().runTask(plugin, () ->
                        player.sendMessage(plugin.getMessageManager().getMessage("common.command-error"))
                );
            }
        });
    }

    /**
     * 却下処理
     */
    private void handleDeny(Player player, int reportId) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                // 却下前のレポートを取得
                Report reportBefore = plugin.getDatabaseManager().getReportDAO().getReportById(reportId);

                String note = ""; // GUIからのメモは簡易実装のため未実装
                boolean success = plugin.getDatabaseManager().getReportDAO()
                        .updateReportStatus(reportId, ReportStatus.DENIED,
                                player.getUniqueId(), player.getName(), note);

                if (success) {
                    // Discord通知
                    if (reportBefore != null) {
                        Report reportAfter = plugin.getDatabaseManager().getReportDAO().getReportById(reportId);
                        if (reportAfter != null) {
                            plugin.getDiscordWebhookManager().notifyReportDenied(reportAfter);
                        }
                    }

                    Map<String, String> placeholders = Map.of("id", String.valueOf(reportId));
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        player.sendMessage(plugin.getMessageManager().getMessage("admin.denied", placeholders));
                        open(player, reportId); // GUIを更新
                    });
                } else {
                    Bukkit.getScheduler().runTask(plugin, () ->
                            player.sendMessage(plugin.getMessageManager().getMessage("admin.report-not-found",
                                    Map.of("id", String.valueOf(reportId))))
                    );
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("通報却下エラー: " + e.getMessage());
                Bukkit.getScheduler().runTask(plugin, () ->
                        player.sendMessage(plugin.getMessageManager().getMessage("common.command-error"))
                );
            }
        });
    }

    /**
     * 情報アイテムを作成
     */
    private ItemStack createInfoItem(String label, String value, Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName("§b" + label);
            meta.setLore(Arrays.asList(
                    "§7" + value
            ));
            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * アクションボタンを作成
     */
    private ItemStack createActionButton(String displayName, Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName("§l" + displayName);
            meta.setLore(Arrays.asList(
                    "§7クリックして実行"
            ));
            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * 無効化されたボタンを作成
     */
    private ItemStack createDisabledButton(String displayName) {
        ItemStack item = new ItemStack(Material.GRAY_WOOL);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName("§8" + displayName);
            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * 戻るボタンを作成
     */
    private ItemStack createBackButton() {
        ItemStack item = new ItemStack(Material.RED_WOOL);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName("§c§l← 戻る");
            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * 背景をガラスで埋める
     */
    private void fillWithGlass(Inventory inventory) {
        ItemStack glass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = glass.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(" ");
            glass.setItemMeta(meta);
        }

        for (int i = 0; i < INVENTORY_SIZE; i++) {
            if (inventory.getItem(i) == null || inventory.getItem(i).getType() == Material.AIR) {
                inventory.setItem(i, glass);
            }
        }
    }

    /**
     * GUIListenerを取得
     */
    private GUIListener getGUIListener() {
        return plugin.getGUIListener();
    }
}
