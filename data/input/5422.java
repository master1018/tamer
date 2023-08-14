public class Jps {
    private static Arguments arguments;
    public static void main(String[] args) {
        try {
            arguments = new Arguments(args);
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            Arguments.printUsage(System.err);
            return;
        }
        if (arguments.isHelp()) {
            Arguments.printUsage(System.out);
            System.exit(0);
        }
        try {
            HostIdentifier hostId = arguments.hostId();
            MonitoredHost monitoredHost =
                    MonitoredHost.getMonitoredHost(hostId);
            Set jvms = monitoredHost.activeVms();
            for (Iterator j = jvms.iterator(); j.hasNext();  ) {
                StringBuilder output = new StringBuilder();
                Throwable lastError = null;
                int lvmid = ((Integer)j.next()).intValue();
                output.append(String.valueOf(lvmid));
                if (arguments.isQuiet()) {
                    System.out.println(output);
                    continue;
                }
                MonitoredVm vm = null;
                String vmidString = "
                String errorString = null;
                try {
                    errorString = " -- process information unavailable";
                    VmIdentifier id = new VmIdentifier(vmidString);
                    vm = monitoredHost.getMonitoredVm(id, 0);
                    errorString = " -- main class information unavailable";
                    output.append(" " + MonitoredVmUtil.mainClass(vm,
                            arguments.showLongPaths()));
                    if (arguments.showMainArgs()) {
                        errorString = " -- main args information unavailable";
                        String mainArgs = MonitoredVmUtil.mainArgs(vm);
                        if (mainArgs != null && mainArgs.length() > 0) {
                            output.append(" " + mainArgs);
                        }
                    }
                    if (arguments.showVmArgs()) {
                        errorString = " -- jvm args information unavailable";
                        String jvmArgs = MonitoredVmUtil.jvmArgs(vm);
                        if (jvmArgs != null && jvmArgs.length() > 0) {
                          output.append(" " + jvmArgs);
                        }
                    }
                    if (arguments.showVmFlags()) {
                        errorString = " -- jvm flags information unavailable";
                        String jvmFlags = MonitoredVmUtil.jvmFlags(vm);
                        if (jvmFlags != null && jvmFlags.length() > 0) {
                            output.append(" " + jvmFlags);
                        }
                    }
                    errorString = " -- detach failed";
                    monitoredHost.detach(vm);
                    System.out.println(output);
                    errorString = null;
                } catch (URISyntaxException e) {
                    lastError = e;
                    assert false;
                } catch (Exception e) {
                    lastError = e;
                } finally {
                    if (errorString != null) {
                        output.append(errorString);
                        if (arguments.isDebug()) {
                            if ((lastError != null)
                                    && (lastError.getMessage() != null)) {
                                output.append("\n\t");
                                output.append(lastError.getMessage());
                            }
                        }
                        System.out.println(output);
                        if (arguments.printStackTrace()) {
                            lastError.printStackTrace();
                        }
                        continue;
                    }
                }
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
        }
    }
}
