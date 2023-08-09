public class JStatLogger {
    private MonitoredVm monitoredVm;
    private volatile boolean active = true;
    public JStatLogger(MonitoredVm monitoredVm) {
        this.monitoredVm = monitoredVm;
    }
    public void printNames(String names, Comparator<Monitor> comparator,
                           boolean showUnsupported, PrintStream out)
                throws MonitorException, PatternSyntaxException {
        List<Monitor> items = monitoredVm.findByPattern(names);
        Collections.sort(items, comparator);
        for (Monitor m: items) {
            if (!(m.isSupported() || showUnsupported)) {
                continue;
            }
            out.println(m.getName());
        }
    }
    public void printSnapShot(String names, Comparator<Monitor> comparator,
                              boolean verbose, boolean showUnsupported,
                              PrintStream out)
                throws MonitorException, PatternSyntaxException {
        List<Monitor> items = monitoredVm.findByPattern(names);
        Collections.sort(items, comparator);
        printList(items, verbose, showUnsupported, out);
    }
    public void printList(List<Monitor> list, boolean verbose, boolean showUnsupported,
                          PrintStream out)
                throws MonitorException {
        for (Monitor m: list ) {
            if (!(m.isSupported() || showUnsupported)) {
                continue;
            }
            StringBuilder buffer = new StringBuilder();
            buffer.append(m.getName()).append("=");
            if (m instanceof StringMonitor) {
                buffer.append("\"").append(m.getValue()).append("\"");
            } else {
                buffer.append(m.getValue());
            }
            if (verbose) {
                buffer.append(" ").append(m.getUnits());
                buffer.append(" ").append(m.getVariability());
                buffer.append(" ").append(m.isSupported() ? "Supported"
                                                          : "Unsupported");
            }
            out.println(buffer);
        }
    }
    public void stopLogging() {
        active = false;
    }
    public void logSamples(OutputFormatter formatter, int headerRate,
                           int sampleInterval, int sampleCount, PrintStream out)
                throws MonitorException {
        long iterationCount = 0;
        int printHeaderCount = 0;
        int printHeader = headerRate;
        if (printHeader == 0) {
            out.println(formatter.getHeader());
            printHeader = -1;
        }
        while (active) {
          if (printHeader > 0 && --printHeaderCount <= 0) {
              printHeaderCount = printHeader;
              out.println(formatter.getHeader());
          }
          out.println(formatter.getRow());
          if (sampleCount > 0 && ++iterationCount >= sampleCount) {
              break;
          }
          try { Thread.sleep(sampleInterval); } catch (Exception e) { };
        }
    }
}
