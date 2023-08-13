public class MonitorStatus {
    protected List inserted;
    protected List removed;
    public MonitorStatus(List inserted, List removed) {
        this.inserted = inserted;
        this.removed = removed;
    }
    public List getInserted() {
        return inserted;
    }
    public List getRemoved() {
        return removed;
    }
}
