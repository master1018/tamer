public class VmStatusChangeEvent extends HostEvent {
    protected Set active;
    protected Set started;
    protected Set terminated;
    public VmStatusChangeEvent(MonitoredHost host, Set active,
                               Set started, Set terminated) {
        super(host);
        this.active = active;
        this.started = started;
        this.terminated = terminated;
    }
    public Set getActive() {
        return active;
    }
    public Set getStarted() {
        return started;
    }
    public Set getTerminated() {
        return terminated;
    }
}
