class RuntimeImpl implements RuntimeMXBean {
    private final VMManagement jvm;
    private final long vmStartupTime;
    RuntimeImpl(VMManagement vm) {
        this.jvm = vm;
        this.vmStartupTime = jvm.getStartupTime();
    }
    public String getName() {
        return jvm.getVmId();
    }
    public String getManagementSpecVersion() {
        return jvm.getManagementVersion();
    }
    public String getVmName() {
        return jvm.getVmName();
    }
    public String getVmVendor() {
        return jvm.getVmVendor();
    }
    public String getVmVersion() {
        return jvm.getVmVersion();
    }
    public String getSpecName() {
        return jvm.getVmSpecName();
    }
    public String getSpecVendor() {
        return jvm.getVmSpecVendor();
    }
    public String getSpecVersion() {
        return jvm.getVmSpecVersion();
    }
    public String getClassPath() {
        return jvm.getClassPath();
    }
    public String getLibraryPath() {
        return jvm.getLibraryPath();
    }
    public String getBootClassPath() {
        if (!isBootClassPathSupported()) {
            throw new UnsupportedOperationException(
                "Boot class path mechanism is not supported");
        }
        Util.checkMonitorAccess();
        return jvm.getBootClassPath();
    }
    public List<String> getInputArguments() {
        Util.checkMonitorAccess();
        return jvm.getVmArguments();
    }
    public long getUptime() {
        long current = System.currentTimeMillis();
        return (current - vmStartupTime);
    }
    public long getStartTime() {
        return vmStartupTime;
    }
    public boolean isBootClassPathSupported() {
        return jvm.isBootClassPathSupported();
    }
    public Map<String,String> getSystemProperties() {
        Properties sysProps = System.getProperties();
        Map<String,String> map = new HashMap<String, String>();
        Set<String> keys = sysProps.stringPropertyNames();
        for (String k : keys) {
            String value = sysProps.getProperty(k);
            map.put(k, value);
        }
        return map;
    }
    public ObjectName getObjectName() {
        return Util.newObjectName(ManagementFactory.RUNTIME_MXBEAN_NAME);
    }
}
