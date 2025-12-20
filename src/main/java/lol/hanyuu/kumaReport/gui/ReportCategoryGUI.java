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
        // 既存のGUIデータを保存（インベントリを開くとクリアされるため）
        GUIListener listener = getGUIListener();
        Map<String, Object> existingData = new HashMap<>();
        if (listener != null) {
            existingData = listener.getGUIData(player);
        }

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

        // GUI状態とデータを設定（既存データを保持）
        if (listener != null) {
            listener.setGUIState(player, "CATEGORY_SELECT");
            if (!existingData.isEmpty()) {
                listener.setGUIData(player, existingData);
            }
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
                // 全てのカテゴリで詳細入力オプションを提供
                openChatInputMode(player, category.type, data);
                return;
            }
        }
    }

    /**
     * チャット入力モードを開始
     */
    private void openChatInputMode(Player player, ReportType reportType, Map<String, Object> data) {
        player.closeInventory();
        
        Player targetPlayer = (Player) data.get("targetPlayer");
        if (targetPlayer == null) {
            player.sendMessage(plugin.getMessageManager().getMessage("common.player-not-found",
                    Map.of("player", (String) data.getOrDefault("targetName", "不明"))));
            return;
        }

        // 詳細入力のプロンプトを表示
        player.sendMessage(plugin.getMessageManager().getMessage("report.detail-input"));
        
        // ChatListenerで詳細入力を待つ
        plugin.getChatListener().startReportDetailInput(player, targetPlayer, reportType);
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
            lore.add("§7クリック後、詳細を入力できます");
            
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
                    "§7その他の違反行為"
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
