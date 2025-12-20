package lol.hanyuu.kumaReport.manager;

import lol.hanyuu.kumaReport.KumaReport;
import lol.hanyuu.kumaReport.model.Report;
import org.bukkit.Bukkit;

import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;

/**
 * Discord Webhook マネージャー
 * 通報イベントを Discord チャンネルに送信
 */
public class DiscordWebhookManager {
    private final KumaReport plugin;
    private final String webhookUrl;
    private final String mentionRoleId;
    private final boolean enabled;

    public DiscordWebhookManager(KumaReport plugin) {
        this.plugin = plugin;
        this.webhookUrl = plugin.getConfigManager().getDiscordWebhookUrl();
        this.mentionRoleId = plugin.getConfigManager().getDiscordMentionRoleId();
        this.enabled = plugin.getConfigManager().isDiscordEnabled() && isValidWebhookUrl(webhookUrl);

        if (enabled) {
            plugin.getLogger().info("Discord Webhook マネージャーが有効化されました。");
        }
    }

    /**
     * 通報発生時の通知を送信
     */
    public void notifyNewReport(Report report) {
        if (!enabled) {
            return;
        }

        // 非同期で送信
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                String embed = buildReportEmbed(report);
                sendWebhook(embed);
            } catch (Exception e) {
                plugin.getLogger().warning("Discord 通知送信エラー: " + e.getMessage());
            }
        });
    }

    /**
     * 通報承認時の通知を送信
     */
    public void notifyReportApproved(Report report) {
        if (!enabled) {
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                String embed = buildApprovedEmbed(report);
                sendWebhook(embed);
            } catch (Exception e) {
                plugin.getLogger().warning("Discord 通知送信エラー: " + e.getMessage());
            }
        });
    }

    /**
     * 通報却下時の通知を送信
     */
    public void notifyReportDenied(Report report) {
        if (!enabled) {
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                String embed = buildDeniedEmbed(report);
                sendWebhook(embed);
            } catch (Exception e) {
                plugin.getLogger().warning("Discord 通知送信エラー: " + e.getMessage());
            }
        });
    }

    /**
     * 新規通報用のエンベッドを構築
     */
    private String buildReportEmbed(Report report) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        String mention = "";
        if (mentionRoleId != null && !mentionRoleId.isEmpty()) {
            mention = "<@&" + mentionRoleId + "> ";
        }

        String typeDisplay = plugin.getMessageManager().getMessage("type." + report.getReportType().name());
        if (typeDisplay == null || typeDisplay.startsWith("type.")) {
            typeDisplay = report.getReportType().name();
        }

        // カラーコードを削除（Discord用）
        typeDisplay = stripColorCodes(typeDisplay);

        String json = "{\n" +
                "  \"content\": \"" + mention + "\",\n" +
                "  \"embeds\": [{\n" +
                "    \"title\": \"新しい通報が届きました\",\n" +
                "    \"color\": 16711680,\n" +
                "    \"fields\": [\n" +
                "      {\"name\": \"通報ID\", \"value\": \"#" + report.getId() + "\", \"inline\": true},\n" +
                "      {\"name\": \"通報者\", \"value\": \"" + report.getReporterName() + "\", \"inline\": true},\n" +
                "      {\"name\": \"被通報者\", \"value\": \"" + report.getReportedName() + "\", \"inline\": false},\n" +
                "      {\"name\": \"理由\", \"value\": \"" + escapeJson(report.getReason()) + "\", \"inline\": false},\n" +
                "      {\"name\": \"種類\", \"value\": \"" + typeDisplay + "\", \"inline\": true},\n" +
                "      {\"name\": \"日時\", \"value\": \"" + sdf.format(report.getCreatedAt()) + "\", \"inline\": true}\n" +
                "    ],\n" +
                "    \"footer\": {\"text\": \"Kuma-Report\"}\n" +
                "  }]\n" +
                "}";

        return json;
    }

    /**
     * 通報承認用のエンベッドを構築
     */
    private String buildApprovedEmbed(Report report) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String handlerName = report.getHandlerName() != null ? report.getHandlerName() : "不明";
        String handledDate = report.getHandledAt() != null ? sdf.format(report.getHandledAt()) : "不明";

        String json = "{\n" +
                "  \"embeds\": [{\n" +
                "    \"title\": \"通報が承認されました\",\n" +
                "    \"color\": 65280,\n" +
                "    \"fields\": [\n" +
                "      {\"name\": \"通報ID\", \"value\": \"#" + report.getId() + "\", \"inline\": true},\n" +
                "      {\"name\": \"被通報者\", \"value\": \"" + report.getReportedName() + "\", \"inline\": true},\n" +
                "      {\"name\": \"処理者\", \"value\": \"" + handlerName + "\", \"inline\": false},\n" +
                "      {\"name\": \"処理日時\", \"value\": \"" + handledDate + "\", \"inline\": true}\n" +
                "    ],\n" +
                "    \"footer\": {\"text\": \"Kuma-Report\"}\n" +
                "  }]\n" +
                "}";

        return json;
    }

    /**
     * 通報却下用のエンベッドを構築
     */
    private String buildDeniedEmbed(Report report) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String handlerName = report.getHandlerName() != null ? report.getHandlerName() : "不明";
        String handledDate = report.getHandledAt() != null ? sdf.format(report.getHandledAt()) : "不明";

        String json = "{\n" +
                "  \"embeds\": [{\n" +
                "    \"title\": \"通報が却下されました\",\n" +
                "    \"color\": 16711680,\n" +
                "    \"fields\": [\n" +
                "      {\"name\": \"通報ID\", \"value\": \"#" + report.getId() + "\", \"inline\": true},\n" +
                "      {\"name\": \"被通報者\", \"value\": \"" + report.getReportedName() + "\", \"inline\": true},\n" +
                "      {\"name\": \"処理者\", \"value\": \"" + handlerName + "\", \"inline\": false},\n" +
                "      {\"name\": \"処理日時\", \"value\": \"" + handledDate + "\", \"inline\": true}\n" +
                "    ],\n" +
                "    \"footer\": {\"text\": \"Kuma-Report\"}\n" +
                "  }]\n" +
                "}";

        return json;
    }

    /**
     * Webhook に JSON を送信
     */
    private void sendWebhook(String jsonPayload) throws Exception {
        URL url = new URL(webhookUrl);
        URLConnection connection = url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");

        byte[] jsonData = jsonPayload.getBytes(StandardCharsets.UTF_8);

        try (OutputStream os = connection.getOutputStream()) {
            os.write(jsonData);
            os.flush();
        }

        // レスポンス受け取り（エラーチェック用）
        int responseCode = ((java.net.HttpURLConnection) connection).getResponseCode();
        if (responseCode < 200 || responseCode >= 300) {
            plugin.getLogger().warning("Discord Webhook エラー: HTTP " + responseCode);
        }
    }

    /**
     * JSON 文字列をエスケープ
     */
    private String escapeJson(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * Minecraft カラーコードを削除
     */
    private String stripColorCodes(String str) {
        if (str == null) {
            return "";
        }
        // Minecraft カラーコード (&c など) を削除
        return str.replaceAll("(?i)&[0-9A-F]", "")
                // 16進数カラーコード (&#RRGGBB や §x など) を削除
                .replaceAll("(?i)&#[0-9A-F]{6}", "")
                .replaceAll("§x", "")
                .replaceAll("(?i)§[0-9A-F]", "");
    }

    /**
     * Webhook URL が有効かチェック
     */
    private boolean isValidWebhookUrl(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }
        return url.startsWith("https://discord.com/api/webhooks/");
    }

    /**
     * 有効化確認
     */
    public boolean isEnabled() {
        return enabled;
    }
}
