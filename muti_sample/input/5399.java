public class BugSpotAgent {
    private JVMDebugger debugger;
    private MachineDescription machDesc;
    private TypeDataBase db;
    private String os;
    private String cpu;
    private String fileSep;
    private static final int PROCESS_MODE   = 0;
    private static final int CORE_FILE_MODE = 1;
    private static final int REMOTE_MODE    = 2;
    private int startupMode;
    private boolean isServer;
    private int pid;
    private String executableName;
    private String coreFileName;
    private String debugServerID;
    private String serverID;
    private boolean javaMode;
    private ServiceabilityAgentJVMDIModule jvmdi;
    private boolean javaInteractionDisabled;
    private String[] jvmLibNames;
    private String[] saLibNames;
    private static final String defaultDbxPathPrefix                = "/net/jano.eng/export/disk05/hotspot/sa";
    private static final String defaultDbxSvcAgentDSOPathPrefix     = "/net/jano.eng/export/disk05/hotspot/sa";
    private static final boolean DEBUG;
    static {
        DEBUG = System.getProperty("sun.jvm.hotspot.bugspot.BugSpotAgent.DEBUG")
        != null;
    }
    static void debugPrintln(String str) {
        if (DEBUG) {
            System.err.println(str);
        }
    }
    static void showUsage() {
        System.out.println("    You can also pass these -D options to java to specify where to find dbx and the \n" +
        "    Serviceability Agent plugin for dbx:");
        System.out.println("       -DdbxPathName=<path-to-dbx-executable>\n" +
        "             Default is derived from dbxPathPrefix");
        System.out.println("    or");
        System.out.println("       -DdbxPathPrefix=<xxx>\n" +
        "             where xxx is the path name of a dir structure that contains:\n" +
        "                   <os>/<arch>/bin/dbx\n" +
        "             The default is " + defaultDbxPathPrefix);
        System.out.println("    and");
        System.out.println("       -DdbxSvcAgentDSOPathName=<path-to-dbx-serviceability-agent-module>\n" +
        "             Default is determined from dbxSvcAgentDSOPathPrefix");
        System.out.println("    or");
        System.out.println("       -DdbxSvcAgentDSOPathPrefix=<xxx>\n" +
        "             where xxx is the pathname of a dir structure that contains:\n" +
        "                   <os>/<arch>/bin/lib/libsvc_agent_dbx.so\n" +
        "             The default is " + defaultDbxSvcAgentDSOPathPrefix);
    }
    public BugSpotAgent() {
        Runtime.getRuntime().addShutdownHook(new java.lang.Thread(
        new Runnable() {
            public void run() {
                synchronized (BugSpotAgent.this) {
                    if (!isServer) {
                        detach();
                    }
                }
            }
        }));
    }
    public synchronized Debugger getDebugger() {
        return debugger;
    }
    public synchronized CDebugger getCDebugger() {
        return getDebugger().getCDebugger();
    }
    public synchronized ProcessControl getProcessControl() {
        return getCDebugger().getProcessControl();
    }
    public synchronized TypeDataBase getTypeDataBase() {
        return db;
    }
    public synchronized boolean isSuspended() throws DebuggerException {
        return getProcessControl().isSuspended();
    }
    public synchronized void suspend() throws DebuggerException {
        getProcessControl().suspend();
    }
    public synchronized void resume() throws DebuggerException {
        getProcessControl().resume();
    }
    public synchronized boolean isJavaMode() {
        return javaMode;
    }
    public synchronized void disableJavaInteraction() {
        javaInteractionDisabled = true;
    }
    public synchronized void enableJavaInteraction() {
        javaInteractionDisabled = false;
    }
    public synchronized boolean isJavaInteractionDisabled() {
        return javaInteractionDisabled;
    }
    public synchronized boolean canInteractWithJava() {
        return (jvmdi != null) && !javaInteractionDisabled;
    }
    public synchronized void suspendJava() throws DebuggerException {
        if (!canInteractWithJava()) {
            throw new DebuggerException("Could not connect to SA's JVMDI module");
        }
        if (jvmdi.isSuspended()) {
            throw new DebuggerException("Target process already suspended via JVMDI");
        }
        jvmdi.suspend();
    }
    public synchronized void resumeJava() throws DebuggerException {
        if (!canInteractWithJava()) {
            throw new DebuggerException("Could not connect to SA's JVMDI module");
        }
        if (!jvmdi.isSuspended()) {
            throw new DebuggerException("Target process already resumed via JVMDI");
        }
        jvmdi.resume();
    }
    public synchronized boolean isJavaSuspended() throws DebuggerException {
        return jvmdi.isSuspended();
    }
    public synchronized ServiceabilityAgentJVMDIModule.BreakpointToggleResult
    toggleJavaBreakpoint(String srcFileName,
    String pkgName,
    int lineNo) {
        if (!canInteractWithJava()) {
            throw new DebuggerException("Could not connect to SA's JVMDI module; can not toggle Java breakpoints");
        }
        return jvmdi.toggleBreakpoint(srcFileName, pkgName, lineNo);
    }
    public synchronized boolean javaEventPending() throws DebuggerException {
        if (!canInteractWithJava()) {
            throw new DebuggerException("Could not connect to SA's JVMDI module; can not poll for Java debug events");
        }
        return jvmdi.eventPending();
    }
    public synchronized Event javaEventPoll() throws DebuggerException {
        if (!canInteractWithJava()) {
            throw new DebuggerException("Could not connect to SA's JVMDI module; can not poll for Java debug events");
        }
        return jvmdi.eventPoll();
    }
    public synchronized void javaEventContinue() throws DebuggerException {
        if (!canInteractWithJava()) {
            throw new DebuggerException("Could not connect to SA's JVMDI module; can not continue past Java debug events");
        }
        jvmdi.eventContinue();
    }
    public synchronized void attach(int processID)
    throws DebuggerException {
        if (debugger != null) {
            throw new DebuggerException("Already attached");
        }
        pid = processID;
        startupMode = PROCESS_MODE;
        isServer = false;
        go();
    }
    public synchronized void attach(String executableName, String coreFileName)
    throws DebuggerException {
        if (debugger != null) {
            throw new DebuggerException("Already attached");
        }
        if ((executableName == null) || (coreFileName == null)) {
            throw new DebuggerException("Both the core file name and executable name must be specified");
        }
        this.executableName = executableName;
        this.coreFileName = coreFileName;
        startupMode = CORE_FILE_MODE;
        isServer = false;
        go();
    }
    public synchronized void attach(String remoteServerID)
    throws DebuggerException {
        if (debugger != null) {
            throw new DebuggerException("Already attached to a process");
        }
        if (remoteServerID == null) {
            throw new DebuggerException("Debug server id must be specified");
        }
        debugServerID = remoteServerID;
        startupMode = REMOTE_MODE;
        isServer = false;
        go();
    }
    public synchronized boolean detach() throws DebuggerException {
        if (isServer) {
            throw new DebuggerException("Should not call detach() for server configuration");
        }
        return detachInternal();
    }
    public synchronized void startServer(int processID, String uniqueID)
    throws DebuggerException {
        if (debugger != null) {
            throw new DebuggerException("Already attached");
        }
        pid = processID;
        startupMode = PROCESS_MODE;
        isServer = true;
        serverID = uniqueID;
        go();
    }
    public synchronized void startServer(int processID)
    throws DebuggerException {
        startServer(processID, null);
    }
    public synchronized void startServer(String executableName, String coreFileName,
    String uniqueID)
    throws DebuggerException {
        if (debugger != null) {
            throw new DebuggerException("Already attached");
        }
        if ((executableName == null) || (coreFileName == null)) {
            throw new DebuggerException("Both the core file name and Java executable name must be specified");
        }
        this.executableName = executableName;
        this.coreFileName = coreFileName;
        startupMode = CORE_FILE_MODE;
        isServer = true;
        serverID = uniqueID;
        go();
    }
    public synchronized void startServer(String executableName, String coreFileName)
    throws DebuggerException {
        startServer(executableName, coreFileName, null);
    }
    public synchronized boolean shutdownServer() throws DebuggerException {
        if (!isServer) {
            throw new DebuggerException("Should not call shutdownServer() for client configuration");
        }
        return detachInternal();
    }
    private boolean detachInternal() {
        if (debugger == null) {
            return false;
        }
        if (canInteractWithJava()) {
            jvmdi.detach();
            jvmdi = null;
        }
        boolean retval = true;
        if (!isServer) {
            VM.shutdown();
        }
        Debugger dbg = null;
        DebuggerException ex = null;
        if (isServer) {
            try {
                RMIHelper.unbind(serverID);
            }
            catch (DebuggerException de) {
                ex = de;
            }
            dbg = debugger;
        } else {
            if (startupMode != REMOTE_MODE) {
                dbg = debugger;
            }
        }
        if (dbg != null) {
            retval = dbg.detach();
        }
        debugger = null;
        machDesc = null;
        db = null;
        if (ex != null) {
            throw(ex);
        }
        return retval;
    }
    private void go() {
        setupDebugger();
        javaMode = setupVM();
    }
    private void setupDebugger() {
        if (startupMode != REMOTE_MODE) {
            try {
                os  = PlatformInfo.getOS();
                cpu = PlatformInfo.getCPU();
            }
            catch (UnsupportedPlatformException e) {
                throw new DebuggerException(e);
            }
            fileSep = System.getProperty("file.separator");
            if (os.equals("solaris")) {
                setupDebuggerSolaris();
            } else if (os.equals("win32")) {
                setupDebuggerWin32();
            } else if (os.equals("linux")) {
                setupDebuggerLinux();
            } else {
                throw new DebuggerException("Operating system " + os + " not yet supported");
            }
            if (isServer) {
                RemoteDebuggerServer remote = null;
                try {
                    remote = new RemoteDebuggerServer(debugger);
                }
                catch (RemoteException rem) {
                    throw new DebuggerException(rem);
                }
                RMIHelper.rebind(serverID, remote);
            }
        } else {
            connectRemoteDebugger();
        }
    }
    private boolean setupVM() {
        try {
            if (os.equals("solaris")) {
                db = new HotSpotTypeDataBase(machDesc, new HotSpotSolarisVtblAccess(debugger, jvmLibNames),
                debugger, jvmLibNames);
            } else if (os.equals("win32")) {
                db = new HotSpotTypeDataBase(machDesc, new Win32VtblAccess(debugger, jvmLibNames),
                debugger, jvmLibNames);
            } else if (os.equals("linux")) {
                db = new HotSpotTypeDataBase(machDesc, new LinuxVtblAccess(debugger, jvmLibNames),
                debugger, jvmLibNames);
            } else {
                throw new DebuggerException("OS \"" + os + "\" not yet supported (no VtblAccess implemented yet)");
            }
        }
        catch (NoSuchSymbolException e) {
            e.printStackTrace();
            return false;
        }
        if (startupMode != REMOTE_MODE) {
            debugger.configureJavaPrimitiveTypeSizes(db.getJBooleanType().getSize(),
            db.getJByteType().getSize(),
            db.getJCharType().getSize(),
            db.getJDoubleType().getSize(),
            db.getJFloatType().getSize(),
            db.getJIntType().getSize(),
            db.getJLongType().getSize(),
            db.getJShortType().getSize());
        }
        if (!isServer) {
            VM.initialize(db, debugger);
        }
        try {
            jvmdi = new ServiceabilityAgentJVMDIModule(debugger, saLibNames);
            if (jvmdi.canAttach()) {
                jvmdi.attach();
                jvmdi.setCommandTimeout(6000);
                debugPrintln("Attached to Serviceability Agent's JVMDI module.");
                resume();
                suspendJava();
                suspend();
                debugPrintln("Suspended all Java threads.");
            } else {
                debugPrintln("Could not locate SA's JVMDI module; skipping attachment");
                jvmdi = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            jvmdi = null;
        }
        return true;
    }
    private void setupDebuggerSolaris() {
        setupJVMLibNamesSolaris();
        String prop = System.getProperty("sun.jvm.hotspot.debugger.useProcDebugger");
        if (prop != null && !prop.equals("false")) {
            ProcDebuggerLocal dbg = new ProcDebuggerLocal(null, true);
            debugger = dbg;
            attachDebugger();
            if (cpu.equals("x86")) {
                machDesc = new MachineDescriptionIntelX86();
            } else if (cpu.equals("sparc")) {
                int addressSize = dbg.getRemoteProcessAddressSize();
                if (addressSize == -1) {
                    throw new DebuggerException("Error occurred while trying to determine the remote process's address size");
                }
                if (addressSize == 32) {
                    machDesc = new MachineDescriptionSPARC32Bit();
                } else if (addressSize == 64) {
                    machDesc = new MachineDescriptionSPARC64Bit();
                } else {
                    throw new DebuggerException("Address size " + addressSize + " is not supported on SPARC");
                }
            } else if (cpu.equals("amd64")) {
                machDesc = new MachineDescriptionAMD64();
            } else {
                throw new DebuggerException("Solaris only supported on sparc/sparcv9/x86/amd64");
            }
            dbg.setMachineDescription(machDesc);
            return;
        } else {
            String dbxPathName;
            String dbxPathPrefix;
            String dbxSvcAgentDSOPathName;
            String dbxSvcAgentDSOPathPrefix;
            String[] dbxSvcAgentDSOPathNames = null;
            dbxPathName = System.getProperty("dbxPathName");
            if (dbxPathName == null) {
                dbxPathPrefix = System.getProperty("dbxPathPrefix");
                if (dbxPathPrefix == null) {
                    dbxPathPrefix = defaultDbxPathPrefix;
                }
                dbxPathName = dbxPathPrefix + fileSep + os + fileSep + cpu + fileSep + "bin" + fileSep + "dbx";
            }
            dbxSvcAgentDSOPathName = System.getProperty("dbxSvcAgentDSOPathName");
            if (dbxSvcAgentDSOPathName != null) {
                dbxSvcAgentDSOPathNames = new String[] { dbxSvcAgentDSOPathName } ;
            } else {
                dbxSvcAgentDSOPathPrefix = System.getProperty("dbxSvcAgentDSOPathPrefix");
                if (dbxSvcAgentDSOPathPrefix == null) {
                    dbxSvcAgentDSOPathPrefix = defaultDbxSvcAgentDSOPathPrefix;
                }
                if (cpu.equals("sparc")) {
                    dbxSvcAgentDSOPathNames = new String[] {
                        dbxSvcAgentDSOPathPrefix + fileSep + os + fileSep + cpu + "v9" + fileSep + "lib" + fileSep + "libsvc_agent_dbx.so",
                        dbxSvcAgentDSOPathPrefix + fileSep + os + fileSep + cpu + fileSep + "lib" + fileSep + "libsvc_agent_dbx.so",
                    };
                } else {
                    dbxSvcAgentDSOPathNames = new String[] {
                        dbxSvcAgentDSOPathPrefix + fileSep + os + fileSep + cpu + fileSep + "lib" + fileSep + "libsvc_agent_dbx.so"
                    };
                }
            }
            DbxDebuggerLocal dbg = new DbxDebuggerLocal(null, dbxPathName, dbxSvcAgentDSOPathNames, !isServer);
            debugger = dbg;
            attachDebugger();
            if (cpu.equals("x86")) {
                machDesc = new MachineDescriptionIntelX86();
            } else if (cpu.equals("sparc")) {
                int addressSize = dbg.getRemoteProcessAddressSize();
                if (addressSize == -1) {
                    throw new DebuggerException("Error occurred while trying to determine the remote process's address size. It's possible that the Serviceability Agent's dbx module failed to initialize. Examine the standard output and standard error streams from the dbx process for more information.");
                }
                if (addressSize == 32) {
                    machDesc = new MachineDescriptionSPARC32Bit();
                } else if (addressSize == 64) {
                    machDesc = new MachineDescriptionSPARC64Bit();
                } else {
                    throw new DebuggerException("Address size " + addressSize + " is not supported on SPARC");
                }
            }
            dbg.setMachineDescription(machDesc);
        }
    }
    private void connectRemoteDebugger() throws DebuggerException {
        RemoteDebugger remote =
        (RemoteDebugger) RMIHelper.lookup(debugServerID);
        debugger = new RemoteDebuggerClient(remote);
        machDesc = ((RemoteDebuggerClient) debugger).getMachineDescription();
        os = debugger.getOS();
        if (os.equals("solaris")) {
            setupJVMLibNamesSolaris();
        } else if (os.equals("win32")) {
            setupJVMLibNamesWin32();
        } else if (os.equals("linux")) {
            setupJVMLibNamesLinux();
        } else {
            throw new RuntimeException("Unknown OS type");
        }
        cpu = debugger.getCPU();
    }
    private void setupJVMLibNamesSolaris() {
        jvmLibNames = new String[] { "libjvm.so", "libjvm_g.so", "gamma_g" };
        saLibNames = new String[] { "libsa.so", "libsa_g.so" };
    }
    private void setupDebuggerWin32() {
        setupJVMLibNamesWin32();
        if (cpu.equals("x86")) {
            machDesc = new MachineDescriptionIntelX86();
        } else if (cpu.equals("amd64")) {
            machDesc = new MachineDescriptionAMD64();
        } else if (cpu.equals("ia64")) {
            machDesc = new MachineDescriptionIA64();
        } else {
            throw new DebuggerException("Win32 supported under x86, amd64 and ia64 only");
        }
        if (System.getProperty("sun.jvm.hotspot.debugger.useWindbgDebugger") != null) {
            debugger = new WindbgDebuggerLocal(machDesc, !isServer);
        } else {
            debugger = new Win32DebuggerLocal(machDesc, !isServer);
        }
        attachDebugger();
    }
    private void setupJVMLibNamesWin32() {
        jvmLibNames = new String[] { "jvm.dll", "jvm_g.dll" };
        saLibNames = new String[] { "sa.dll", "sa_g.dll" };
    }
    private void setupDebuggerLinux() {
        setupJVMLibNamesLinux();
        if (cpu.equals("x86")) {
            machDesc = new MachineDescriptionIntelX86();
        } else if (cpu.equals("ia64")) {
            machDesc = new MachineDescriptionIA64();
        } else if (cpu.equals("amd64")) {
            machDesc = new MachineDescriptionAMD64();
        } else if (cpu.equals("sparc")) {
            if (LinuxDebuggerLocal.getAddressSize()==8) {
               machDesc = new MachineDescriptionSPARC64Bit();
            } else {
               machDesc = new MachineDescriptionSPARC32Bit();
            }
        } else {
            throw new DebuggerException("Linux only supported on x86/ia64/amd64/sparc/sparc64");
        }
        debugger = new LinuxDebuggerLocal(machDesc, !isServer);
        attachDebugger();
    }
    private void setupJVMLibNamesLinux() {
        setupJVMLibNamesSolaris();
    }
    private void attachDebugger() {
        if (startupMode == PROCESS_MODE) {
            debugger.attach(pid);
        } else if (startupMode == CORE_FILE_MODE) {
            debugger.attach(executableName, coreFileName);
        } else {
            throw new DebuggerException("Should not call attach() for startupMode == " + startupMode);
        }
    }
}
