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
import java.util.*;

/**
 * 通報一覧GUI（スタッフ用）
 * 54スロット（6行）でページネーション付きの通報一覧を表示
 */
public class ReportListGUI {
    private final KumaReport plugin;
    private static final int INVENTORY_SIZE = 54;
    private static final int ITEMS_PER_PAGE = 45; // 最後の行はナビゲーション用

    public ReportListGUI(KumaReport plugin) {
        this.plugin = plugin;
    }

    /**
     * GUIを開く
     */
    public void open(Player player) {
        open(player, 1, ReportStatus.PENDING);
    }

    /**
     * GUIを開く（ページ・ステータス指定）
     */
    public void open(Player player, int page, ReportStatus filterStatus) {
        try {
            List<Report> allReports = plugin.getDatabaseManager().getReportDAO()
                    .getReportsByStatus(filterStatus);

            int totalPages = (allReports.size() + ITEMS_PER_PAGE - 1) / ITEMS_PER_PAGE;
            if (page < 1) page = 1;
            if (page > totalPages) page = totalPages;

            String title = "通報一覧 - " + plugin.getMessageManager().getMessage("status." + filterStatus.name());
            Inventory inventory = Bukkit.createInventory(null, INVENTORY_SIZE, title);

            // 通報アイテムを追加
            int startIndex = (page - 1) * ITEMS_PER_PAGE;
            int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, allReports.size());

            for (int i = startIndex; i < endIndex; i++) {
                Report report = allReports.get(i);
                inventory.addItem(createReportItem(report));
            }

            // 背景アイテム（黒いガラス）を埋める
            fillWithGlass(inventory, ITEMS_PER_PAGE);

            // ナビゲーションボタン
            if (page > 1) {
                inventory.setItem(45, createNavigationButton("\\« 前へ", Material.ARROW));
            } else {
                inventory.setItem(45, createDisabledButton());
            }

            // ステータスフィルタ情報
            inventory.setItem(49, createFilterInfo(filterStatus, page, totalPages));

            // 次へボタン
            if (page < totalPages) {
                inventory.setItem(53, createNavigationButton("次へ \\»", Material.ARROW));
            } else {
                inventory.setItem(53, createDisabledButton());
            }

            player.openInventory(inventory);

            // GUI状態を設定
            GUIListener listener = getGUIListener();
            if (listener != null) {
                Map<String, Object> data = new HashMap<>();
                data.put("page", page);
                data.put("totalPages", totalPages);
                data.put("reports", allReports);
                data.put("filterStatus", filterStatus);
                listener.setGUIState(player, "REPORT_LIST");
                listener.setGUIData(player, data);
            }

        } catch (SQLException e) {
            plugin.getLogger().severe("通報一覧取得エラー: " + e.getMessage());
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

        int currentPage = (int) data.getOrDefault("page", 1);
        int totalPages = (int) data.getOrDefault("totalPages", 1);
        ReportStatus filterStatus = (ReportStatus) data.getOrDefault("filterStatus", ReportStatus.PENDING);
        @SuppressWarnings("unchecked")
        List<Report> reports = (List<Report>) data.getOrDefault("reports", new ArrayList<>());

        // 前へボタン
        if (slot == 45 && currentPage > 1) {
            open(player, currentPage - 1, filterStatus);
            return;
        }

        // 次へボタン
        if (slot == 53 && currentPage < totalPages) {
            open(player, currentPage + 1, filterStatus);
            return;
        }

        // 通報アイテム選択
        if (slot < ITEMS_PER_PAGE) {
            int startIndex = (currentPage - 1) * ITEMS_PER_PAGE;
            int reportIndex = startIndex + slot;

            if (reportIndex < reports.size()) {
                Report report = reports.get(reportIndex);
                ReportDetailGUI detailGUI = new ReportDetailGUI(plugin);
                detailGUI.open(player, report.getId());
            }
        }
    }

    /**
     * 通報アイテムを作成
     */
    private ItemStack createReportItem(Report report) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            String statusKey = "status." + report.getStatus().name();
            String statusText = plugin.getMessageManager().getMessage(statusKey);
            if (statusText == null || statusText.equals(statusKey)) {
                statusText = report.getStatus().name();
            }

            meta.setDisplayName("§e§l#" + report.getId() + " §8» §f" + report.getReportedName());

            List<String> lore = new ArrayList<>();
            lore.add("§8━━━━━━━━━━━━━━━");
            lore.add("§7通報者: §f" + report.getReporterName());
            lore.add("§7ステータス: " + statusText);
            lore.add("§7理由: §f" + (report.getReason().length() > 20 ? 
                    report.getReason().substring(0, 20) + "..." : report.getReason()));
            lore.add("§8━━━━━━━━━━━━━━━");
            lore.add("");
            lore.add("§a§l▶ §aクリックして詳細を表示");

            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * ナビゲーションボタンを作成
     */
    private ItemStack createNavigationButton(String displayName, Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName("§e§l" + displayName);
            meta.setLore(Arrays.asList(
                    "§7クリックしてページ移動"
            ));
            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * 無効化されたボタンを作成
     */
    private ItemStack createDisabledButton() {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName("§8-");
            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * フィルタ情報アイテムを作成
     */
    private ItemStack createFilterInfo(ReportStatus status, int page, int totalPages) {
        ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            String statusDisplay = plugin.getMessageManager().getMessage("status." + status.name());
            if (statusDisplay == null || statusDisplay.startsWith("status.")) {
                statusDisplay = status.name();
            }

            meta.setDisplayName("§b§lフィルタ情報");
            meta.setLore(Arrays.asList(
                    "§8━━━━━━━━━━━━━━━",
                    "§7ステータス: " + statusDisplay,
                    "§7ページ: §f" + page + " §8/ §f" + totalPages,
                    "§8━━━━━━━━━━━━━━━"
            ));
            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * 背景をガラスで埋める
     */
    private void fillWithGlass(Inventory inventory, int fillUpTo) {
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = glass.getItemMeta();

        if (meta != null) {
            meta.setDisplayName("§8");
            glass.setItemMeta(meta);
        }

        for (int i = 0; i < fillUpTo; i++) {
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
