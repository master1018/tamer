public class FileMonitoredVm extends AbstractMonitoredVm {
    public FileMonitoredVm(VmIdentifier vmid, int interval)
           throws MonitorException {
        super(vmid, interval);
        this.pdb = new PerfDataBuffer(vmid);
    }
    public void addVmListener(VmListener l) { }
    public void removeVmListener(VmListener l) { }
}
