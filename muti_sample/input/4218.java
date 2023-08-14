public class MonitorStatusChangeEvent extends VmEvent {
    protected List inserted;
    protected List removed;
    public MonitorStatusChangeEvent(MonitoredVm vm, List inserted,
                                    List removed) {
        super(vm);
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
