package lol.hanyuu.kumaReport.gui;

import lol.hanyuu.kumaReport.KumaReport;
import lol.hanyuu.kumaReport.listener.GUIListener;
import lol.hanyuu.kumaReport.model.ReportType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * 通報理由カテゴリ選択GUI
 * 45スロット（5行）で5つのカテゴリを表示
 */
public class ReportCategoryGUI {
    private final KumaReport plugin;
    private static final int INVENTORY_SIZE = 45;

    // カテゴリ定義
    private static final Map<Integer, CategoryInfo> CATEGORIES = new LinkedHashMap<>();

    static {
        CATEGORIES.put(0, new CategoryInfo(ReportType.CHEAT, Material.BARRIER, 11));
        CATEGORIES.put(1, new CategoryInfo(ReportType.HARASSMENT, Material.TNT, 13));
        CATEGORIES.put(2, new CategoryInfo(ReportType.CHAT_ABUSE, Material.WRITABLE_BOOK, 15));
        CATEGORIES.put(3, new CategoryInfo(ReportType.GRIEFING, Material.DIAMOND_PICKAXE, 21));
        CATEGORIES.put(4, new CategoryInfo(ReportType.OTHER, Material.PAPER, 23));
    }

    public ReportCategoryGUI(KumaReport plugin) {
        this.plugin = plugin;
    }

    /**
     * GUIを開く
     */
    public void open(Player player) {
        String title = plugin.getMessageManager().getMessage("report.category-select");
        Inventory inventory = Bukkit.createInventory(null, INVENTORY_SIZE, title);

        // 背景をガラスで埋める
        fillWithGlass(inventory);

        // タイトルアイテム
        inventory.setItem(4, createTitleItem());

        // カテゴリアイテムを配置
        for (Map.Entry<Integer, CategoryInfo> entry : CATEGORIES.entrySet()) {
            CategoryInfo category = entry.getValue();
            inventory.setItem(category.slot, createCategoryItem(category.type));
        }

        // 戻るボタン
        inventory.setItem(40, createBackButton());

        player.openInventory(inventory);

        // GUI状態を設定
        GUIListener listener = getGUIListener();
        if (listener != null) {
            listener.setGUIState(player, "CATEGORY_SELECT");
        }
    }

    /**
     * クリック処理
     */
    public void handleClick(Player player, int slot, Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            return;
        }

        // 戻るボタン
        if (slot == 40) {
            ReportPlayerGUI playerGUI = new ReportPlayerGUI(plugin);
            playerGUI.open(player);
            return;
        }

        // カテゴリ選択
        for (Map.Entry<Integer, CategoryInfo> entry : CATEGORIES.entrySet()) {
            CategoryInfo category = entry.getValue();
            if (category.slot == slot) {
                if (category.type == ReportType.OTHER) {
                    // 「その他」の場合はチャット入力待機へ
                    openChatInputMode(player, data);
                } else {
                    // 他のカテゴリはそのまま通報を送信
                    submitReport(player, category.type, "", data);
                }
                return;
            }
        }
    }

    /**
     * チャット入力モードを開始
     */
    private void openChatInputMode(Player player, Map<String, Object> data) {
        player.closeInventory();
        player.sendMessage(plugin.getMessageManager().getMessage("report.reason-input"));

        // TODO: チャットリスナーを実装して30秒のタイムアウト処理
        // 一時的に簡易実装
        GUIListener listener = getGUIListener();
        if (listener != null) {
            Map<String, Object> newData = new HashMap<>(data);
            newData.put("waitingForReason", true);
            newData.put("reportType", ReportType.OTHER);
            listener.setGUIData(player, newData);
        }
    }

    /**
     * 通報を送信
     */
    private void submitReport(Player reporter, ReportType reportType, String customReason, Map<String, Object> data) {
        Player targetPlayer = (Player) data.get("targetPlayer");
        if (targetPlayer == null) {
            reporter.sendMessage(plugin.getMessageManager().getMessage("common.player-not-found",
                    Map.of("player", (String) data.getOrDefault("targetName", "不明"))));
            return;
        }

        String reason = customReason.isEmpty() ? reportType.name() : customReason;

        // 通報処理をReportCommandに委譲
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
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
                Map<String, String> placeholders = Map.of("player", targetPlayer.getName());
                Bukkit.getScheduler().runTask(plugin, () ->
                        reporter.sendMessage(plugin.getMessageManager().getMessage("report.success", placeholders))
                );

                // スタッフに通知
                notifyStaff(reporter.getName(), targetPlayer.getName(), reason);

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

    /**
     * タイトルアイテムを作成
     */
    private ItemStack createTitleItem() {
        ItemStack item = new ItemStack(Material.NAME_TAG);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName("§e§l通報理由を選択");
            meta.setLore(Arrays.asList(
                    "§8━━━━━━━━━━━━━━━",
                    "§7該当する理由を",
                    "§7クリックしてください",
                    "§8━━━━━━━━━━━━━━━"
            ));
            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * カテゴリアイテムを作成
     */
    private ItemStack createCategoryItem(ReportType type) {
        Material material = getCategoryMaterial(type);
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            String typeKey = "type." + type.name();
            String typeDisplay = plugin.getMessageManager().getMessage(typeKey);
            meta.setDisplayName("§f§l" + typeDisplay);
            
            List<String> lore = new ArrayList<>();
            lore.add("§8━━━━━━━━━━━━━━━");
            lore.addAll(getCategoryDescription(type));
            lore.add("§8━━━━━━━━━━━━━━━");
            lore.add("");
            lore.add("§a§l▶ §aクリックして選択");
            
            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * カテゴリの説明を取得
     */
    private List<String> getCategoryDescription(ReportType type) {
        return switch (type) {
            case CHEAT -> Arrays.asList(
                    "§7不正なMOD、ハック、",
                    "§7X-Rayなどの使用"
            );
            case HARASSMENT -> Arrays.asList(
                    "§7他プレイヤーへの",
                    "§7嫌がらせ行為"
            );
            case CHAT_ABUSE -> Arrays.asList(
                    "§7暴言、スパム、",
                    "§7不適切な発言"
            );
            case GRIEFING -> Arrays.asList(
                    "§7建築物の破壊、",
                    "§7荒らし行為"
            );
            case OTHER -> Arrays.asList(
                    "§7上記に該当しない",
                    "§7その他の違反行為",
                    "§8(詳細を入力できます)"
            );
        };
    }

    /**
     * カテゴリのマテリアルを取得
     */
    private Material getCategoryMaterial(ReportType type) {
        return switch (type) {
            case CHEAT -> Material.BARRIER;
            case HARASSMENT -> Material.TNT;
            case CHAT_ABUSE -> Material.WRITABLE_BOOK;
            case GRIEFING -> Material.DIAMOND_PICKAXE;
            case OTHER -> Material.PAPER;
        };
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
                    "§7プレイヤー選択に戻る"
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

    /**
     * カテゴリ情報
     */
    private static class CategoryInfo {
        ReportType type;
        Material material;
        int slot;

        CategoryInfo(ReportType type, Material material, int slot) {
            this.type = type;
            this.material = material;
            this.slot = slot;
        }
    }
}
