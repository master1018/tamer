public class VmEvent extends EventObject {
    public VmEvent(MonitoredVm vm) {
        super(vm);
    }
    public MonitoredVm getMonitoredVm() {
      return (MonitoredVm)source;
    }
}
