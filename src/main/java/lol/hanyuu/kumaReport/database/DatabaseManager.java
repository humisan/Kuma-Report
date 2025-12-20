package lol.hanyuu.kumaReport.database;

// Note: HikariCP がshadow JARで lol.hanyuu.kumaReport.lib.hikari に relocate される場合、
// ここでもそのパッケージからインポートされます。
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lol.hanyuu.kumaReport.KumaReport;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * データベース管理クラス
 * SQLite接続、HikariCP接続プール、テーブル作成を担当
 */
public class DatabaseManager {
    private final KumaReport plugin;
    private HikariDataSource dataSource;
    private ReportDAO reportDAO;
    private BugReportDAO bugReportDAO;

    public DatabaseManager(KumaReport plugin) {
        this.plugin = plugin;
    }

    /**
     * データベースを初期化
     */
    public void initializeDatabase() {
        try {
            // データベースファイルのパスを取得
            String dbPath = plugin.getConfigManager().getDatabasePath();
            File dbFile = new File(dbPath);

            // 親ディレクトリを作成
            if (!dbFile.getParentFile().exists()) {
                dbFile.getParentFile().mkdirs();
            }

            // HikariCP設定
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:sqlite:" + dbPath);
            config.setDriverClassName("org.sqlite.JDBC");
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setConnectionTimeout(30000);
            config.setIdleTimeout(600000);
            config.setMaxLifetime(1800000);

            // SQLite固有の設定
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

            // データソース作成
            dataSource = new HikariDataSource(config);

            // テーブル作成
            createTables();

            // DAO初期化
            reportDAO = new ReportDAO(this);
            bugReportDAO = new BugReportDAO(this);

            plugin.getLogger().info("データベースの初期化が完了しました。");

        } catch (Exception e) {
            plugin.getLogger().severe("データベースの初期化に失敗しました: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * テーブルを作成
     */
    private void createTables() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // reportsテーブル
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS reports (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "reporter_uuid VARCHAR(36) NOT NULL, " +
                            "reporter_name VARCHAR(16) NOT NULL, " +
                            "reported_uuid VARCHAR(36) NOT NULL, " +
                            "reported_name VARCHAR(16) NOT NULL, " +
                            "reason TEXT NOT NULL, " +
                            "report_type VARCHAR(20) NOT NULL, " +
                            "status VARCHAR(20) DEFAULT 'PENDING', " +
                            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                            "handler_uuid VARCHAR(36), " +
                            "handler_name VARCHAR(16), " +
                            "handled_at TIMESTAMP, " +
                            "handler_note TEXT" +
                            ")"
            );

            // bug_reportsテーブル
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS bug_reports (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "reporter_uuid VARCHAR(36) NOT NULL, " +
                            "reporter_name VARCHAR(16) NOT NULL, " +
                            "description TEXT NOT NULL, " +
                            "location VARCHAR(100), " +
                            "status VARCHAR(20) DEFAULT 'PENDING', " +
                            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                            "handler_uuid VARCHAR(36), " +
                            "handler_name VARCHAR(16), " +
                            "handled_at TIMESTAMP, " +
                            "handler_note TEXT" +
                            ")"
            );

            // notification_queueテーブル（通報者通知用）
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS notification_queue (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "player_uuid VARCHAR(36) NOT NULL, " +
                            "message TEXT NOT NULL, " +
                            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                            "delivered BOOLEAN DEFAULT 0" +
                            ")"
            );

            // インデックス作成
            createIndexes(stmt);

            plugin.getLogger().info("データベーステーブルを作成しました。");

        } catch (SQLException e) {
            plugin.getLogger().severe("テーブル作成に失敗しました: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * インデックスを作成
     */
    private void createIndexes(Statement stmt) throws SQLException {
        // reportsテーブルのインデックス
        stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_reported_uuid ON reports(reported_uuid)");
        stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_reporter_uuid ON reports(reporter_uuid)");
        stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_status ON reports(status)");
        stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_created_at ON reports(created_at)");

        // bug_reportsテーブルのインデックス
        stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_bug_status ON bug_reports(status)");
        stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_bug_created_at ON bug_reports(created_at)");

        // notification_queueテーブルのインデックス
        stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_notification_uuid ON notification_queue(player_uuid)");
        stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_notification_delivered ON notification_queue(delivered)");
    }

    /**
     * データベース接続を取得
     */
    public Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("データソースが初期化されていません。");
        }
        return dataSource.getConnection();
    }

    /**
     * データベース接続を閉じる
     */
    public void closeConnection() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            plugin.getLogger().info("データベース接続を閉じました。");
        }
    }

    /**
     * データソースが初期化されているか確認
     */
    public boolean isInitialized() {
        return dataSource != null && !dataSource.isClosed();
    }

    /**
     * ReportDAOを取得
     */
    public ReportDAO getReportDAO() {
        return reportDAO;
    }

    /**
     * BugReportDAOを取得
     */
    public BugReportDAO getBugReportDAO() {
        return bugReportDAO;
    }
}
