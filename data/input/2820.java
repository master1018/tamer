class CompilationImpl implements CompilationMXBean {
    private final VMManagement jvm;
    private final String name;
    CompilationImpl(VMManagement vm) {
        this.jvm = vm;
        this.name = jvm.getCompilerName();
        if (name == null) {
            throw new AssertionError("Null compiler name");
        }
    }
    public java.lang.String getName() {
        return name;
    }
    public boolean isCompilationTimeMonitoringSupported() {
        return jvm.isCompilationTimeMonitoringSupported();
    }
    public long getTotalCompilationTime() {
        if (!isCompilationTimeMonitoringSupported()) {
            throw new UnsupportedOperationException(
                "Compilation time monitoring is not supported.");
        }
        return jvm.getTotalCompileTime();
    }
    public ObjectName getObjectName() {
        return Util.newObjectName(ManagementFactory.COMPILATION_MXBEAN_NAME);
    }
}
