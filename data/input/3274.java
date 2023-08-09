public class RemoteHostImpl implements RemoteHost, HostListener {
    private MonitoredHost monitoredHost;
    private Set<Integer> activeVms;
    public RemoteHostImpl() throws MonitorException {
        try {
            monitoredHost = MonitoredHost.getMonitoredHost("localhost");
        } catch (URISyntaxException e) { }
        activeVms = monitoredHost.activeVms();
        monitoredHost.addHostListener(this);
    }
    public RemoteVm attachVm(int lvmid, String mode)
                    throws RemoteException, MonitorException {
        Integer v = new Integer(lvmid);
        RemoteVm stub = null;
        StringBuffer sb = new StringBuffer();
        sb.append("local:
        if (mode != null) {
            sb.append("?mode=" + mode);
        }
        String vmidStr = sb.toString();
        try {
            VmIdentifier vmid = new VmIdentifier(vmidStr);
            MonitoredVm mvm = monitoredHost.getMonitoredVm(vmid);
            RemoteVmImpl rvm = new RemoteVmImpl((BufferedMonitoredVm)mvm);
            stub = (RemoteVm) UnicastRemoteObject.exportObject(rvm, 0);
        }
        catch (URISyntaxException e) {
            throw new RuntimeException("Malformed VmIdentifier URI: "
                                       + vmidStr, e);
        }
        return stub;
    }
    public void detachVm(RemoteVm rvm) throws RemoteException {
        rvm.detach();
    }
    public int[] activeVms() throws MonitorException {
        Object[] vms = null;
        int[] vmids = null;
        vms = monitoredHost.activeVms().toArray();
        vmids = new int[vms.length];
        for (int i = 0; i < vmids.length; i++) {
            vmids[i] = ((Integer)vms[i]).intValue();
        }
        return vmids;
    }
    public void vmStatusChanged(VmStatusChangeEvent ev) {
        synchronized(this.activeVms) {
            activeVms.retainAll(ev.getActive());
        }
    }
    public void disconnected(HostEvent ev) {
    }
}
