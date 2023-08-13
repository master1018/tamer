public class MonitoredVmUtil {
    private MonitoredVmUtil() { }
    public static String vmVersion(MonitoredVm vm) throws MonitorException {
        StringMonitor ver =
               (StringMonitor)vm.findByName("java.property.java.vm.version");
        return (ver == null) ? "Unknown" : ver.stringValue();
    }
    public static String commandLine(MonitoredVm vm) throws MonitorException {
        StringMonitor cmd = (StringMonitor)vm.findByName("sun.rt.javaCommand");
        return (cmd == null) ? "Unknown" : cmd.stringValue();
    }
    public static String mainArgs(MonitoredVm vm) throws MonitorException {
        String commandLine = commandLine(vm);
        int firstSpace = commandLine.indexOf(' ');
        if (firstSpace > 0) {
            return commandLine.substring(firstSpace + 1);
        } else if (commandLine.compareTo("Unknown") == 0) {
            return commandLine;
        } else {
            return null;
        }
    }
    public static String mainClass(MonitoredVm vm, boolean fullPath)
                         throws MonitorException {
        String commandLine = commandLine(vm);
        String arg0 = commandLine;
        int firstSpace = commandLine.indexOf(' ');
        if (firstSpace > 0) {
            arg0 = commandLine.substring(0, firstSpace);
        }
        if (!fullPath) {
            int lastFileSeparator = arg0.lastIndexOf('/');
            if (lastFileSeparator > 0) {
                 return arg0.substring(lastFileSeparator + 1);
            }
            lastFileSeparator = arg0.lastIndexOf('\\');
            if (lastFileSeparator > 0) {
                 return arg0.substring(lastFileSeparator + 1);
            }
            int lastPackageSeparator = arg0.lastIndexOf('.');
            if (lastPackageSeparator > 0) {
                 return arg0.substring(lastPackageSeparator + 1);
            }
        }
        return arg0;
    }
    public static String jvmArgs(MonitoredVm vm) throws MonitorException {
        StringMonitor jvmArgs = (StringMonitor)vm.findByName("java.rt.vmArgs");
        return (jvmArgs == null) ? "Unknown" : jvmArgs.stringValue();
    }
    public static String jvmFlags(MonitoredVm vm) throws MonitorException {
        StringMonitor jvmFlags =
               (StringMonitor)vm.findByName("java.rt.vmFlags");
        return (jvmFlags == null) ? "Unknown" : jvmFlags.stringValue();
    }
    private static int IS_ATTACHABLE = 0;
    private static int IS_KERNEL_VM  = 1;
    public static boolean isAttachable(MonitoredVm vm) throws MonitorException {
        StringMonitor jvmCapabilities =
               (StringMonitor)vm.findByName("sun.rt.jvmCapabilities");
        if (jvmCapabilities == null) {
             return false;
        } else {
             return jvmCapabilities.stringValue().charAt(IS_ATTACHABLE) == '1';
        }
    }
}
