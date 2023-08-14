public class BatchUpdateException extends SQLException implements Serializable {
    private static final long serialVersionUID = 5977529877145521757L;
    private int[] updateCounts = null;
    public BatchUpdateException() {
        super();
    }
    public BatchUpdateException(int[] updateCounts) {
        super();
        this.updateCounts = updateCounts;
    }
    public BatchUpdateException(String reason, int[] updateCounts) {
        super(reason);
        this.updateCounts = updateCounts;
    }
    public BatchUpdateException(String reason, String SQLState,
            int[] updateCounts) {
        super(reason, SQLState);
        this.updateCounts = updateCounts;
    }
    public BatchUpdateException(String reason, String SQLState, int vendorCode,
            int[] updateCounts) {
        super(reason, SQLState, vendorCode);
        this.updateCounts = updateCounts;
    }
    public int[] getUpdateCounts() {
        return updateCounts;
    }
}
