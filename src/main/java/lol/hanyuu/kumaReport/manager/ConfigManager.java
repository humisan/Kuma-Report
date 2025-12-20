package lol.hanyuu.kumaReport.manager;

import lol.hanyuu.kumaReport.KumaReport;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * 設定管理クラス
 * config.ymlとmessages.ymlの読み込み・管理を担当
 */
public class ConfigManager {
    private final KumaReport plugin;

    public ConfigManager(KumaReport plugin) {
        this.plugin = plugin;
    }

    /**
     * 設定ファイルをロード
     */
    public void loadConfigs() {
        // プラグインフォルダを作成
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        // config.ymlを保存（存在しない場合）
        plugin.saveDefaultConfig();

        plugin.getLogger().info("設定ファイルをロードしました。");
    }

    /**
     * config.ymlを取得
     */
    public FileConfiguration getConfig() {
        return plugin.getConfig();
    }

    /**
     * config.ymlをリロード
     */
    public void reloadConfig() {
        plugin.reloadConfig();
        plugin.getLogger().info("config.ymlをリロードしました。");
    }

    // === 設定値取得用メソッド ===

    /**
     * プレイヤー通報のクールダウン時間を取得（秒）
     */
    public int getReportCooldown() {
        return getConfig().getInt("cooldown.player-report", 60);
    }

    /**
     * バグレポートのクールダウン時間を取得（秒）
     */
    public int getBugReportCooldown() {
        return getConfig().getInt("cooldown.bug-report", 120);
    }

    /**
     * スタッフ通知が有効かどうか
     */
    public boolean isStaffNotificationEnabled() {
        return getConfig().getBoolean("notifications.enable-staff-notification", true);
    }

    /**
     * 通知音が有効かどうか
     */
    public boolean isSoundEnabled() {
        return getConfig().getBoolean("notifications.play-sound", true);
    }

    /**
     * 通知音の種類を取得
     */
    public String getSoundType() {
        return getConfig().getString("notifications.sound-type", "BLOCK_NOTE_BLOCK_PLING");
    }

    /**
     * Discord連携が有効かどうか
     */
    public boolean isDiscordEnabled() {
        return getConfig().getBoolean("discord.enabled", false);
    }

    /**
     * Discord Webhook URLを取得
     */
    public String getDiscordWebhookUrl() {
        return getConfig().getString("discord.webhook-url", "");
    }

    /**
     * Discord メンションロールIDを取得
     */
    public String getDiscordMentionRoleId() {
        return getConfig().getString("discord.mention-role-id", "");
    }

    /**
     * 1ページあたりのアイテム表示数を取得
     */
    public int getItemsPerPage() {
        return getConfig().getInt("gui.items-per-page", 45);
    }

    /**
     * GUIタイトルを取得
     */
    public String getGuiTitle(String type) {
        return getConfig().getString("gui.title." + type, "");
    }

    /**
     * デバッグモードが有効かどうか
     */
    public boolean isDebugEnabled() {
        return getConfig().getBoolean("debug", false);
    }

    /**
     * データベースのパスを取得
     */
    public String getDatabasePath() {
        return getConfig().getString("database.path", "plugins/Kuma-Report/reports.db");
    }

    /**
     * データベースのタイプを取得
     */
    public String getDatabaseType() {
        return getConfig().getString("database.type", "sqlite");
    }
}
