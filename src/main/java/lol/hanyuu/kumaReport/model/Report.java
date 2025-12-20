package lol.hanyuu.kumaReport.model;

import java.sql.Timestamp;
import java.util.UUID;

/**
 * プレイヤー通報のデータモデル
 */
public class Report {
    private int id;
    private UUID reporterUuid;
    private String reporterName;
    private UUID reportedUuid;
    private String reportedName;
    private String reason;
    private ReportType reportType;
    private ReportStatus status;
    private Timestamp createdAt;
    private UUID handlerUuid;
    private String handlerName;
    private Timestamp handledAt;
    private String handlerNote;

    // コンストラクタ
    public Report() {
    }

    public Report(UUID reporterUuid, String reporterName, UUID reportedUuid, String reportedName,
                  String reason, ReportType reportType) {
        this.reporterUuid = reporterUuid;
        this.reporterName = reporterName;
        this.reportedUuid = reportedUuid;
        this.reportedName = reportedName;
        this.reason = reason;
        this.reportType = reportType;
        this.status = ReportStatus.PENDING;
        this.createdAt = new Timestamp(System.currentTimeMillis());
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public UUID getReporterUuid() {
        return reporterUuid;
    }

    public void setReporterUuid(UUID reporterUuid) {
        this.reporterUuid = reporterUuid;
    }

    public String getReporterName() {
        return reporterName;
    }

    public void setReporterName(String reporterName) {
        this.reporterName = reporterName;
    }

    public UUID getReportedUuid() {
        return reportedUuid;
    }

    public void setReportedUuid(UUID reportedUuid) {
        this.reportedUuid = reportedUuid;
    }

    public String getReportedName() {
        return reportedName;
    }

    public void setReportedName(String reportedName) {
        this.reportedName = reportedName;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public ReportType getReportType() {
        return reportType;
    }

    public void setReportType(ReportType reportType) {
        this.reportType = reportType;
    }

    public ReportStatus getStatus() {
        return status;
    }

    public void setStatus(ReportStatus status) {
        this.status = status;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public UUID getHandlerUuid() {
        return handlerUuid;
    }

    public void setHandlerUuid(UUID handlerUuid) {
        this.handlerUuid = handlerUuid;
    }

    public String getHandlerName() {
        return handlerName;
    }

    public void setHandlerName(String handlerName) {
        this.handlerName = handlerName;
    }

    public Timestamp getHandledAt() {
        return handledAt;
    }

    public void setHandledAt(Timestamp handledAt) {
        this.handledAt = handledAt;
    }

    public String getHandlerNote() {
        return handlerNote;
    }

    public void setHandlerNote(String handlerNote) {
        this.handlerNote = handlerNote;
    }
}
