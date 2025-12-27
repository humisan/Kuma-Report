package lol.hanyuu.kumaReport.gui.forms;

import lol.hanyuu.kumaReport.KumaReport;
import lol.hanyuu.kumaReport.gui.ReportCategoryGUI;
import lol.hanyuu.kumaReport.gui.ReportListGUI;
import lol.hanyuu.kumaReport.gui.ReportPlayerGUI;
import org.bukkit.entity.Player;

/**
 * Form 管理クラス
 * Bedrock版（Floodgate）プレイヤーの判定と Form/GUI の切り替え
 */
public class FormManager {
    private final KumaReport plugin;

    public FormManager(KumaReport plugin) {
        this.plugin = plugin;
    }

    /**
     * プレイヤーが Bedrock 版かどうかを判定
     * Floodgate が導入されていない場合は全員 Java 版として扱う
     */
    public boolean isBedrockPlayer(Player player) {
        try {
            // Floodgate API を使用してプレイヤー判定
            Class<?> floodgateApiClass = Class.forName("org.geysermc.floodgate.api.FloodgateApi");
            java.lang.reflect.Method getInstanceMethod = floodgateApiClass.getMethod("getInstance");
            Object floodgateApi = getInstanceMethod.invoke(null);

            java.lang.reflect.Method isFloodgatePlayerMethod = floodgateApiClass.getMethod("isFloodgatePlayer", java.util.UUID.class);
            return (boolean) isFloodgatePlayerMethod.invoke(floodgateApi, player.getUniqueId());
        } catch (Exception e) {
            // Floodgate が導入されていない場合は Java 版として扱う
            return false;
        }
    }

    /**
     * 通報 GUI/Form を開く
     */
    public void openReportGUI(Player player) {
        if (isBedrockPlayer(player)) {
            // Bedrock版: Form API を使用
            new ReportPlayerForm(plugin).open(player);
        } else {
            // Java版: Inventory GUI を使用
            new ReportPlayerGUI(plugin).open(player);
        }
    }

    /**
     * スタッフ用管理 GUI/Form を開く
     */
    public void openStaffGUI(Player player) {
        if (isBedrockPlayer(player)) {
            // Bedrock版: Form API を使用
            new ReportListForm(plugin).open(player);
        } else {
            // Java版: Inventory GUI を使用
            new ReportListGUI(plugin).open(player);
        }
    }
}
