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
 * 45スロット（5行）で通報情報と処理ボタンを表示
 */
public class ReportDetailGUI {
    private final KumaReport plugin;
    private static final int INVENTORY_SIZE = 45;

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

            // 背景をガラスで埋める
            fillWithGlass(inventory);

            // 情報表示アイテム
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            // タイトル（最上段中央）
            inventory.setItem(4, createTitleItem(reportId));

            // 1行目: 通報者, 被通報者
            inventory.setItem(11, createInfoItem("§e通報者", report.getReporterName(), 
                    Material.PLAYER_HEAD, Arrays.asList(
                            "§7UUID: §f" + report.getReporterUUID()
                    )));
            inventory.setItem(13, createInfoItem("§c被通報者", report.getReportedName(), 
                    Material.PLAYER_HEAD, Arrays.asList(
                            "§7UUID: §f" + report.getReportedUUID()
                    )));

            String statusKey = "status." + report.getStatus().name();
            String statusText = plugin.getMessageManager().getMessage(statusKey);
            if (statusText == null || statusText.equals(statusKey)) {
                statusText = report.getStatus().name();
            }
            inventory.setItem(15, createInfoItem("§bステータス", statusText, 
                    getMaterialForStatus(report.getStatus()), 
                    Arrays.asList("§7現在の処理状況")));

            // 2行目: 理由、種類、日時
            String typeKey = "type." + report.getReportType().name();
            String typeText = plugin.getMessageManager().getMessage(typeKey);
            if (typeText == null || typeText.equals(typeKey)) {
                typeText = report.getReportType().name();
            }

            inventory.setItem(20, createInfoItem("§6種類", typeText, 
                    Material.REDSTONE, Arrays.asList("§7通報の分類")));

            inventory.setItem(22, createInfoItem("§d理由", 
                    report.getReason().length() > 30 ? 
                            report.getReason().substring(0, 30) + "..." : report.getReason(), 
                    Material.BOOK, Arrays.asList(
                            "§8━━━━━━━━━━━━━━━",
                            "§f" + report.getReason(),
                            "§8━━━━━━━━━━━━━━━"
                    )));

            inventory.setItem(24, createInfoItem("§a日時", sdf.format(report.getCreatedAt()), 
                    Material.CLOCK, Arrays.asList("§7通報された日時")));

            // 3行目: ボタン
            if (report.getStatus() == ReportStatus.PENDING || report.getStatus() == ReportStatus.IN_PROGRESS) {
                inventory.setItem(37, createActionButton("✓ 承認", Material.LIME_CONCRETE));
                inventory.setItem(38, createActionButton("✗ 却下", Material.RED_CONCRETE));
            } else {
                inventory.setItem(37, createDisabledButton("承認済み"));
                inventory.setItem(38, createDisabledButton("却下済み"));
            }

            inventory.setItem(43, createBackButton());

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
        if (slot == 37) {
            handleApprove(player, reportId);
            return;
        }

        // 却下ボタン
        if (slot == 38) {
            handleDeny(player, reportId);
            return;
        }

        // 戻るボタン
        if (slot == 43) {
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
     * タイトルアイテムを作成
     */
    private ItemStack createTitleItem(int reportId) {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName("§6§l通報詳細 #" + reportId);
            meta.setLore(Arrays.asList(
                    "§8━━━━━━━━━━━━━━━",
                    "§7詳細情報を確認して",
                    "§7処理を行ってください",
                    "§8━━━━━━━━━━━━━━━"
            ));
            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * 情報アイテムを作成
     */
    private ItemStack createInfoItem(String label, String value, Material material, List<String> extraLore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(label);
            List<String> lore = new ArrayList<>();
            lore.add("§8━━━━━━━━━━━━━━━");
            lore.add("§f" + value);
            if (extraLore != null && !extraLore.isEmpty()) {
                lore.add("");
                lore.addAll(extraLore);
            }
            lore.add("§8━━━━━━━━━━━━━━━");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * ステータスに応じたマテリアルを取得
     */
    private Material getMaterialForStatus(ReportStatus status) {
        return switch (status) {
            case PENDING -> Material.YELLOW_STAINED_GLASS;
            case IN_PROGRESS -> Material.CYAN_STAINED_GLASS;
            case ACCEPTED -> Material.LIME_STAINED_GLASS;
            case DENIED -> Material.RED_STAINED_GLASS;
        };
    }

    /**
     * アクションボタンを作成
     */
    private ItemStack createActionButton(String displayName, Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName("§f§l" + displayName);
            meta.setLore(Arrays.asList(
                    "§8━━━━━━━━━━━━━━━",
                    "§7クリックして実行",
                    "§8━━━━━━━━━━━━━━━"
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
        ItemStack item = new ItemStack(Material.RED_CONCRETE);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName("§c§l← 戻る");
            meta.setLore(Arrays.asList(
                    "§7通報一覧に戻る"
            ));
            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * 背景をガラスで埋める
     */
    private void fillWithGlass(Inventory inventory) {
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = glass.getItemMeta();

        if (meta != null) {
            meta.setDisplayName("§8");
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
