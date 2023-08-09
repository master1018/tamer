public class JvmRuntimeImpl implements JvmRuntimeMBean {
    static final EnumJvmRTBootClassPathSupport
        JvmRTBootClassPathSupportSupported =
        new EnumJvmRTBootClassPathSupport("supported");
    static final EnumJvmRTBootClassPathSupport
        JvmRTBootClassPathSupportUnSupported =
        new EnumJvmRTBootClassPathSupport("unsupported");
    public JvmRuntimeImpl(SnmpMib myMib) {
    }
    public JvmRuntimeImpl(SnmpMib myMib, MBeanServer server) {
    }
    static RuntimeMXBean getRuntimeMXBean() {
        return ManagementFactory.getRuntimeMXBean();
    }
    private static String validDisplayStringTC(String str) {
        return JVM_MANAGEMENT_MIB_IMPL.validDisplayStringTC(str);
    }
    private static String validPathElementTC(String str) {
        return JVM_MANAGEMENT_MIB_IMPL.validPathElementTC(str);
    }
    private static String validJavaObjectNameTC(String str) {
        return JVM_MANAGEMENT_MIB_IMPL.validJavaObjectNameTC(str);
    }
    static String[] splitPath(String path) {
        final String[] items = path.split(java.io.File.pathSeparator);
        return items;
    }
    static String[] getClassPath(Object userData) {
        final Map<Object, Object> m =
                Util.cast((userData instanceof Map)?userData:null);
        final String tag = "JvmRuntime.getClassPath";
        if (m != null) {
            final String[] cached = (String[])m.get(tag);
            if (cached != null) return cached;
        }
        final String[] args = splitPath(getRuntimeMXBean().getClassPath());
        if (m != null) m.put(tag,args);
        return args;
    }
    static String[] getBootClassPath(Object userData) {
        if (!getRuntimeMXBean().isBootClassPathSupported())
        return new String[0];
        final Map<Object, Object> m =
                Util.cast((userData instanceof Map)?userData:null);
        final String tag = "JvmRuntime.getBootClassPath";
        if (m != null) {
            final String[] cached = (String[])m.get(tag);
            if (cached != null) return cached;
        }
        final String[] args = splitPath(getRuntimeMXBean().getBootClassPath());
        if (m != null) m.put(tag,args);
        return args;
    }
    static String[] getLibraryPath(Object userData) {
        final Map<Object, Object> m =
                Util.cast((userData instanceof Map)?userData:null);
        final String tag = "JvmRuntime.getLibraryPath";
        if (m != null) {
            final String[] cached = (String[])m.get(tag);
            if (cached != null) return cached;
        }
        final String[] args = splitPath(getRuntimeMXBean().getLibraryPath());
        if (m != null) m.put(tag,args);
        return args;
    }
    static String[] getInputArguments(Object userData) {
        final Map<Object, Object> m =
                Util.cast((userData instanceof Map)?userData:null);
        final String tag = "JvmRuntime.getInputArguments";
        if (m != null) {
            final String[] cached = (String[])m.get(tag);
            if (cached != null) return cached;
        }
        final List<String> l = getRuntimeMXBean().getInputArguments();
        final String[] args = l.toArray(new String[0]);
        if (m != null) m.put(tag,args);
        return args;
    }
    public String getJvmRTSpecVendor() throws SnmpStatusException {
        return validDisplayStringTC(getRuntimeMXBean().getSpecVendor());
    }
    public String getJvmRTSpecName() throws SnmpStatusException {
        return validDisplayStringTC(getRuntimeMXBean().getSpecName());
    }
    public String getJvmRTVMVersion() throws SnmpStatusException {
        return validDisplayStringTC(getRuntimeMXBean().getVmVersion());
    }
    public String getJvmRTVMVendor() throws SnmpStatusException {
        return validDisplayStringTC(getRuntimeMXBean().getVmVendor());
    }
    public String getJvmRTManagementSpecVersion() throws SnmpStatusException {
        return validDisplayStringTC(getRuntimeMXBean().
                                    getManagementSpecVersion());
    }
    public String getJvmRTVMName() throws SnmpStatusException {
        return validJavaObjectNameTC(getRuntimeMXBean().getVmName());
    }
    public Integer getJvmRTInputArgsCount() throws SnmpStatusException {
        final String[] args = getInputArguments(JvmContextFactory.
                                                getUserData());
        return new Integer(args.length);
    }
    public EnumJvmRTBootClassPathSupport getJvmRTBootClassPathSupport()
        throws SnmpStatusException {
        if(getRuntimeMXBean().isBootClassPathSupported())
            return JvmRTBootClassPathSupportSupported;
        else
            return JvmRTBootClassPathSupportUnSupported;
    }
    public Long getJvmRTUptimeMs() throws SnmpStatusException {
        return new Long(getRuntimeMXBean().getUptime());
    }
    public Long getJvmRTStartTimeMs() throws SnmpStatusException {
        return new Long(getRuntimeMXBean().getStartTime());
    }
    public String getJvmRTSpecVersion() throws SnmpStatusException {
        return validDisplayStringTC(getRuntimeMXBean().getSpecVersion());
    }
    public String getJvmRTName() throws SnmpStatusException {
        return validDisplayStringTC(getRuntimeMXBean().getName());
    }
}
