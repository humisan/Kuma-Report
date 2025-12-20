package lol.hanyuu.kumaReport.gui;

import lol.hanyuu.kumaReport.KumaReport;
import lol.hanyuu.kumaReport.listener.GUIListener;
import lol.hanyuu.kumaReport.model.BugReport;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.SQLException;
import java.util.*;

/**
 * バグレポート説明入力 GUI
 * `/bugreport` コマンド実行時に開く
 * 45スロット（5行）で説明入力フィールドを表示
 */
public class BugReportGUI {
    private final KumaReport plugin;
    private static final int INVENTORY_SIZE = 45;

    public BugReportGUI(KumaReport plugin) {
        this.plugin = plugin;
    }

    /**
     * GUIを開く
     */
    public void open(Player player) {
        String title = plugin.getMessageManager().getMessage("bugreport.gui-title");
        Inventory inventory = Bukkit.createInventory(null, INVENTORY_SIZE, title);

        // 背景を埋める
        fillWithGlass(inventory);

        // タイトルデコレーション（上段）
        inventory.setItem(4, createTitleItem());

        // 説明アイテム（2段目中央）
        inventory.setItem(11, createInfoItem("§e§l手順 1", Material.WRITABLE_BOOK, Arrays.asList(
                "§7確定ボタンをクリックして",
                "§7入力モードを開始します"
        )));

        inventory.setItem(13, createInfoItem("§e§l手順 2", Material.BOOK, Arrays.asList(
                "§7チャットにバグの詳細を",
                "§7入力してください",
                "§8(最低10文字、最大1000文字)"
        )));

        inventory.setItem(15, createInfoItem("§e§l手順 3", Material.COMPASS, Arrays.asList(
                "§7現在地の座標が",
                "§7自動的に記録されます",
                "§8(X: " + (int)player.getLocation().getX() + 
                ", Y: " + (int)player.getLocation().getY() + 
                ", Z: " + (int)player.getLocation().getZ() + ")"
        )));

        // 注意事項（3段目）
        inventory.setItem(20, createWarningItem("§c§l重要", Material.RED_STAINED_GLASS_PANE, Arrays.asList(
                "§7再現手順を詳しく",
                "§7説明してください"
        )));

        inventory.setItem(22, createWarningItem("§6§lヒント", Material.ORANGE_STAINED_GLASS_PANE, Arrays.asList(
                "§7いつ・どこで・何をしたら",
                "§7バグが発生したか記載"
        )));

        inventory.setItem(24, createWarningItem("§a§l感謝", Material.LIME_STAINED_GLASS_PANE, Arrays.asList(
                "§7バグ報告は",
                "§7サーバー改善に役立ちます"
        )));

        // 確定ボタン（下段左寄り）
        inventory.setItem(38, createConfirmButton());

        // キャンセルボタン（下段右寄り）
        inventory.setItem(42, createCancelButton());

        player.openInventory(inventory);

        // GUI状態を設定
        GUIListener listener = getGUIListener();
        if (listener != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("inputMode", "awaiting_description");
            listener.setGUIState(player, "BUG_REPORT");
            listener.setGUIData(player, data);
        }
    }

    /**
     * クリック処理
     */
    public void handleClick(Player player, int slot, Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            return;
        }

        // 確定ボタン
        if (slot == 38) {
            player.closeInventory();
            player.sendMessage(plugin.getMessageManager().getMessage("bugreport.input-prompt"));
            // ChatListenerでチャット入力を待つ
            plugin.getChatListener().startBugReportInput(player);
            return;
        }

        // キャンセルボタン
        if (slot == 42) {
            player.closeInventory();
            player.sendMessage(plugin.getMessageManager().getMessage("report.reason-cancelled"));
            // 入力待機状態をクリア
            plugin.getChatListener().clearInputState(player);
            return;
        }
    }

    /**
     * タイトルアイテムを作成
     */
    private ItemStack createTitleItem() {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName("§6§l✦ バグレポート ✦");
            meta.setLore(Arrays.asList(
                    "§8━━━━━━━━━━━━━━━",
                    "§7バグの報告にご協力",
                    "§7いただきありがとうございます",
                    "§8━━━━━━━━━━━━━━━"
            ));
            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * 情報アイテムを作成
     */
    private ItemStack createInfoItem(String displayName, Material material, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(displayName);
            List<String> finalLore = new ArrayList<>();
            finalLore.add("§8━━━━━━━━━━━━━━━");
            finalLore.addAll(lore);
            finalLore.add("§8━━━━━━━━━━━━━━━");
            meta.setLore(finalLore);
            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * 警告アイテムを作成
     */
    private ItemStack createWarningItem(String displayName, Material material, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(displayName);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * 確定ボタンを作成
     */
    private ItemStack createConfirmButton() {
        ItemStack item = new ItemStack(Material.LIME_CONCRETE);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName("§a§l✓ 確定して入力開始");
            meta.setLore(Arrays.asList(
                    "§8━━━━━━━━━━━━━━━",
                    "§7クリックするとチャット",
                    "§7入力モードに移行します",
                    "§8━━━━━━━━━━━━━━━",
                    "",
                    "§a§l▶ クリックして続行"
            ));
            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * キャンセルボタンを作成
     */
    private ItemStack createCancelButton() {
        ItemStack item = new ItemStack(Material.RED_CONCRETE);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName("§c§l✕ キャンセル");
            meta.setLore(Arrays.asList(
                    "§8━━━━━━━━━━━━━━━",
                    "§7バグレポートを",
                    "§7キャンセルします",
                    "§8━━━━━━━━━━━━━━━"
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
