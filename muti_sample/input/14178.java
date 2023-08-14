public class RemoteVmManager {
    private RemoteHost remoteHost;
    private String user;
    public RemoteVmManager(RemoteHost remoteHost) {
        this(remoteHost, null);
    }
    public RemoteVmManager(RemoteHost remoteHost, String user) {
        this.user = user;
        this.remoteHost = remoteHost;
    }
    public Set<Integer> activeVms() throws MonitorException {
        int[] active = null;
        try {
            active = remoteHost.activeVms();
        } catch (RemoteException e) {
            throw new MonitorException("Error communicating with remote host: "
                                       + e.getMessage(), e);
        }
        Set<Integer> activeSet = new HashSet<Integer>(active.length);
        for (int i = 0; i < active.length; i++) {
            activeSet.add(new Integer(active[i]));
        }
        return activeSet;
    }
}
