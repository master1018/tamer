public class PrologSizeSanityCheck {
    private static final String sizeName = "sun.perfdata.size";
    private static final String usedName = "sun.perfdata.used";
    private static final String overflowName = "sun.perfdata.overflow";
    private static final int K = 1024;
    public static void main(String args[]) throws Exception {
        VmIdentifier vmid = new VmIdentifier("0");
        MonitoredHost localhost = MonitoredHost.getMonitoredHost("localhost");
        MonitoredVm self = localhost.getMonitoredVm(vmid);
        IntegerMonitor prologSize = (IntegerMonitor)self.findByName(sizeName);
        IntegerMonitor prologUsed = (IntegerMonitor)self.findByName(usedName);
        IntegerMonitor prologOverflow =
                (IntegerMonitor)self.findByName(overflowName);
        if (prologOverflow.intValue() != 0) {
            throw new RuntimeException("jvmstat memory buffer overflow: "
                    + sizeName + "=" + prologSize.intValue()
                    + usedName + "=" + prologUsed.intValue()
                    + overflowName + "=" + prologOverflow.intValue()
                    + " : PerfDataMemorySize must be increased");
        }
        if (prologUsed.intValue() + 3*K >= prologSize.intValue()) {
            throw new RuntimeException(
                   "jvmstat memory buffer usage approaching size: "
                    + sizeName + "=" + prologSize.intValue()
                    + usedName + "=" + prologUsed.intValue()
                    + " : consider increasing PerfDataMemorySize");
        }
    }
}
