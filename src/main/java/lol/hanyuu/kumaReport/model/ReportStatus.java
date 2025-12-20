package lol.hanyuu.kumaReport.model;

/**
 * 通報のステータス
 */
public enum ReportStatus {
    /**
     * 未処理
     */
    PENDING,

    /**
     * 処理中
     */
    IN_PROGRESS,

    /**
     * 承認済み
     */
    ACCEPTED,

    /**
     * 却下済み
     */
    DENIED
}
