class ZygoteConnection {
    private static final String TAG = "Zygote";
    private static final int[][] intArray2d = new int[0][0];
    private static final int CONNECTION_TIMEOUT_MILLIS = 1000;
    private static final int MAX_ZYGOTE_ARGC=1024;
    private final LocalSocket mSocket;
    private final DataOutputStream mSocketOutStream;
    private final BufferedReader mSocketReader;
    private final Credentials peer;
    private static LocalSocket sPeerWaitSocket = null;
    ZygoteConnection(LocalSocket socket) throws IOException {
        mSocket = socket;
        mSocketOutStream
                = new DataOutputStream(socket.getOutputStream());
        mSocketReader = new BufferedReader(
                new InputStreamReader(socket.getInputStream()), 256);
        mSocket.setSoTimeout(CONNECTION_TIMEOUT_MILLIS);
        try {
            peer = mSocket.getPeerCredentials();
        } catch (IOException ex) {
            Log.e(TAG, "Cannot read peer credentials", ex);
            throw ex;
        }
    }
    FileDescriptor getFileDesciptor() {
        return mSocket.getFileDescriptor();
    }
    void run() throws ZygoteInit.MethodAndArgsCaller {
        int loopCount = ZygoteInit.GC_LOOP_COUNT;
        while (true) {
            if (loopCount <= 0) {
                ZygoteInit.gc();
                loopCount = ZygoteInit.GC_LOOP_COUNT;
            } else {
                loopCount--;
            }
            if (runOnce()) {
                break;
            }
        }
    }
    boolean runOnce() throws ZygoteInit.MethodAndArgsCaller {
        String args[];
        Arguments parsedArgs = null;
        FileDescriptor[] descriptors;
        try {
            args = readArgumentList();
            descriptors = mSocket.getAncillaryFileDescriptors();
        } catch (IOException ex) {
            Log.w(TAG, "IOException on command socket " + ex.getMessage());
            closeSocket();
            return true;
        }
        if (args == null) {
            closeSocket();
            return true;
        }
        PrintStream newStderr = null;
        if (descriptors != null && descriptors.length >= 3) {
            newStderr = new PrintStream(
                    new FileOutputStream(descriptors[2]));
        }
        int pid;
        try {
            parsedArgs = new Arguments(args);
            applyUidSecurityPolicy(parsedArgs, peer);
            applyDebuggerSecurityPolicy(parsedArgs);
            applyRlimitSecurityPolicy(parsedArgs, peer);
            applyCapabilitiesSecurityPolicy(parsedArgs, peer);
            int[][] rlimits = null;
            if (parsedArgs.rlimits != null) {
                rlimits = parsedArgs.rlimits.toArray(intArray2d);
            }
            pid = Zygote.forkAndSpecialize(parsedArgs.uid, parsedArgs.gid,
                    parsedArgs.gids, parsedArgs.debugFlags, rlimits);
        } catch (IllegalArgumentException ex) {
            logAndPrintError (newStderr, "Invalid zygote arguments", ex);
            pid = -1;
        } catch (ZygoteSecurityException ex) {
            logAndPrintError(newStderr,
                    "Zygote security policy prevents request: ", ex);
            pid = -1;
        }
        if (pid == 0) {
            handleChildProc(parsedArgs, descriptors, newStderr);
            return true;
        } else { 
            return handleParentProc(pid, descriptors, parsedArgs);
        }
    }
    void closeSocket() {
        try {
            mSocket.close();
        } catch (IOException ex) {
            Log.e(TAG, "Exception while closing command "
                    + "socket in parent", ex);
        }
    }
    static class Arguments {
        int uid = 0;
        boolean uidSpecified;
        int gid = 0;
        boolean gidSpecified;
        int[] gids;
        boolean peerWait;
        int debugFlags;
        String classpath;
        boolean runtimeInit;
        boolean capabilitiesSpecified;
        long permittedCapabilities;
        long effectiveCapabilities;
        ArrayList<int[]> rlimits;
        String remainingArgs[];
        Arguments(String args[]) throws IllegalArgumentException {
            parseArgs(args);
        }
        private void parseArgs(String args[])
                throws IllegalArgumentException {
            int curArg = 0;
            for (  ; curArg < args.length; curArg++) {
                String arg = args[curArg];
                if (arg.equals("--")) {
                    curArg++;
                    break;
                } else if (arg.startsWith("--setuid=")) {
                    if (uidSpecified) {
                        throw new IllegalArgumentException(
                                "Duplicate arg specified");
                    }
                    uidSpecified = true;
                    uid = Integer.parseInt(
                            arg.substring(arg.indexOf('=') + 1));
                } else if (arg.startsWith("--setgid=")) {
                    if (gidSpecified) {
                        throw new IllegalArgumentException(
                                "Duplicate arg specified");
                    }
                    gidSpecified = true;
                    gid = Integer.parseInt(
                            arg.substring(arg.indexOf('=') + 1));
                } else if (arg.equals("--enable-debugger")) {
                    debugFlags |= Zygote.DEBUG_ENABLE_DEBUGGER;
                } else if (arg.equals("--enable-safemode")) {
                    debugFlags |= Zygote.DEBUG_ENABLE_SAFEMODE;
                } else if (arg.equals("--enable-checkjni")) {
                    debugFlags |= Zygote.DEBUG_ENABLE_CHECKJNI;
                } else if (arg.equals("--enable-assert")) {
                    debugFlags |= Zygote.DEBUG_ENABLE_ASSERT;
                } else if (arg.equals("--peer-wait")) {
                    peerWait = true;
                } else if (arg.equals("--runtime-init")) {
                    runtimeInit = true;
                } else if (arg.startsWith("--capabilities=")) {
                    if (capabilitiesSpecified) {
                        throw new IllegalArgumentException(
                                "Duplicate arg specified");
                    }
                    capabilitiesSpecified = true;
                    String capString = arg.substring(arg.indexOf('=')+1);
                    String[] capStrings = capString.split(",", 2);
                    if (capStrings.length == 1) {
                        effectiveCapabilities = Long.decode(capStrings[0]);
                        permittedCapabilities = effectiveCapabilities;
                    } else {
                        permittedCapabilities = Long.decode(capStrings[0]);
                        effectiveCapabilities = Long.decode(capStrings[1]);
                    }
                } else if (arg.startsWith("--rlimit=")) {
                    String[] limitStrings
                            = arg.substring(arg.indexOf('=')+1).split(",");
                    if (limitStrings.length != 3) {
                        throw new IllegalArgumentException(
                                "--rlimit= should have 3 comma-delimited ints");
                    }
                    int[] rlimitTuple = new int[limitStrings.length];
                    for(int i=0; i < limitStrings.length; i++) {
                        rlimitTuple[i] = Integer.parseInt(limitStrings[i]);
                    }
                    if (rlimits == null) {
                        rlimits = new ArrayList();
                    }
                    rlimits.add(rlimitTuple);
                } else if (arg.equals("-classpath")) {
                    if (classpath != null) {
                        throw new IllegalArgumentException(
                                "Duplicate arg specified");
                    }
                    try {
                        classpath = args[++curArg];
                    } catch (IndexOutOfBoundsException ex) {
                        throw new IllegalArgumentException(
                                "-classpath requires argument");
                    }
                } else if (arg.startsWith("--setgroups=")) {
                    if (gids != null) {
                        throw new IllegalArgumentException(
                                "Duplicate arg specified");
                    }
                    String[] params
                            = arg.substring(arg.indexOf('=') + 1).split(",");
                    gids = new int[params.length];
                    for (int i = params.length - 1; i >= 0 ; i--) {
                        gids[i] = Integer.parseInt(params[i]);
                    }
                } else {
                    break;
                }
            }
            if (runtimeInit && classpath != null) {
                throw new IllegalArgumentException(
                        "--runtime-init and -classpath are incompatible");
            }
            remainingArgs = new String[args.length - curArg];
            System.arraycopy(args, curArg, remainingArgs, 0,
                    remainingArgs.length);
        }
    }
    private String[] readArgumentList()
            throws IOException {
        int argc;
        try {
            String s = mSocketReader.readLine();
            if (s == null) {
                return null;
            }
            argc = Integer.parseInt(s);
        } catch (NumberFormatException ex) {
            Log.e(TAG, "invalid Zygote wire format: non-int at argc");
            throw new IOException("invalid wire format");
        }
        if (argc > MAX_ZYGOTE_ARGC) {   
            throw new IOException("max arg count exceeded");
        }
        String[] result = new String[argc];
        for (int i = 0; i < argc; i++) {
            result[i] = mSocketReader.readLine();
            if (result[i] == null) {
                throw new IOException("truncated request");
            }
        }
        return result;
    }
    private static void applyUidSecurityPolicy(Arguments args, Credentials peer)
            throws ZygoteSecurityException {
        int peerUid = peer.getUid();
        if (peerUid == 0) {
        } else if (peerUid == Process.SYSTEM_UID ) {
            String factoryTest = SystemProperties.get("ro.factorytest");
            boolean uidRestricted;
            uidRestricted  
                 = !(factoryTest.equals("1") || factoryTest.equals("2"));
            if (uidRestricted
                    && args.uidSpecified && (args.uid < Process.SYSTEM_UID)) {
                throw new ZygoteSecurityException(
                        "System UID may not launch process with UID < "
                                + Process.SYSTEM_UID);
            }
        } else {
            if (args.uidSpecified || args.gidSpecified
                || args.gids != null) {
                throw new ZygoteSecurityException(
                        "App UIDs may not specify uid's or gid's");
            }
        }
        if (!args.uidSpecified) {
            args.uid = peer.getUid();
            args.uidSpecified = true;
        }
        if (!args.gidSpecified) {
            args.gid = peer.getGid();
            args.gidSpecified = true;
        }
    }
    private static void applyDebuggerSecurityPolicy(Arguments args) {
        if ("1".equals(SystemProperties.get("ro.debuggable"))) {
            args.debugFlags |= Zygote.DEBUG_ENABLE_DEBUGGER;
        }
    }
    private static void applyRlimitSecurityPolicy(
            Arguments args, Credentials peer)
            throws ZygoteSecurityException {
        int peerUid = peer.getUid();
        if (!(peerUid == 0 || peerUid == Process.SYSTEM_UID)) {
            if (args.rlimits != null) {
                throw new ZygoteSecurityException(
                        "This UID may not specify rlimits.");
            }
        }
    }
    private static void applyCapabilitiesSecurityPolicy(
            Arguments args, Credentials peer)
            throws ZygoteSecurityException {
        if (args.permittedCapabilities == 0
                && args.effectiveCapabilities == 0) {
            return;
        }
        if (peer.getUid() == 0) {
            return;
        }
        long permittedCaps;
        try {
            permittedCaps = ZygoteInit.capgetPermitted(peer.getPid());
        } catch (IOException ex) {
            throw new ZygoteSecurityException(
                    "Error retrieving peer's capabilities.");
        }
        if (((~args.permittedCapabilities) & args.effectiveCapabilities) != 0) {
            throw new ZygoteSecurityException(
                    "Effective capabilities cannot be superset of "
                            + " permitted capabilities" );
        }
        if (((~permittedCaps) & args.permittedCapabilities) != 0) {
            throw new ZygoteSecurityException(
                    "Peer specified unpermitted capabilities" );
        }
    }
    private void handleChildProc(Arguments parsedArgs,
            FileDescriptor[] descriptors, PrintStream newStderr)
            throws ZygoteInit.MethodAndArgsCaller {
        if (parsedArgs.peerWait) {
            try {
                ZygoteInit.setCloseOnExec(mSocket.getFileDescriptor(), true);
                sPeerWaitSocket = mSocket;
            } catch (IOException ex) {
                Log.e(TAG, "Zygote Child: error setting peer wait "
                        + "socket to be close-on-exec", ex);
            }
        } else {
            closeSocket();
            ZygoteInit.closeServerSocket();
        }
        if (descriptors != null) {
            try {
                ZygoteInit.reopenStdio(descriptors[0],
                        descriptors[1], descriptors[2]);
                for (FileDescriptor fd: descriptors) {
                    ZygoteInit.closeDescriptor(fd);
                }
                newStderr = System.err;
            } catch (IOException ex) {
                Log.e(TAG, "Error reopening stdio", ex);
            }
        }
        if (parsedArgs.runtimeInit) {
            RuntimeInit.zygoteInit(parsedArgs.remainingArgs);
        } else {
            ClassLoader cloader;
            if (parsedArgs.classpath != null) {
                cloader
                    = new PathClassLoader(parsedArgs.classpath,
                    ClassLoader.getSystemClassLoader());
            } else {
                cloader = ClassLoader.getSystemClassLoader();
            }
            String className;
            try {
                className = parsedArgs.remainingArgs[0];
            } catch (ArrayIndexOutOfBoundsException ex) {
                logAndPrintError (newStderr,
                        "Missing required class name argument", null);
                return;
            }
            String[] mainArgs
                    = new String[parsedArgs.remainingArgs.length - 1];
            System.arraycopy(parsedArgs.remainingArgs, 1,
                    mainArgs, 0, mainArgs.length);
            try {
                ZygoteInit.invokeStaticMain(cloader, className, mainArgs);
            } catch (RuntimeException ex) {
                logAndPrintError (newStderr, "Error starting. ", ex);
            }
        }
    }
    private boolean handleParentProc(int pid,
            FileDescriptor[] descriptors, Arguments parsedArgs) {
        if(pid > 0) {
            try {
                ZygoteInit.setpgid(pid, ZygoteInit.getpgid(peer.getPid()));
            } catch (IOException ex) {
                Log.i(TAG, "Zygote: setpgid failed. This is "
                    + "normal if peer is not in our session");
            }
        }
        try {
            if (descriptors != null) {
                for (FileDescriptor fd: descriptors) {
                    ZygoteInit.closeDescriptor(fd);
                }
            }
        } catch (IOException ex) {
            Log.e(TAG, "Error closing passed descriptors in "
                    + "parent process", ex);
        }
        try {
            mSocketOutStream.writeInt(pid);
        } catch (IOException ex) {
            Log.e(TAG, "Error reading from command socket", ex);
            return true;
        }
        if (parsedArgs.peerWait) {
            try {
                mSocket.close();
            } catch (IOException ex) {
                Log.e(TAG, "Zygote: error closing sockets", ex);
            }
            return true;
        }
        return false;
    }
    private static void logAndPrintError (PrintStream newStderr,
            String message, Throwable ex) {
        Log.e(TAG, message, ex);
        if (newStderr != null) {
            newStderr.println(message + (ex == null ? "" : ex));
        }
    }
}
