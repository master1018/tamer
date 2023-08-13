public class CR6672135 {
    private static final int INTERVAL = 2000;
    public static void main(String[] args) {
        int vmInterval;
        int hostInterval;
        try {
            MonitoredHost localHost = MonitoredHost.getMonitoredHost("localhost");
            Set vms = localHost.activeVms();
            Integer vmInt =  (Integer) vms.iterator().next();
            String uriString = "
            VmIdentifier vmId = new VmIdentifier(uriString);
            MonitoredVm vm = localHost.getMonitoredVm(vmId);
            vm.setInterval(INTERVAL);
            localHost.setInterval(INTERVAL);
            vmInterval = vm.getInterval();
            hostInterval = localHost.getInterval();
        } catch (Exception ex) {
            throw new Error ("Test failed",ex);
        }
        System.out.println("VM "+vmInterval);
        if (vmInterval != INTERVAL) {
            throw new Error("Test failed");
        }
        System.out.println("Host "+hostInterval);
        if (hostInterval != INTERVAL) {
            throw new Error("Test failed");
        }
    }
}
