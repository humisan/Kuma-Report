package lol.hanyuu.kumaReport.manager;

import lol.hanyuu.kumaReport.KumaReport;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * メッセージ管理クラス
 * messages.ymlの読み込み、プレースホルダー置換、16進数カラーコード変換を担当
 */
public class MessageManager {
    private final KumaReport plugin;
    private FileConfiguration messagesConfig;
    private final Map<String, String> messageCache;

    // 16進数カラーコードのパターン (&#RRGGBB)
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    public MessageManager(KumaReport plugin) {
        this.plugin = plugin;
        this.messageCache = new HashMap<>();
        loadMessages();
    }

    /**
     * messages.ymlをロード
     */
    public void loadMessages() {
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");

        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }

        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        messageCache.clear();

        plugin.getLogger().info("messages.ymlをロードしました。");
    }

    /**
     * メッセージを取得（キーから）
     *
     * @param key メッセージキー（例: "report.success"）
     * @return 処理済みメッセージ
     */
    public String getMessage(String key) {
        return getMessage(key, new HashMap<>());
    }

    /**
     * メッセージを取得してプレースホルダーを置換
     *
     * @param key メッセージキー
     * @param placeholders プレースホルダーのマップ
     * @return 処理済みメッセージ
     */
    public String getMessage(String key, Map<String, String> placeholders) {
        String message = getCachedMessage(key);

        if (message == null) {
            return key; // キーが見つからない場合はキーをそのまま返す
        }

        // プレフィックスを置換
        if (message.contains("{prefix}")) {
            String prefix = getCachedMessage("prefix");
            message = message.replace("{prefix}", prefix != null ? prefix : "");
        }

        // プレースホルダーを置換
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", entry.getValue());
        }

        return message;
    }

    /**
     * キャッシュからメッセージを取得（なければ読み込み＆変換）
     */
    private String getCachedMessage(String key) {
        if (messageCache.containsKey(key)) {
            return messageCache.get(key);
        }

        String rawMessage = messagesConfig.getString(key);
        if (rawMessage == null) {
            return null;
        }

        // 16進数カラーコード変換
        String processedMessage = translateHexColorCodes(rawMessage);
        messageCache.put(key, processedMessage);

        return processedMessage;
    }

    /**
     * 16進数カラーコード (&#RRGGBB) をMinecraft形式に変換
     *
     * @param message 変換対象のメッセージ
     * @return 変換後のメッセージ
     */
    public String translateHexColorCodes(String message) {
        // 16進数カラーコード機能が無効の場合は通常の&コード変換のみ
        if (!plugin.getConfig().getBoolean("colors.use-hex-colors", true)) {
            return ChatColor.translateAlternateColorCodes('&', message);
        }

        // デバッグモードの場合はカラーコードを削除
        if (plugin.getConfig().getBoolean("colors.debug-mode", false)) {
            String plainText = message.replaceAll("&#[A-Fa-f0-9]{6}", "");
            return ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', plainText));
        }

        // 16進数カラーコードを変換
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String hexCode = matcher.group(1);
            StringBuilder magic = new StringBuilder("§x");

            // 16進数の各文字を§で区切る
            for (char c : hexCode.toCharArray()) {
                magic.append('§').append(c);
            }

            matcher.appendReplacement(buffer, magic.toString());
        }

        matcher.appendTail(buffer);

        // 通常の&コードも変換
        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }

    /**
     * ConfigManager用のヘルパー：設定から文字列リストを取得して変換
     */
    public String translateHexColorCodes(java.util.List<String> lines) {
        StringBuilder result = new StringBuilder();
        for (String line : lines) {
            if (result.length() > 0) {
                result.append("\n");
            }
            result.append(translateHexColorCodes(line));
        }
        return result.toString();
    }

    /**
     * キャッシュをクリア
     */
    public void clearCache() {
        messageCache.clear();
    }

    /**
     * メッセージをリロード
     */
    public void reload() {
        loadMessages();
        plugin.getLogger().info("メッセージをリロードしました。");
    }
}
