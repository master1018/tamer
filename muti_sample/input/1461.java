public class HostEvent extends EventObject {
    public HostEvent(MonitoredHost host) {
        super(host);
    }
    public MonitoredHost getMonitoredHost() {
        return (MonitoredHost)source;
    }
}
