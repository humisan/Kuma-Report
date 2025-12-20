package lol.hanyuu.kumaReport.model;

import java.sql.Timestamp;
import java.util.UUID;

/**
 * バグレポートのデータモデル
 */
public class BugReport {
    private int id;
    private UUID reporterUuid;
    private String reporterName;
    private String description;
    private String location;
    private ReportStatus status;
    private Timestamp createdAt;
    private UUID handlerUuid;
    private String handlerName;
    private Timestamp handledAt;
    private String handlerNote;

    // コンストラクタ
    public BugReport() {
    }

    public BugReport(UUID reporterUuid, String reporterName, String description, String location) {
        this.reporterUuid = reporterUuid;
        this.reporterName = reporterName;
        this.description = description;
        this.location = location;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
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
