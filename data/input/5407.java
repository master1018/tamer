class ClassLoadingImpl implements ClassLoadingMXBean {
    private final VMManagement jvm;
    ClassLoadingImpl(VMManagement vm) {
        this.jvm = vm;
    }
    public long getTotalLoadedClassCount() {
        return jvm.getTotalClassCount();
    }
    public int getLoadedClassCount() {
        return jvm.getLoadedClassCount();
    }
    public long getUnloadedClassCount() {
        return jvm.getUnloadedClassCount();
    }
    public boolean isVerbose() {
        return jvm.getVerboseClass();
    }
    public void setVerbose(boolean value) {
        Util.checkControlAccess();
        setVerboseClass(value);
    }
    native static void setVerboseClass(boolean value);
    public ObjectName getObjectName() {
        return Util.newObjectName(ManagementFactory.CLASS_LOADING_MXBEAN_NAME);
    }
}
