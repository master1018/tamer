 class ZygoteStartFailedEx extends Exception {
    ZygoteStartFailedEx() {};
    ZygoteStartFailedEx(String s) {super(s);}
    ZygoteStartFailedEx(Throwable cause) {super(cause);}
}
public class Process {
    private static final String LOG_TAG = "Process";
    private static final String ZYGOTE_SOCKET = "zygote";
    public static final String ANDROID_SHARED_MEDIA = "com.android.process.media";
    public static final String GOOGLE_SHARED_APP_CONTENT = "com.google.process.content";
    public static final int SYSTEM_UID = 1000;
    public static final int PHONE_UID = 1001;
    public static final int SHELL_UID = 2000;
    public static final int LOG_UID = 1007;
    public static final int WIFI_UID = 1010;
    public static final int FIRST_APPLICATION_UID = 10000;
    public static final int LAST_APPLICATION_UID = 99999;
    public static final int BLUETOOTH_GID = 2000;
    public static final int THREAD_PRIORITY_DEFAULT = 0;
    public static final int THREAD_PRIORITY_LOWEST = 19;
    public static final int THREAD_PRIORITY_BACKGROUND = 10;
    public static final int THREAD_PRIORITY_FOREGROUND = -2;
    public static final int THREAD_PRIORITY_DISPLAY = -4;
    public static final int THREAD_PRIORITY_URGENT_DISPLAY = -8;
    public static final int THREAD_PRIORITY_AUDIO = -16;
    public static final int THREAD_PRIORITY_URGENT_AUDIO = -19;
    public static final int THREAD_PRIORITY_MORE_FAVORABLE = -1;
    public static final int THREAD_PRIORITY_LESS_FAVORABLE = +1;
    public static final int THREAD_GROUP_DEFAULT = 0;
    public static final int THREAD_GROUP_BG_NONINTERACTIVE = 1;
    public static final int THREAD_GROUP_FG_BOOST = 2;
    public static final int SIGNAL_QUIT = 3;
    public static final int SIGNAL_KILL = 9;
    public static final int SIGNAL_USR1 = 10;
    static LocalSocket sZygoteSocket;
    static DataInputStream sZygoteInputStream;
    static BufferedWriter sZygoteWriter;
    static boolean sPreviousZygoteOpenFailed;
    public static final int start(final String processClass,
                                  final String niceName,
                                  int uid, int gid, int[] gids,
                                  int debugFlags,
                                  String[] zygoteArgs)
    {
        if (supportsProcesses()) {
            try {
                return startViaZygote(processClass, niceName, uid, gid, gids,
                        debugFlags, zygoteArgs);
            } catch (ZygoteStartFailedEx ex) {
                Log.e(LOG_TAG,
                        "Starting VM process through Zygote failed");
                throw new RuntimeException(
                        "Starting VM process through Zygote failed", ex);
            }
        } else {
            Runnable runnable = new Runnable() {
                        public void run() {
                            Process.invokeStaticMain(processClass);
                        }
            };
            if (niceName != null) {
                new Thread(runnable, niceName).start();
            } else {
                new Thread(runnable).start();
            }
            return 0;
        }
    }
    public static final int start(String processClass, int uid, int gid,
            int[] gids, int debugFlags, String[] zygoteArgs) {
        return start(processClass, "", uid, gid, gids, 
                debugFlags, zygoteArgs);
    }
    private static void invokeStaticMain(String className) {
        Class cl;
        Object args[] = new Object[1];
        args[0] = new String[0];     
        try {
            cl = Class.forName(className);
            cl.getMethod("main", new Class[] { String[].class })
                    .invoke(null, args);            
        } catch (Exception ex) {
            Log.e(LOG_TAG, "Exception invoking static main on " 
                    + className, ex);
            throw new RuntimeException(ex);
        }
    }
    static final int ZYGOTE_RETRY_MILLIS = 500;
    private static void openZygoteSocketIfNeeded() 
            throws ZygoteStartFailedEx {
        int retryCount;
        if (sPreviousZygoteOpenFailed) {
            retryCount = 0;
        } else {
            retryCount = 10;            
        }
        for (int retry = 0
                ; (sZygoteSocket == null) && (retry < (retryCount + 1))
                ; retry++ ) {
            if (retry > 0) {
                try {
                    Log.i("Zygote", "Zygote not up yet, sleeping...");
                    Thread.sleep(ZYGOTE_RETRY_MILLIS);
                } catch (InterruptedException ex) {
                }
            }
            try {
                sZygoteSocket = new LocalSocket();
                sZygoteSocket.connect(new LocalSocketAddress(ZYGOTE_SOCKET, 
                        LocalSocketAddress.Namespace.RESERVED));
                sZygoteInputStream
                        = new DataInputStream(sZygoteSocket.getInputStream());
                sZygoteWriter =
                    new BufferedWriter(
                            new OutputStreamWriter(
                                    sZygoteSocket.getOutputStream()),
                            256);
                Log.i("Zygote", "Process: zygote socket opened");
                sPreviousZygoteOpenFailed = false;
                break;
            } catch (IOException ex) {
                if (sZygoteSocket != null) {
                    try {
                        sZygoteSocket.close();
                    } catch (IOException ex2) {
                        Log.e(LOG_TAG,"I/O exception on close after exception",
                                ex2);
                    }
                }
                sZygoteSocket = null;
            }
        }
        if (sZygoteSocket == null) {
            sPreviousZygoteOpenFailed = true;
            throw new ZygoteStartFailedEx("connect failed");                 
        }
    }
    private static int zygoteSendArgsAndGetPid(ArrayList<String> args)
            throws ZygoteStartFailedEx {
        int pid;
        openZygoteSocketIfNeeded();
        try {
            sZygoteWriter.write(Integer.toString(args.size()));
            sZygoteWriter.newLine();
            int sz = args.size();
            for (int i = 0; i < sz; i++) {
                String arg = args.get(i);
                if (arg.indexOf('\n') >= 0) {
                    throw new ZygoteStartFailedEx(
                            "embedded newlines not allowed");
                }
                sZygoteWriter.write(arg);
                sZygoteWriter.newLine();
            }
            sZygoteWriter.flush();
            pid = sZygoteInputStream.readInt();
            if (pid < 0) {
                throw new ZygoteStartFailedEx("fork() failed");
            }
        } catch (IOException ex) {
            try {
                if (sZygoteSocket != null) {
                    sZygoteSocket.close();
                }
            } catch (IOException ex2) {
                Log.e(LOG_TAG,"I/O exception on routine close", ex2);
            }
            sZygoteSocket = null;
            throw new ZygoteStartFailedEx(ex);
        }
        return pid;
    }
    private static int startViaZygote(final String processClass,
                                  final String niceName,
                                  final int uid, final int gid,
                                  final int[] gids,
                                  int debugFlags,
                                  String[] extraArgs)
                                  throws ZygoteStartFailedEx {
        int pid;
        synchronized(Process.class) {
            ArrayList<String> argsForZygote = new ArrayList<String>();
            argsForZygote.add("--runtime-init");
            argsForZygote.add("--setuid=" + uid);
            argsForZygote.add("--setgid=" + gid);
            if ((debugFlags & Zygote.DEBUG_ENABLE_SAFEMODE) != 0) {
                argsForZygote.add("--enable-safemode");
            }
            if ((debugFlags & Zygote.DEBUG_ENABLE_DEBUGGER) != 0) {
                argsForZygote.add("--enable-debugger");
            }
            if ((debugFlags & Zygote.DEBUG_ENABLE_CHECKJNI) != 0) {
                argsForZygote.add("--enable-checkjni");
            }
            if ((debugFlags & Zygote.DEBUG_ENABLE_ASSERT) != 0) {
                argsForZygote.add("--enable-assert");
            }
            if (gids != null && gids.length > 0) {
                StringBuilder sb = new StringBuilder();
                sb.append("--setgroups=");
                int sz = gids.length;
                for (int i = 0; i < sz; i++) {
                    if (i != 0) {
                        sb.append(',');
                    }
                    sb.append(gids[i]);
                }
                argsForZygote.add(sb.toString());
            }
            if (niceName != null) {
                argsForZygote.add("--nice-name=" + niceName);
            }
            argsForZygote.add(processClass);
            if (extraArgs != null) {
                for (String arg : extraArgs) {
                    argsForZygote.add(arg);
                }
            }
            pid = zygoteSendArgsAndGetPid(argsForZygote);
        }
        if (pid <= 0) {
            throw new ZygoteStartFailedEx("zygote start failed:" + pid);
        }
        return pid;
    }
    public static final native long getElapsedCpuTime();
    public static final native int myPid();
    public static final native int myTid();
    public static final native int myUid();
    public static final native int getUidForName(String name);
    public static final native int getGidForName(String name);
    public static final int getUidForPid(int pid) {
        String[] procStatusLabels = { "Uid:" };
        long[] procStatusValues = new long[1];
        procStatusValues[0] = -1;
        Process.readProcLines("/proc/" + pid + "/status", procStatusLabels, procStatusValues);
        return (int) procStatusValues[0];
    }
    public static final native void setThreadPriority(int tid, int priority)
            throws IllegalArgumentException, SecurityException;
    public static final native void setThreadGroup(int tid, int group)
            throws IllegalArgumentException, SecurityException;
    public static final native void setProcessGroup(int pid, int group)
            throws IllegalArgumentException, SecurityException;
    public static final native void setThreadPriority(int priority)
            throws IllegalArgumentException, SecurityException;
    public static final native int getThreadPriority(int tid)
            throws IllegalArgumentException;
    public static final native boolean supportsProcesses();
    public static final native boolean setOomAdj(int pid, int amt);
    public static final native void setArgV0(String text);
    public static final void killProcess(int pid) {
        sendSignal(pid, SIGNAL_KILL);
    }
    public static final native int setUid(int uid);
    public static final native int setGid(int uid);
    public static final native void sendSignal(int pid, int signal);
    public static final void killProcessQuiet(int pid) {
        sendSignalQuiet(pid, SIGNAL_KILL);
    }
    public static final native void sendSignalQuiet(int pid, int signal);
    public static final native long getFreeMemory();
    public static final native void readProcLines(String path,
            String[] reqFields, long[] outSizes);
    public static final native int[] getPids(String path, int[] lastArray);
    public static final int PROC_TERM_MASK = 0xff;
    public static final int PROC_ZERO_TERM = 0;
    public static final int PROC_SPACE_TERM = (int)' ';
    public static final int PROC_TAB_TERM = (int)'\t';
    public static final int PROC_COMBINE = 0x100;
    public static final int PROC_PARENS = 0x200;
    public static final int PROC_OUT_STRING = 0x1000;
    public static final int PROC_OUT_LONG = 0x2000;
    public static final int PROC_OUT_FLOAT = 0x4000;
    public static final native boolean readProcFile(String file, int[] format,
            String[] outStrings, long[] outLongs, float[] outFloats);
    public static final native boolean parseProcLine(byte[] buffer, int startIndex, 
            int endIndex, int[] format, String[] outStrings, long[] outLongs, float[] outFloats);
    public static final native long getPss(int pid);
}
