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
 * 27スロット（3行）で説明入力フィールドを表示
 */
public class BugReportGUI {
    private final KumaReport plugin;
    private static final int INVENTORY_SIZE = 27;

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

        // 説明テキスト（中央上部）
        inventory.setItem(10, createInfoItem("バグの説明を入力", Material.WRITABLE_BOOK, Arrays.asList(
                "§7確定ボタンをクリック後、",
                "§7チャットにバグの説明を",
                "§7入力してください"
        )));

        inventory.setItem(13, createInfoItem("最低10文字", Material.BOOK, Arrays.asList(
                "§7最大1000文字まで",
                "§7入力可能です"
        )));

        inventory.setItem(16, createInfoItem("座標は自動記録", Material.COMPASS, Arrays.asList(
                "§7現在位置の座標が",
                "§7自動的に記録されます"
        )));

        // 確定ボタン（下段中央）
        inventory.setItem(22, createConfirmButton());

        // キャンセルボタン（下段右）
        inventory.setItem(26, createCancelButton());

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
        if (slot == 22) {
            player.closeInventory();
            player.sendMessage(plugin.getMessageManager().getMessage("bugreport.input-prompt"));
            // プレイヤーがチャットで説明を入力するのを待つ
            // AsyncPlayerChatEventリスナーで処理される
            return;
        }

        // キャンセルボタン
        if (slot == 26) {
            player.closeInventory();
            player.sendMessage(plugin.getMessageManager().getMessage("report.reason-cancelled"));
            return;
        }
    }

    /**
     * 情報アイテムを作成
     */
    private ItemStack createInfoItem(String displayName, Material material, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName("§f§l" + displayName);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * 確定ボタンを作成
     */
    private ItemStack createConfirmButton() {
        ItemStack item = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName("§a§l確定");
            meta.setLore(Arrays.asList(
                    "§7クリックしてチャット入力モードへ"
            ));
            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * キャンセルボタンを作成
     */
    private ItemStack createCancelButton() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName("§c§lキャンセル");
            meta.setLore(Arrays.asList(
                    "§7バグレポートをキャンセル"
            ));
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
