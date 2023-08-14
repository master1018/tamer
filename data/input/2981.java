public class Jstat {
    private static Arguments arguments;
    public static void main(String[] args) {
        try {
            arguments = new Arguments(args);
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            Arguments.printUsage(System.err);
            System.exit(1);
        }
        if (arguments.isHelp()) {
            Arguments.printUsage(System.out);
            System.exit(0);
        }
        if (arguments.isOptions()) {
            OptionLister ol = new OptionLister(arguments.optionsSources());
            ol.print(System.out);
            System.exit(0);
        }
        try {
            if (arguments.isList()) {
                logNames();
            } else if (arguments.isSnap()) {
                logSnapShot();
            } else {
                logSamples();
            }
        } catch (MonitorException e) {
            if (e.getMessage() != null) {
                System.err.println(e.getMessage());
            } else {
                Throwable cause = e.getCause();
                if ((cause != null) && (cause.getMessage() != null)) {
                    System.err.println(cause.getMessage());
                } else {
                    e.printStackTrace();
                }
            }
            System.exit(1);
        }
        System.exit(0);
    }
    static void logNames() throws MonitorException {
        VmIdentifier vmId = arguments.vmId();
        int interval = arguments.sampleInterval();
        MonitoredHost monitoredHost = MonitoredHost.getMonitoredHost(vmId);
        MonitoredVm monitoredVm = monitoredHost.getMonitoredVm(vmId, interval);
        JStatLogger logger = new JStatLogger(monitoredVm);
        logger.printNames(arguments.counterNames(), arguments.comparator(),
                          arguments.showUnsupported(), System.out);
        monitoredHost.detach(monitoredVm);
    }
    static void logSnapShot() throws MonitorException {
        VmIdentifier vmId = arguments.vmId();
        int interval = arguments.sampleInterval();
        MonitoredHost monitoredHost = MonitoredHost.getMonitoredHost(vmId);
        MonitoredVm monitoredVm = monitoredHost.getMonitoredVm(vmId, interval);
        JStatLogger logger = new JStatLogger(monitoredVm);
        logger.printSnapShot(arguments.counterNames(), arguments.comparator(),
                             arguments.isVerbose(), arguments.showUnsupported(),
                             System.out);
        monitoredHost.detach(monitoredVm);
    }
    static void logSamples() throws MonitorException {
        final VmIdentifier vmId = arguments.vmId();
        int interval = arguments.sampleInterval();
        final MonitoredHost monitoredHost =
                MonitoredHost.getMonitoredHost(vmId);
        MonitoredVm monitoredVm = monitoredHost.getMonitoredVm(vmId, interval);
        final JStatLogger logger = new JStatLogger(monitoredVm);
        OutputFormatter formatter = null;
        if (arguments.isSpecialOption()) {
            OptionFormat format = arguments.optionFormat();
            formatter = new OptionOutputFormatter(monitoredVm, format);
        } else {
            List<Monitor> logged = monitoredVm.findByPattern(arguments.counterNames());
            Collections.sort(logged, arguments.comparator());
            List<Monitor> constants = new ArrayList<Monitor>();
            for (Iterator i = logged.iterator(); i.hasNext(); ) {
                Monitor m = (Monitor)i.next();
                if (!(m.isSupported() || arguments.showUnsupported())) {
                    i.remove();
                    continue;
                }
                if (m.getVariability() == Variability.CONSTANT) {
                    i.remove();
                    if (arguments.printConstants()) constants.add(m);
                } else if ((m.getUnits() == Units.STRING)
                        && !arguments.printStrings()) {
                    i.remove();
                }
            }
            if (!constants.isEmpty()) {
                logger.printList(constants, arguments.isVerbose(),
                                 arguments.showUnsupported(), System.out);
                if (!logged.isEmpty()) {
                    System.out.println();
                }
            }
            if (logged.isEmpty()) {
                monitoredHost.detach(monitoredVm);
                return;
            }
            formatter = new RawOutputFormatter(logged,
                                               arguments.printStrings());
        }
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                logger.stopLogging();
            }
        });
        HostListener terminator = new HostListener() {
            public void vmStatusChanged(VmStatusChangeEvent ev) {
                Integer lvmid = new Integer(vmId.getLocalVmId());
                if (ev.getTerminated().contains(lvmid)) {
                    logger.stopLogging();
                } else if (!ev.getActive().contains(lvmid)) {
                    logger.stopLogging();
                }
            }
            public void disconnected(HostEvent ev) {
                if (monitoredHost == ev.getMonitoredHost()) {
                    logger.stopLogging();
                }
            }
        };
        if (vmId.getLocalVmId() != 0) {
            monitoredHost.addHostListener(terminator);
        }
        logger.logSamples(formatter, arguments.headerRate(),
                          arguments.sampleInterval(), arguments.sampleCount(),
                          System.out);
        if (terminator != null) {
            monitoredHost.removeHostListener(terminator);
        }
        monitoredHost.detach(monitoredVm);
    }
}
