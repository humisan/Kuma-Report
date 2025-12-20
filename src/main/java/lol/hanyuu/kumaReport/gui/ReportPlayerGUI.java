package lol.hanyuu.kumaReport.gui;

import lol.hanyuu.kumaReport.KumaReport;
import lol.hanyuu.kumaReport.listener.GUIListener;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

/**
 * プレイヤー選択GUI
 * `/report` コマンド実行時に開く
 * 54スロット（6行）でオンラインプレイヤーを表示
 */
public class ReportPlayerGUI {
    private final KumaReport plugin;
    private static final int INVENTORY_SIZE = 54;
    private static final int ITEMS_PER_PAGE = 45; // 最後の行はナビゲーション用

    public ReportPlayerGUI(KumaReport plugin) {
        this.plugin = plugin;
    }

    /**
     * GUIを開く
     */
    public void open(Player player) {
        open(player, 1);
    }

    /**
     * GUIを開く（ページ指定）
     */
    public void open(Player player, int page) {
        List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
        // 自分自身を除外
        onlinePlayers.removeIf(p -> p.getUniqueId().equals(player.getUniqueId()));

        // プレイヤーがいない場合の処理
        if (onlinePlayers.isEmpty()) {
            player.sendMessage(plugin.getMessageManager().getMessage("common.player-not-found",
                    Map.of("player", "other")));
            return;
        }

        int totalPages = (onlinePlayers.size() + ITEMS_PER_PAGE - 1) / ITEMS_PER_PAGE;
        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;

        String title = plugin.getMessageManager().getMessage("report.gui-title");
        Inventory inventory = Bukkit.createInventory(null, INVENTORY_SIZE, title);

        // プレイヤーアイテムを追加
        int startIndex = (page - 1) * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, onlinePlayers.size());

        for (int i = startIndex; i < endIndex; i++) {
            Player targetPlayer = onlinePlayers.get(i);
            inventory.addItem(createPlayerSkull(targetPlayer));
        }

        // 背景アイテム（黒いガラス）を埋める
        fillWithGlass(inventory, ITEMS_PER_PAGE);

        // ナビゲーションボタン
        if (page > 1) {
            inventory.setItem(45, createNavigationButton("« 前へ", Material.ARROW));
        } else {
            inventory.setItem(45, createDisabledButton());
        }

        // ページ情報
        inventory.setItem(49, createPageInfo(page, totalPages));

        // 次へボタン
        if (page < totalPages) {
            inventory.setItem(53, createNavigationButton("次へ »", Material.ARROW));
        } else {
            inventory.setItem(53, createDisabledButton());
        }

        // キャンセルボタン
        inventory.setItem(47, createCancelButton());

        player.openInventory(inventory);

        // GUI状態を設定
        GUIListener listener = getGUIListener();
        if (listener != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("page", page);
            data.put("totalPages", totalPages);
            data.put("onlinePlayers", onlinePlayers);
            listener.setGUIState(player, "PLAYER_SELECT");
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

        int currentPage = (int) data.getOrDefault("page", 1);
        int totalPages = (int) data.getOrDefault("totalPages", 1);
        @SuppressWarnings("unchecked")
        List<Player> onlinePlayers = (List<Player>) data.getOrDefault("onlinePlayers", new ArrayList<>());

        // 前へボタン
        if (slot == 45 && currentPage > 1) {
            open(player, currentPage - 1);
            return;
        }

        // 次へボタン
        if (slot == 53 && currentPage < totalPages) {
            open(player, currentPage + 1);
            return;
        }

        // キャンセルボタン
        if (slot == 47) {
            player.closeInventory();
            player.sendMessage(plugin.getMessageManager().getMessage("report.reason-cancelled"));
            return;
        }

        // プレイヤー選択
        if (slot < ITEMS_PER_PAGE) {
            int startIndex = (currentPage - 1) * ITEMS_PER_PAGE;
            int playerIndex = startIndex + slot;

            if (playerIndex < onlinePlayers.size()) {
                Player targetPlayer = onlinePlayers.get(playerIndex);

                // 次のGUIへ移動
                GUIListener listener = getGUIListener();
                if (listener != null) {
                    Map<String, Object> newData = new HashMap<>();
                    newData.put("targetPlayer", targetPlayer);
                    newData.put("targetUUID", targetPlayer.getUniqueId());
                    newData.put("targetName", targetPlayer.getName());
                    listener.setGUIData(player, newData);
                    listener.setGUIState(player, "CATEGORY_SELECT");
                }

                ReportCategoryGUI categoryGUI = new ReportCategoryGUI(plugin);
                categoryGUI.open(player);
            }
        }
    }

    /**
     * プレイヤーのスカルアイテムを作成
     */
    private ItemStack createPlayerSkull(Player player) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();

        if (meta != null) {
            meta.setOwningPlayer(player);
            meta.setDisplayName("§f" + player.getName());
            meta.setLore(Arrays.asList(
                    "§7クリックして選択"
            ));
            skull.setItemMeta(meta);
        }

        return skull;
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
     * ページ情報アイテムを作成
     */
    private ItemStack createPageInfo(int page, int totalPages) {
        ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName("§b§lページ情報");
            meta.setLore(Arrays.asList(
                    "§7現在のページ: §f" + page + " §8/ §f" + totalPages,
                    "",
                    "§7矢印をクリックして",
                    "§7ページを移動できます"
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
            meta.setDisplayName("§c§l✕ キャンセル");
            meta.setLore(Arrays.asList(
                    "§7通報をキャンセルして",
                    "§7GUIを閉じます"
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
