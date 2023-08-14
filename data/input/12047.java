public class Threads {
    private static JavaThreadFactory threadFactory;
    private static AddressField      threadListField;
    private static VirtualConstructor virtualConstructor;
    private static JavaThreadPDAccess access;
    static {
        VM.registerVMInitializedObserver(new Observer() {
            public void update(Observable o, Object data) {
                initialize(VM.getVM().getTypeDataBase());
            }
        });
    }
    private static synchronized void initialize(TypeDataBase db) {
        Type type = db.lookupType("Threads");
        threadListField = type.getAddressField("_thread_list");
        String os  = VM.getVM().getOS();
        String cpu = VM.getVM().getCPU();
        access = null;
        if (os.equals("solaris")) {
            if (cpu.equals("sparc")) {
                access = new SolarisSPARCJavaThreadPDAccess();
            } else if (cpu.equals("x86")) {
                access = new SolarisX86JavaThreadPDAccess();
            } else if (cpu.equals("amd64")) {
                access = new SolarisAMD64JavaThreadPDAccess();
            }
        } else if (os.equals("win32")) {
            if (cpu.equals("x86")) {
                access =  new Win32X86JavaThreadPDAccess();
            } else if (cpu.equals("amd64")) {
                access =  new Win32AMD64JavaThreadPDAccess();
            } else if (cpu.equals("ia64")) {
                access =  new Win32IA64JavaThreadPDAccess();
            }
        } else if (os.equals("linux")) {
            if (cpu.equals("x86")) {
                access = new LinuxX86JavaThreadPDAccess();
            } else if (cpu.equals("ia64")) {
                access = new LinuxIA64JavaThreadPDAccess();
            } else if (cpu.equals("amd64")) {
                access = new LinuxAMD64JavaThreadPDAccess();
            } else if (cpu.equals("sparc")) {
                access = new LinuxSPARCJavaThreadPDAccess();
            }
        }
        if (access == null) {
            throw new RuntimeException("OS/CPU combination " + os + "/" + cpu +
            " not yet supported");
        }
        virtualConstructor = new VirtualConstructor(db);
        virtualConstructor.addMapping("JavaThread", JavaThread.class);
        if (!VM.getVM().isCore()) {
            virtualConstructor.addMapping("CompilerThread", CompilerThread.class);
        }
        virtualConstructor.addMapping("SurrogateLockerThread", JavaThread.class);
        virtualConstructor.addMapping("JvmtiAgentThread", JvmtiAgentThread.class);
        virtualConstructor.addMapping("ServiceThread", ServiceThread.class);
    }
    public Threads() {
    }
    public JavaThread first() {
        Address threadAddr = threadListField.getValue();
        if (threadAddr == null) {
            return null;
        }
        return createJavaThreadWrapper(threadAddr);
    }
    public JavaThread createJavaThreadWrapper(Address threadAddr) {
        try {
            JavaThread thread = (JavaThread)virtualConstructor.instantiateWrapperFor(threadAddr);
            thread.setThreadPDAccess(access);
            return thread;
        } catch (Exception e) {
            throw new RuntimeException("Unable to deduce type of thread from address " + threadAddr +
            " (expected type JavaThread, CompilerThread, ServiceThread, JvmtiAgentThread, or SurrogateLockerThread)", e);
        }
    }
    public void oopsDo(AddressVisitor oopVisitor) {
        for (JavaThread thread = first(); thread != null; thread = thread.next()) {
            thread.oopsDo(oopVisitor);
        }
    }
    public JavaThread owningThreadFromMonitor(Address o) {
        if (o == null) return null;
        for (JavaThread thread = first(); thread != null; thread = thread.next()) {
            if (o.equals(thread.threadObjectAddress())) {
                return thread;
            }
        }
        for (JavaThread thread = first(); thread != null; thread = thread.next()) {
          if (thread.isLockOwned(o))
            return thread;
        }
        return null;
    }
    public JavaThread owningThreadFromMonitor(ObjectMonitor monitor) {
        return owningThreadFromMonitor(monitor.owner());
    }
    public List getPendingThreads(ObjectMonitor monitor) {
        List pendingThreads = new ArrayList();
        for (JavaThread thread = first(); thread != null; thread = thread.next()) {
            if (thread.isCompilerThread()) {
                continue;
            }
            ObjectMonitor pending = thread.getCurrentPendingMonitor();
            if (monitor.equals(pending)) {
                pendingThreads.add(thread);
            }
        }
        return pendingThreads;
    }
    public List getWaitingThreads(ObjectMonitor monitor) {
        List pendingThreads = new ArrayList();
        for (JavaThread thread = first(); thread != null; thread = thread.next()) {
            ObjectMonitor waiting = thread.getCurrentWaitingMonitor();
            if (monitor.equals(waiting)) {
                pendingThreads.add(thread);
            }
        }
        return pendingThreads;
    }
}
