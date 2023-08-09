public class HotSpotAgent {
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
    private String javaExecutableName;
    private String coreFileName;
    private String debugServerID;
    private String serverID;
    private String[] jvmLibNames;
    private static final String defaultDbxPathPrefix                = "/net/jano.sfbay/export/disk05/hotspot/sa";
    private static final String defaultDbxSvcAgentDSOPathPrefix     = "/net/jano.sfbay/export/disk05/hotspot/sa";
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
    public HotSpotAgent() {
        Runtime.getRuntime().addShutdownHook(new java.lang.Thread(
        new Runnable() {
            public void run() {
                synchronized (HotSpotAgent.this) {
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
    public synchronized TypeDataBase getTypeDataBase() {
        return db;
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
    public synchronized void attach(String javaExecutableName, String coreFileName)
    throws DebuggerException {
        if (debugger != null) {
            throw new DebuggerException("Already attached");
        }
        if ((javaExecutableName == null) || (coreFileName == null)) {
            throw new DebuggerException("Both the core file name and Java executable name must be specified");
        }
        this.javaExecutableName = javaExecutableName;
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
    public synchronized void startServer(int processID, String uniqueID) {
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
    public synchronized void startServer(String javaExecutableName,
    String coreFileName,
    String uniqueID) {
        if (debugger != null) {
            throw new DebuggerException("Already attached");
        }
        if ((javaExecutableName == null) || (coreFileName == null)) {
            throw new DebuggerException("Both the core file name and Java executable name must be specified");
        }
        this.javaExecutableName = javaExecutableName;
        this.coreFileName = coreFileName;
        startupMode = CORE_FILE_MODE;
        isServer = true;
        serverID = uniqueID;
        go();
    }
    public synchronized void startServer(String javaExecutableName, String coreFileName)
    throws DebuggerException {
        startServer(javaExecutableName, coreFileName, null);
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
        setupVM();
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
    private void setupVM() {
        try {
            if (os.equals("solaris")) {
                db = new HotSpotTypeDataBase(machDesc,
                new HotSpotSolarisVtblAccess(debugger, jvmLibNames),
                debugger, jvmLibNames);
            } else if (os.equals("win32")) {
                db = new HotSpotTypeDataBase(machDesc,
                new Win32VtblAccess(debugger, jvmLibNames),
                debugger, jvmLibNames);
            } else if (os.equals("linux")) {
                db = new HotSpotTypeDataBase(machDesc,
                new LinuxVtblAccess(debugger, jvmLibNames),
                debugger, jvmLibNames);
            } else {
                throw new DebuggerException("OS \"" + os + "\" not yet supported (no VtblAccess yet)");
            }
        }
        catch (NoSuchSymbolException e) {
            throw new DebuggerException("Doesn't appear to be a HotSpot VM (could not find symbol \"" +
            e.getSymbol() + "\" in remote process)");
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
            try {
                VM.initialize(db, debugger);
            } catch (DebuggerException e) {
                throw (e);
            } catch (Exception e) {
                throw new DebuggerException(e);
            }
        }
    }
    private void setupDebuggerSolaris() {
        setupJVMLibNamesSolaris();
        if(System.getProperty("sun.jvm.hotspot.debugger.useProcDebugger") != null) {
            ProcDebuggerLocal dbg = new ProcDebuggerLocal(null, true);
            debugger = dbg;
            attachDebugger();
            if (cpu.equals("x86")) {
                machDesc = new MachineDescriptionIntelX86();
            } else if (cpu.equals("sparc")) {
                int addressSize = dbg.getRemoteProcessAddressSize();
                if (addressSize == -1) {
                    throw new DebuggerException("Error occurred while trying to determine the remote process's " +
                    "address size");
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
                        dbxSvcAgentDSOPathPrefix + fileSep + os + fileSep + cpu + "v9" + fileSep + "lib" +
                        fileSep + "libsvc_agent_dbx.so",
                        dbxSvcAgentDSOPathPrefix + fileSep + os + fileSep + cpu + fileSep + "lib" +
                        fileSep + "libsvc_agent_dbx.so",
                    };
                } else {
                    dbxSvcAgentDSOPathNames = new String[] {
                        dbxSvcAgentDSOPathPrefix + fileSep + os + fileSep + cpu + fileSep + "lib" +
                        fileSep + "libsvc_agent_dbx.so"
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
                    throw new DebuggerException("Error occurred while trying to determine the remote process's " +
                    "address size. It's possible that the Serviceability Agent's dbx module failed to " +
                    "initialize. Examine the standard output and standard error streams from the dbx " +
                    "process for more information.");
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
        LinuxDebuggerLocal dbg =
        new LinuxDebuggerLocal(machDesc, !isServer);
        debugger = dbg;
        attachDebugger();
    }
    private void setupJVMLibNamesLinux() {
        jvmLibNames = new String[] { "libjvm.so", "libjvm_g.so" };
    }
    private void attachDebugger() {
        if (startupMode == PROCESS_MODE) {
            debugger.attach(pid);
        } else if (startupMode == CORE_FILE_MODE) {
            debugger.attach(javaExecutableName, coreFileName);
        } else {
            throw new DebuggerException("Should not call attach() for startupMode == " + startupMode);
        }
    }
}
