package lol.hanyuu.kumaReport.database;

import lol.hanyuu.kumaReport.model.BugReport;
import lol.hanyuu.kumaReport.model.ReportStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * バグレポートのデータアクセスオブジェクト
 */
public class BugReportDAO {
    private final DatabaseManager dbManager;

    public BugReportDAO(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * バグレポートを作成
     */
    public int createBugReport(BugReport bugReport) throws SQLException {
        String sql = "INSERT INTO bug_reports (reporter_uuid, reporter_name, description, location, status) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, bugReport.getReporterUuid().toString());
            pstmt.setString(2, bugReport.getReporterName());
            pstmt.setString(3, bugReport.getDescription());
            pstmt.setString(4, bugReport.getLocation());
            pstmt.setString(5, bugReport.getStatus().name());

            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }

    /**
     * IDでバグレポートを取得
     */
    public BugReport getBugReportById(int id) throws SQLException {
        String sql = "SELECT * FROM bug_reports WHERE id = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToBugReport(rs);
                }
            }
        }
        return null;
    }

    /**
     * ステータスでバグレポートを取得
     */
    public List<BugReport> getBugReportsByStatus(ReportStatus status) throws SQLException {
        String sql = "SELECT * FROM bug_reports WHERE status = ? ORDER BY created_at DESC";
        List<BugReport> bugReports = new ArrayList<>();

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status.name());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    bugReports.add(mapResultSetToBugReport(rs));
                }
            }
        }
        return bugReports;
    }

    /**
     * プレイヤーのバグレポートを取得
     */
    public List<BugReport> getBugReportsByReporter(UUID reporterUuid) throws SQLException {
        String sql = "SELECT * FROM bug_reports WHERE reporter_uuid = ? ORDER BY created_at DESC";
        List<BugReport> bugReports = new ArrayList<>();

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, reporterUuid.toString());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    bugReports.add(mapResultSetToBugReport(rs));
                }
            }
        }
        return bugReports;
    }

    /**
     * 未処理バグレポートの件数を取得
     */
    public int getPendingBugReportCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM bug_reports WHERE status = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, ReportStatus.PENDING.name());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    /**
     * バグレポートのステータスを更新
     */
    public boolean updateBugReportStatus(int bugReportId, ReportStatus newStatus, UUID handlerUuid,
                                        String handlerName, String note) throws SQLException {
        String sql = "UPDATE bug_reports SET status = ?, handler_uuid = ?, handler_name = ?, " +
                "handled_at = CURRENT_TIMESTAMP, handler_note = ? WHERE id = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newStatus.name());
            pstmt.setString(2, handlerUuid != null ? handlerUuid.toString() : null);
            pstmt.setString(3, handlerName);
            pstmt.setString(4, note);
            pstmt.setInt(5, bugReportId);

            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * 統計情報を取得
     */
    public BugReportStatistics getStatistics() throws SQLException {
        String sql = "SELECT status, COUNT(*) as count FROM bug_reports GROUP BY status";
        BugReportStatistics stats = new BugReportStatistics();

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            int totalCount = 0;
            while (rs.next()) {
                String status = rs.getString("status");
                int count = rs.getInt("count");
                totalCount += count;

                switch (status) {
                    case "PENDING" -> stats.pending = count;
                    case "IN_PROGRESS" -> stats.inProgress = count;
                    case "ACCEPTED" -> stats.accepted = count;
                    case "DENIED" -> stats.denied = count;
                }
            }
            stats.total = totalCount;
        }
        return stats;
    }

    /**
     * ページネーション付きでバグレポートを取得
     */
    public List<BugReport> getBugReportsPaginated(int page, int itemsPerPage) throws SQLException {
        String sql = "SELECT * FROM bug_reports ORDER BY created_at DESC LIMIT ? OFFSET ?";
        List<BugReport> bugReports = new ArrayList<>();

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, itemsPerPage);
            pstmt.setInt(2, (page - 1) * itemsPerPage);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    bugReports.add(mapResultSetToBugReport(rs));
                }
            }
        }
        return bugReports;
    }

    /**
     * 総ページ数を取得
     */
    public int getTotalPages(int itemsPerPage) throws SQLException {
        String sql = "SELECT COUNT(*) FROM bug_reports";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                int totalCount = rs.getInt(1);
                return (int) Math.ceil((double) totalCount / itemsPerPage);
            }
        }
        return 0;
    }

    /**
     * ResultSetをBugReportオブジェクトにマッピング
     */
    private BugReport mapResultSetToBugReport(ResultSet rs) throws SQLException {
        BugReport bugReport = new BugReport();
        bugReport.setId(rs.getInt("id"));
        bugReport.setReporterUuid(UUID.fromString(rs.getString("reporter_uuid")));
        bugReport.setReporterName(rs.getString("reporter_name"));
        bugReport.setDescription(rs.getString("description"));
        bugReport.setLocation(rs.getString("location"));
        bugReport.setStatus(ReportStatus.valueOf(rs.getString("status")));
        bugReport.setCreatedAt(rs.getTimestamp("created_at"));

        String handlerUuid = rs.getString("handler_uuid");
        if (handlerUuid != null) {
            bugReport.setHandlerUuid(UUID.fromString(handlerUuid));
        }
        bugReport.setHandlerName(rs.getString("handler_name"));
        bugReport.setHandledAt(rs.getTimestamp("handled_at"));
        bugReport.setHandlerNote(rs.getString("handler_note"));

        return bugReport;
    }

    /**
     * 統計情報クラス
     */
    public static class BugReportStatistics {
        public int total = 0;
        public int pending = 0;
        public int inProgress = 0;
        public int accepted = 0;
        public int denied = 0;
    }
}
