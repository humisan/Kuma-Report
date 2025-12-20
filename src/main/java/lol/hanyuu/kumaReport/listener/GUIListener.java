package lol.hanyuu.kumaReport.listener;

import lol.hanyuu.kumaReport.KumaReport;
import lol.hanyuu.kumaReport.gui.BugReportGUI;
import lol.hanyuu.kumaReport.gui.ReportCategoryGUI;
import lol.hanyuu.kumaReport.gui.ReportDetailGUI;
import lol.hanyuu.kumaReport.gui.ReportListGUI;
import lol.hanyuu.kumaReport.gui.ReportPlayerGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;

/**
 * GUI操作イベントハンドラ
 * プレイヤーのインベントリクリック処理とGUI状態管理を行う
 */
public class GUIListener implements Listener {
    private final KumaReport plugin;

    // GUI状態管理: Player UUID → GUIクラス名
    private final Map<String, String> playerGUIState = new HashMap<>();

    // GUI状態管理: Player UUID → 一時データ
    private final Map<String, Map<String, Object>> playerGUIData = new HashMap<>();

    public GUIListener(KumaReport plugin) {
        this.plugin = plugin;
    }

    /**
     * インベントリクリックイベント
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        Inventory inventory = event.getInventory();
        String playerUUID = player.getUniqueId().toString();

        // GUIであるかチェック
        String guiState = playerGUIState.get(playerUUID);
        if (guiState == null) {
            return;
        }

        // クリックをキャンセル（カスタムハンドリング）
        event.setCancelled(true);

        // GUIの種類に応じた処理
        switch (guiState) {
            case "PLAYER_SELECT" -> {
                ReportPlayerGUI gui = new ReportPlayerGUI(plugin);
                gui.handleClick(player, event.getSlot(), playerGUIData.get(playerUUID));
            }
            case "CATEGORY_SELECT" -> {
                ReportCategoryGUI gui = new ReportCategoryGUI(plugin);
                gui.handleClick(player, event.getSlot(), playerGUIData.get(playerUUID));
            }
            case "REPORT_LIST" -> {
                ReportListGUI gui = new ReportListGUI(plugin);
                gui.handleClick(player, event.getSlot(), playerGUIData.get(playerUUID));
            }
            case "REPORT_DETAIL" -> {
                ReportDetailGUI gui = new ReportDetailGUI(plugin);
                gui.handleClick(player, event.getSlot(), playerGUIData.get(playerUUID));
            }
            case "BUG_REPORT" -> {
                BugReportGUI gui = new BugReportGUI(plugin);
                gui.handleClick(player, event.getSlot(), playerGUIData.get(playerUUID));
            }
        }
    }

    /**
     * インベントリクローズイベント
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getPlayer();
        String playerUUID = player.getUniqueId().toString();

        // GUI状態とデータをクリーンアップ
        playerGUIState.remove(playerUUID);
        playerGUIData.remove(playerUUID);
    }

    /**
     * GUIの状態を設定
     */
    public void setGUIState(Player player, String state) {
        playerGUIState.put(player.getUniqueId().toString(), state);
    }

    /**
     * GUIの一時データを設定
     */
    public void setGUIData(Player player, Map<String, Object> data) {
        playerGUIData.put(player.getUniqueId().toString(), data);
    }

    /**
     * GUIの一時データを取得
     */
    public Map<String, Object> getGUIData(Player player) {
        return playerGUIData.getOrDefault(player.getUniqueId().toString(), new HashMap<>());
    }

    /**
     * GUI状態をクリア
     */
    public void clearGUIState(Player player) {
        String playerUUID = player.getUniqueId().toString();
        playerGUIState.remove(playerUUID);
        playerGUIData.remove(playerUUID);
    }
}
