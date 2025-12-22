package lol.hanyuu.kumaReport;

import lol.hanyuu.kumaReport.command.BugReportCommand;
import lol.hanyuu.kumaReport.command.ReportAdminCommand;
import lol.hanyuu.kumaReport.command.ReportCommand;
import lol.hanyuu.kumaReport.database.DatabaseManager;
import lol.hanyuu.kumaReport.listener.ChatListener;
import lol.hanyuu.kumaReport.listener.GUIListener;
import lol.hanyuu.kumaReport.listener.PlayerJoinListener;
import lol.hanyuu.kumaReport.manager.ConfigManager;
import lol.hanyuu.kumaReport.manager.CooldownManager;
import lol.hanyuu.kumaReport.manager.DiscordWebhookManager;
import lol.hanyuu.kumaReport.manager.MessageManager;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Kuma-Report メインクラス
 * 日本人向けMinecraft Earthサーバー用通報プラグイン
 */
public final class KumaReport extends JavaPlugin {

    private static KumaReport instance;

    // マネージャー
    private ConfigManager configManager;
    private MessageManager messageManager;
    private DatabaseManager databaseManager;
    private CooldownManager cooldownManager;
    private DiscordWebhookManager discordWebhookManager;

    // リスナー
    private GUIListener guiListener;
    private ChatListener chatListener;

    @Override
    public void onEnable() {
        instance = this;

        getLogger().info("Kuma-Report を起動しています...");

        // 設定ファイルの初期化
        configManager = new ConfigManager(this);
        configManager.loadConfigs();

        // メッセージマネージャー初期化
        messageManager = new MessageManager(this);

        // データベース初期化
        databaseManager = new DatabaseManager(this);
        databaseManager.initializeDatabase();

        // クールダウンマネージャー初期化
        cooldownManager = new CooldownManager(this);

        // コマンド登録
        registerCommands();

        // リスナー登録
        registerListeners();

        // Discord Webhook マネージャー初期化
        discordWebhookManager = new DiscordWebhookManager(this);

        getLogger().info("Kuma-Report が有効化されました！");
    }

    @Override
    public void onDisable() {
        // データベース接続を閉じる
        if (databaseManager != null) {
            databaseManager.closeConnection();
        }

        getLogger().info("Kuma-Report が無効化されました。");
    }

    /**
     * コマンドを登録
     */
    private void registerCommands() {
        // /report コマンド
        ReportCommand reportCmd = new ReportCommand(this);
        getCommand("report").setExecutor(reportCmd);
        getCommand("report").setTabCompleter(reportCmd);

        // /bugreport コマンド
        BugReportCommand bugReportCmd = new BugReportCommand(this);
        getCommand("bugreport").setExecutor(bugReportCmd);
        getCommand("bugreport").setTabCompleter(bugReportCmd);

        // /reportadmin コマンド
        ReportAdminCommand adminCmd = new ReportAdminCommand(this);
        getCommand("reportadmin").setExecutor(adminCmd);
        getCommand("reportadmin").setTabCompleter(adminCmd);

        getLogger().info("コマンドを登録しました。");
    }

    /**
     * リスナーを登録
     */
    private void registerListeners() {
        // GUI操作リスナー
        guiListener = new GUIListener(this);
        getServer().getPluginManager().registerEvents(guiListener, this);

        // チャット入力リスナー
        chatListener = new ChatListener(this);
        getServer().getPluginManager().registerEvents(chatListener, this);

        // プレイヤー参加リスナー
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);

        getLogger().info("リスナーを登録しました。");
    }

    /**
     * プラグインインスタンスを取得
     */
    public static KumaReport getInstance() {
        return instance;
    }

    /**
     * ConfigManagerを取得
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }

    /**
     * MessageManagerを取得
     */
    public MessageManager getMessageManager() {
        return messageManager;
    }

    /**
     * DatabaseManagerを取得
     */
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    /**
     * CooldownManagerを取得
     */
    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }

    /**
     * GUIListenerを取得
     */
    public GUIListener getGUIListener() {
        return guiListener;
    }

    /**
     * DiscordWebhookManagerを取得
     */
    public DiscordWebhookManager getDiscordWebhookManager() {
        return discordWebhookManager;
    }

    /**
     * ChatListenerを取得
     */
    public ChatListener getChatListener() {
        return chatListener;
    }

    /**
     * スタッフ通知音を再生
     */
    public void playStaffNotificationSound(Player player) {
        if (!configManager.isSoundEnabled()) {
            return;
        }

        String soundName = configManager.getSoundType();
        Sound sound;
        try {
            sound = Sound.valueOf(soundName);
        } catch (IllegalArgumentException e) {
            getLogger().warning("無効な通知音が設定されています: " + soundName);
            return;
        }

        float volume = configManager.getSoundVolume();
        float pitch = configManager.getSoundPitch();
        if (volume < 0.0f) {
            volume = 0.0f;
        }
        if (pitch < 0.0f) {
            pitch = 0.0f;
        } else if (pitch > 2.0f) {
            pitch = 2.0f;
        }

        player.playSound(player.getLocation(), sound, volume, pitch);
    }
}
