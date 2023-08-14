public abstract class AbstractMonitoredVm implements BufferedMonitoredVm {
    protected VmIdentifier vmid;
    protected AbstractPerfDataBuffer pdb;
    protected int interval;
    public AbstractMonitoredVm(VmIdentifier vmid, int interval)
           throws MonitorException {
        this.vmid = vmid;
        this.interval = interval;
    }
    public VmIdentifier getVmIdentifier() {
        return vmid;
    }
    public Monitor findByName(String name) throws MonitorException {
        return pdb.findByName(name);
    }
    public List<Monitor> findByPattern(String patternString) throws MonitorException {
        return pdb.findByPattern(patternString);
    }
    public void detach() {
    }
    public void setInterval(int interval) {
        this.interval = interval;
    }
    public int getInterval() {
        return interval;
    }
    public void setLastException(Exception e) {
    }
    public Exception getLastException() {
        return null;
    }
    public void clearLastException() {
    }
    public boolean isErrored() {
        return false;
    }
    public MonitorStatus getMonitorStatus() throws MonitorException {
        return pdb.getMonitorStatus();
    }
    public abstract void addVmListener(VmListener l);
    public abstract void removeVmListener(VmListener l);
    public byte[] getBytes() {
        return pdb.getBytes();
    }
    public int getCapacity() {
        return pdb.getCapacity();
    }
}
