package lol.hanyuu.kumaReport.database;

import lol.hanyuu.kumaReport.model.Report;
import lol.hanyuu.kumaReport.model.ReportStatus;
import lol.hanyuu.kumaReport.model.ReportType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * プレイヤー通報のデータアクセスオブジェクト
 */
public class ReportDAO {
    private final DatabaseManager dbManager;

    public ReportDAO(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * 通報を作成
     */
    public int createReport(Report report) throws SQLException {
        String sql = "INSERT INTO reports (reporter_uuid, reporter_name, reported_uuid, reported_name, " +
                "reason, report_type, status) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, report.getReporterUuid().toString());
            pstmt.setString(2, report.getReporterName());
            pstmt.setString(3, report.getReportedUuid().toString());
            pstmt.setString(4, report.getReportedName());
            pstmt.setString(5, report.getReason());
            pstmt.setString(6, report.getReportType().name());
            pstmt.setString(7, report.getStatus().name());

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
     * IDで通報を取得
     */
    public Report getReportById(int id) throws SQLException {
        String sql = "SELECT * FROM reports WHERE id = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToReport(rs);
                }
            }
        }
        return null;
    }

    /**
     * ステータスで通報を取得
     */
    public List<Report> getReportsByStatus(ReportStatus status) throws SQLException {
        String sql = "SELECT * FROM reports WHERE status = ? ORDER BY created_at DESC";
        List<Report> reports = new ArrayList<>();

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status.name());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    reports.add(mapResultSetToReport(rs));
                }
            }
        }
        return reports;
    }

    /**
     * プレイヤーの通報を取得（被通報者）
     */
    public List<Report> getReportsByReportedPlayer(UUID reportedUuid) throws SQLException {
        String sql = "SELECT * FROM reports WHERE reported_uuid = ? ORDER BY created_at DESC";
        List<Report> reports = new ArrayList<>();

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, reportedUuid.toString());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    reports.add(mapResultSetToReport(rs));
                }
            }
        }
        return reports;
    }

    /**
     * プレイヤーの通報を取得（通報者）
     */
    public List<Report> getReportsByReporter(UUID reporterUuid) throws SQLException {
        String sql = "SELECT * FROM reports WHERE reporter_uuid = ? ORDER BY created_at DESC";
        List<Report> reports = new ArrayList<>();

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, reporterUuid.toString());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    reports.add(mapResultSetToReport(rs));
                }
            }
        }
        return reports;
    }

    /**
     * 未処理通報の件数を取得
     */
    public int getPendingReportCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM reports WHERE status = ?";

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
     * 通報のステータスを更新
     */
    public boolean updateReportStatus(int reportId, ReportStatus newStatus, UUID handlerUuid,
                                     String handlerName, String note) throws SQLException {
        String sql = "UPDATE reports SET status = ?, handler_uuid = ?, handler_name = ?, " +
                "handled_at = CURRENT_TIMESTAMP, handler_note = ? WHERE id = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newStatus.name());
            pstmt.setString(2, handlerUuid != null ? handlerUuid.toString() : null);
            pstmt.setString(3, handlerName);
            pstmt.setString(4, note);
            pstmt.setInt(5, reportId);

            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * 統計情報を取得
     */
    public ReportStatistics getStatistics() throws SQLException {
        String sql = "SELECT status, COUNT(*) as count FROM reports GROUP BY status";
        ReportStatistics stats = new ReportStatistics();

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
     * ページネーション付きで通報を取得
     */
    public List<Report> getReportsPaginated(int page, int itemsPerPage) throws SQLException {
        String sql = "SELECT * FROM reports ORDER BY created_at DESC LIMIT ? OFFSET ?";
        List<Report> reports = new ArrayList<>();

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, itemsPerPage);
            pstmt.setInt(2, (page - 1) * itemsPerPage);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    reports.add(mapResultSetToReport(rs));
                }
            }
        }
        return reports;
    }

    /**
     * 総ページ数を取得
     */
    public int getTotalPages(int itemsPerPage) throws SQLException {
        String sql = "SELECT COUNT(*) FROM reports";

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
     * ResultSetをReportオブジェクトにマッピング
     */
    private Report mapResultSetToReport(ResultSet rs) throws SQLException {
        Report report = new Report();
        report.setId(rs.getInt("id"));
        report.setReporterUuid(UUID.fromString(rs.getString("reporter_uuid")));
        report.setReporterName(rs.getString("reporter_name"));
        report.setReportedUuid(UUID.fromString(rs.getString("reported_uuid")));
        report.setReportedName(rs.getString("reported_name"));
        report.setReason(rs.getString("reason"));
        report.setReportType(ReportType.valueOf(rs.getString("report_type")));
        report.setStatus(ReportStatus.valueOf(rs.getString("status")));
        report.setCreatedAt(rs.getTimestamp("created_at"));

        String handlerUuid = rs.getString("handler_uuid");
        if (handlerUuid != null) {
            report.setHandlerUuid(UUID.fromString(handlerUuid));
        }
        report.setHandlerName(rs.getString("handler_name"));
        report.setHandledAt(rs.getTimestamp("handled_at"));
        report.setHandlerNote(rs.getString("handler_note"));

        return report;
    }

    /**
     * 統計情報クラス
     */
    public static class ReportStatistics {
        public int total = 0;
        public int pending = 0;
        public int inProgress = 0;
        public int accepted = 0;
        public int denied = 0;
    }
}
