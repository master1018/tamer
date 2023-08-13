public class DrmConstraintInfo {
    private int count;
    private long startDate;
    private long endDate;
    private long interval;
    DrmConstraintInfo() {
        count = -1;
        startDate = -1;
        endDate = -1;
        interval = -1;
    }
    public int getCount() {
        return count;
    }
    public Date getStartDate() {
        if (startDate == -1)
            return null;
        return new Date(startDate);
    }
    public Date getEndDate() {
        if (endDate == -1)
            return null;
        return new Date(endDate);
    }
    public long getInterval() {
        return interval;
    }
}
