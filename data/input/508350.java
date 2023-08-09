public class Monkey {
    private final static int DEBUG_ALLOW_ANY_STARTS = 0;
    private final static int DEBUG_ALLOW_ANY_RESTARTS = 0;
    private IActivityManager mAm;
    private IWindowManager mWm;
    private IPackageManager mPm;
    private String[] mArgs;
    private int mNextArg;
    private String mCurArgData;
    private int mVerbose;
    private boolean mIgnoreCrashes;
    private boolean mIgnoreTimeouts;
    private boolean mIgnoreSecurityExceptions;
    private boolean mMonitorNativeCrashes;
    private boolean mIgnoreNativeCrashes;
    private boolean mSendNoEvents;
    private boolean mAbort;
    private boolean mCountEvents = true;
    private boolean mRequestAnrTraces = false;
    private boolean mRequestDumpsysMemInfo = false;
    private boolean mRequestProcRank = false;
    private boolean mKillProcessAfterError;
    private boolean mGenerateHprof;
    private String mPkgBlacklistFile;
    private String mPkgWhitelistFile;
    private HashSet<String> mValidPackages = new HashSet<String>();
    private HashSet<String> mInvalidPackages = new HashSet<String>();
    private ArrayList<String> mMainCategories = new ArrayList<String>();
    private ArrayList<ComponentName> mMainApps = new ArrayList<ComponentName>();
    long mThrottle = 0;
    boolean mRandomizeThrottle = false;
    int mCount = 1000;
    long mSeed = 0;
    Random mRandom = null;
    long mDroppedKeyEvents = 0;
    long mDroppedPointerEvents = 0;
    long mDroppedTrackballEvents = 0;
    long mDroppedFlipEvents = 0;
    private String mSetupFileName = null;
    private ArrayList<String> mScriptFileNames = new ArrayList<String>();
    private int mServerPort = -1;
    private static final File TOMBSTONES_PATH = new File("/data/tombstones");
    private HashSet<String> mTombstones = null;
    float[] mFactors = new float[MonkeySourceRandom.FACTORZ_COUNT];
    MonkeyEventSource mEventSource;
    private MonkeyNetworkMonitor mNetworkMonitor = new MonkeyNetworkMonitor();
    public static Intent currentIntent;
    public static String currentPackage;
    private boolean checkEnteringPackage(String pkg) {
        if (mInvalidPackages.size() > 0) {
            if (mInvalidPackages.contains(pkg)) {
                return false;
            }
        } else if (mValidPackages.size() > 0) {
            if (!mValidPackages.contains(pkg)) {
                return false;
            }
        }
        return true;
    }
    private class ActivityController extends IActivityController.Stub {
        public boolean activityStarting(Intent intent, String pkg) {
            boolean allow = checkEnteringPackage(pkg) || (DEBUG_ALLOW_ANY_STARTS != 0);
            if (mVerbose > 0) {
                System.out.println("    
                        + intent + " in package " + pkg);
            }
            currentPackage = pkg;
            currentIntent = intent;
            return allow;
        }
        public boolean activityResuming(String pkg) {
            System.out.println("    
            boolean allow = checkEnteringPackage(pkg) || (DEBUG_ALLOW_ANY_RESTARTS != 0);
            if (!allow) {
                if (mVerbose > 0) {
                    System.out.println("    
                            + " resume of package " + pkg);
                }
            }
            currentPackage = pkg;
            return allow;
        }
        public boolean appCrashed(String processName, int pid,
                String shortMsg, String longMsg,
                long timeMillis, String stackTrace) {
            System.err.println("
            System.err.println("
            System.err.println("
            System.err.println("
            System.err.println("
            System.err.println("
            System.err.println("
            if (!mIgnoreCrashes) {
                synchronized (Monkey.this) {
                    mAbort = true;
                }
                return !mKillProcessAfterError;
            }
            return false;
        }
        public int appNotResponding(String processName, int pid, String processStats) {
            System.err.println("
            System.err.println(processStats);
            synchronized (Monkey.this) {
                mRequestAnrTraces = true;
                mRequestDumpsysMemInfo = true;
                mRequestProcRank = true;
            }
            if (!mIgnoreTimeouts) {
                synchronized (Monkey.this) {
                    mAbort = true;
                }
                return (mKillProcessAfterError) ? -1 : 1;
            }
            return 1;
        }
    }
    private void reportProcRank() {
        commandLineReport("procrank", "procrank");
    }
    private void reportAnrTraces() {
        try {
            Thread.sleep(5 * 1000);
        } catch (InterruptedException e) {
        }
        commandLineReport("anr traces", "cat /data/anr/traces.txt");
    }
    private void reportDumpsysMemInfo() {
        commandLineReport("meminfo", "dumpsys meminfo");
    }
    private void commandLineReport(String reportName, String command) {
        System.err.println(reportName + ":");
        Runtime rt = Runtime.getRuntime();
        try {
            java.lang.Process p = Runtime.getRuntime().exec(command);
            InputStream inStream = p.getInputStream();
            InputStreamReader inReader = new InputStreamReader(inStream);
            BufferedReader inBuffer = new BufferedReader(inReader);
            String s;
            while ((s = inBuffer.readLine()) != null) {
                System.err.println(s);
            }
            int status = p.waitFor();
            System.err.println("
        } catch (Exception e) {
            System.err.println("
            System.err.println(e.toString());
        }
    }
    public static void main(String[] args) {
        SystemProperties.set("ro.monkey", "true");
        int resultCode = (new Monkey()).run(args);
        System.exit(resultCode);
    }
    private int run(String[] args) {
        for (String s : args) {
            if ("--wait-dbg".equals(s)) {
                Debug.waitForDebugger();
            }
        }
        mVerbose = 0;
        mCount = 1000;
        mSeed = 0;
        mThrottle = 0;
        mArgs = args;
        mNextArg = 0;
        for (int i = 0; i < MonkeySourceRandom.FACTORZ_COUNT; i++) {
            mFactors[i] = 1.0f;
        }
        if (!processOptions()) {
            return -1;
        }
        if (!loadPackageLists()) {
            return -1;
        }
        if (mMainCategories.size() == 0) {
            mMainCategories.add(Intent.CATEGORY_LAUNCHER);
            mMainCategories.add(Intent.CATEGORY_MONKEY);
        }
        if (mVerbose > 0) {
            System.out.println(":Monkey: seed=" + mSeed + " count=" + mCount);
            if (mValidPackages.size() > 0) {
                Iterator<String> it = mValidPackages.iterator();
                while (it.hasNext()) {
                    System.out.println(":AllowPackage: " + it.next());
                }
            }
            if (mInvalidPackages.size() > 0) {
                Iterator<String> it = mInvalidPackages.iterator();
                while (it.hasNext()) {
                    System.out.println(":DisallowPackage: " + it.next());
                }
            }
            if (mMainCategories.size() != 0) {
                Iterator<String> it = mMainCategories.iterator();
                while (it.hasNext()) {
                    System.out.println(":IncludeCategory: " + it.next());
                }
            }
        }
        if (!checkInternalConfiguration()) {
            return -2;
        }
        if (!getSystemInterfaces()) {
            return -3;
        }
        if (!getMainApps()) {
            return -4;
        }
        mRandom = new SecureRandom();
        mRandom.setSeed((mSeed == 0) ? -1 : mSeed);
        if (mScriptFileNames != null && mScriptFileNames.size() == 1) {
            mEventSource = new MonkeySourceScript(mRandom, mScriptFileNames.get(0), mThrottle,
                    mRandomizeThrottle);
            mEventSource.setVerbose(mVerbose);
            mCountEvents = false;
        } else if (mScriptFileNames != null && mScriptFileNames.size() > 1) {
            if (mSetupFileName != null) {
                mEventSource = new MonkeySourceRandomScript(mSetupFileName, mScriptFileNames,
                        mThrottle, mRandomizeThrottle, mRandom);
                mCount++;
            } else {
                mEventSource = new MonkeySourceRandomScript(mScriptFileNames, mThrottle,
                        mRandomizeThrottle, mRandom);
            }
            mEventSource.setVerbose(mVerbose);
            mCountEvents = false;
        } else if (mServerPort != -1) {
            try {
                mEventSource = new MonkeySourceNetwork(mServerPort);
            } catch (IOException e) {
                System.out.println("Error binding to network socket.");
                return -5;
            }
            mCount = Integer.MAX_VALUE;
        } else {
            if (mVerbose >= 2) { 
                System.out.println("
            }
            mEventSource = new MonkeySourceRandom(mRandom, mMainApps, mThrottle, mRandomizeThrottle);
            mEventSource.setVerbose(mVerbose);
            for (int i = 0; i < MonkeySourceRandom.FACTORZ_COUNT; i++) {
                if (mFactors[i] <= 0.0f) {
                    ((MonkeySourceRandom) mEventSource).setFactors(i, mFactors[i]);
                }
            }
            ((MonkeySourceRandom) mEventSource).generateActivity();
        }
        if (!mEventSource.validate()) {
            return -5;
        }
        if (mGenerateHprof) {
            signalPersistentProcesses();
        }
        mNetworkMonitor.start();
        int crashedAtCycle = runMonkeyCycles();
        mNetworkMonitor.stop();
        synchronized (this) {
            if (mRequestAnrTraces) {
                reportAnrTraces();
                mRequestAnrTraces = false;
            }
            if (mRequestDumpsysMemInfo) {
                reportDumpsysMemInfo();
                mRequestDumpsysMemInfo = false;
            }
        }
        if (mGenerateHprof) {
            signalPersistentProcesses();
            if (mVerbose > 0) {
                System.out.println("
            }
        }
        try {
            mAm.setActivityController(null);
            mNetworkMonitor.unregister(mAm);
        } catch (RemoteException e) {
            if (crashedAtCycle >= mCount) {
                crashedAtCycle = mCount - 1;
            }
        }
        if (mVerbose > 0) {
            System.out.print(":Dropped: keys=");
            System.out.print(mDroppedKeyEvents);
            System.out.print(" pointers=");
            System.out.print(mDroppedPointerEvents);
            System.out.print(" trackballs=");
            System.out.print(mDroppedTrackballEvents);
            System.out.print(" flips=");
            System.out.println(mDroppedFlipEvents);
        }
        mNetworkMonitor.dump();
        if (crashedAtCycle < mCount - 1) {
            System.err.println("** System appears to have crashed at event " + crashedAtCycle
                    + " of " + mCount + " using seed " + mSeed);
            return crashedAtCycle;
        } else {
            if (mVerbose > 0) {
                System.out.println("
            }
            return 0;
        }
    }
    private boolean processOptions() {
        if (mArgs.length < 1) {
            showUsage();
            return false;
        }
        try {
            String opt;
            while ((opt = nextOption()) != null) {
                if (opt.equals("-s")) {
                    mSeed = nextOptionLong("Seed");
                } else if (opt.equals("-p")) {
                    mValidPackages.add(nextOptionData());
                } else if (opt.equals("-c")) {
                    mMainCategories.add(nextOptionData());
                } else if (opt.equals("-v")) {
                    mVerbose += 1;
                } else if (opt.equals("--ignore-crashes")) {
                    mIgnoreCrashes = true;
                } else if (opt.equals("--ignore-timeouts")) {
                    mIgnoreTimeouts = true;
                } else if (opt.equals("--ignore-security-exceptions")) {
                    mIgnoreSecurityExceptions = true;
                } else if (opt.equals("--monitor-native-crashes")) {
                    mMonitorNativeCrashes = true;
                } else if (opt.equals("--ignore-native-crashes")) {
                    mIgnoreNativeCrashes = true;
                } else if (opt.equals("--kill-process-after-error")) {
                    mKillProcessAfterError = true;
                } else if (opt.equals("--hprof")) {
                    mGenerateHprof = true;
                } else if (opt.equals("--pct-touch")) {
                    int i = MonkeySourceRandom.FACTOR_TOUCH;
                    mFactors[i] = -nextOptionLong("touch events percentage");
                } else if (opt.equals("--pct-motion")) {
                    int i = MonkeySourceRandom.FACTOR_MOTION;
                    mFactors[i] = -nextOptionLong("motion events percentage");
                } else if (opt.equals("--pct-trackball")) {
                    int i = MonkeySourceRandom.FACTOR_TRACKBALL;
                    mFactors[i] = -nextOptionLong("trackball events percentage");
                } else if (opt.equals("--pct-nav")) {
                    int i = MonkeySourceRandom.FACTOR_NAV;
                    mFactors[i] = -nextOptionLong("nav events percentage");
                } else if (opt.equals("--pct-majornav")) {
                    int i = MonkeySourceRandom.FACTOR_MAJORNAV;
                    mFactors[i] = -nextOptionLong("major nav events percentage");
                } else if (opt.equals("--pct-appswitch")) {
                    int i = MonkeySourceRandom.FACTOR_APPSWITCH;
                    mFactors[i] = -nextOptionLong("app switch events percentage");
                } else if (opt.equals("--pct-flip")) {
                    int i = MonkeySourceRandom.FACTOR_FLIP;
                    mFactors[i] = -nextOptionLong("keyboard flip percentage");
                } else if (opt.equals("--pct-anyevent")) {
                    int i = MonkeySourceRandom.FACTOR_ANYTHING;
                    mFactors[i] = -nextOptionLong("any events percentage");
                } else if (opt.equals("--pkg-blacklist-file")) {
                    mPkgBlacklistFile = nextOptionData();
                } else if (opt.equals("--pkg-whitelist-file")) {
                    mPkgWhitelistFile = nextOptionData();
                } else if (opt.equals("--throttle")) {
                    mThrottle = nextOptionLong("delay (in milliseconds) to wait between events");
                } else if (opt.equals("--randomize-throttle")) {
                    mRandomizeThrottle = true;
                } else if (opt.equals("--wait-dbg")) {
                } else if (opt.equals("--dbg-no-events")) {
                    mSendNoEvents = true;
                } else if (opt.equals("--port")) {
                    mServerPort = (int) nextOptionLong("Server port to listen on for commands");
                } else if (opt.equals("--setup")) {
                    mSetupFileName = nextOptionData();
                } else if (opt.equals("-f")) {
                    mScriptFileNames.add(nextOptionData());
                } else if (opt.equals("-h")) {
                    showUsage();
                    return false;
                } else {
                    System.err.println("** Error: Unknown option: " + opt);
                    showUsage();
                    return false;
                }
            }
        } catch (RuntimeException ex) {
            System.err.println("** Error: " + ex.toString());
            showUsage();
            return false;
        }
        if (mServerPort == -1) {
            String countStr = nextArg();
            if (countStr == null) {
                System.err.println("** Error: Count not specified");
                showUsage();
                return false;
            }
            try {
                mCount = Integer.parseInt(countStr);
            } catch (NumberFormatException e) {
                System.err.println("** Error: Count is not a number");
                showUsage();
                return false;
            }
        }
        return true;
    }
    private static boolean loadPackageListFromFile(String fileName, HashSet<String> list) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(fileName));
            String s;
            while ((s = reader.readLine()) != null) {
                s = s.trim();
                if ((s.length() > 0) && (!s.startsWith("#"))) {
                    list.add(s);
                }
            }
        } catch (IOException ioe) {
            System.err.println(ioe);
            return false;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ioe) {
                    System.err.println(ioe);
                }
            }
        }
        return true;
    }
    private boolean loadPackageLists() {
        if (((mPkgWhitelistFile != null) || (mValidPackages.size() > 0))
                && (mPkgBlacklistFile != null)) {
            System.err.println("** Error: you can not specify a package blacklist "
                    + "together with a whitelist or individual packages (via -p).");
            return false;
        }
        if ((mPkgWhitelistFile != null)
                && (!loadPackageListFromFile(mPkgWhitelistFile, mValidPackages))) {
            return false;
        }
        if ((mPkgBlacklistFile != null)
                && (!loadPackageListFromFile(mPkgBlacklistFile, mInvalidPackages))) {
            return false;
        }
        return true;
    }
    private boolean checkInternalConfiguration() {
        String lastKeyName = null;
        try {
            lastKeyName = MonkeySourceRandom.getLastKeyName();
        } catch (RuntimeException e) {
        }
        if (!"TAG_LAST_KEYCODE".equals(lastKeyName)) {
            System.err.println("** Error: Key names array malformed (internal error).");
            return false;
        }
        return true;
    }
    private boolean getSystemInterfaces() {
        mAm = ActivityManagerNative.getDefault();
        if (mAm == null) {
            System.err.println("** Error: Unable to connect to activity manager; is the system "
                    + "running?");
            return false;
        }
        mWm = IWindowManager.Stub.asInterface(ServiceManager.getService("window"));
        if (mWm == null) {
            System.err.println("** Error: Unable to connect to window manager; is the system "
                    + "running?");
            return false;
        }
        mPm = IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
        if (mPm == null) {
            System.err.println("** Error: Unable to connect to package manager; is the system "
                    + "running?");
            return false;
        }
        try {
            mAm.setActivityController(new ActivityController());
            mNetworkMonitor.register(mAm);
        } catch (RemoteException e) {
            System.err.println("** Failed talking with activity manager!");
            return false;
        }
        return true;
    }
    private boolean getMainApps() {
        try {
            final int N = mMainCategories.size();
            for (int i = 0; i < N; i++) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                String category = mMainCategories.get(i);
                if (category.length() > 0) {
                    intent.addCategory(category);
                }
                List<ResolveInfo> mainApps = mPm.queryIntentActivities(intent, null, 0);
                if (mainApps == null || mainApps.size() == 0) {
                    System.err.println("
                    continue;
                }
                if (mVerbose >= 2) { 
                    System.out.println("
                }
                final int NA = mainApps.size();
                for (int a = 0; a < NA; a++) {
                    ResolveInfo r = mainApps.get(a);
                    String packageName = r.activityInfo.applicationInfo.packageName;
                    if (checkEnteringPackage(packageName)) {
                        if (mVerbose >= 2) { 
                            System.out.println("
                                    + " (from package " + packageName + ")");
                        }
                        mMainApps.add(new ComponentName(packageName, r.activityInfo.name));
                    } else {
                        if (mVerbose >= 3) { 
                            System.out.println("
                                    + r.activityInfo.name + " (from package " + packageName + ")");
                        }
                    }
                }
            }
        } catch (RemoteException e) {
            System.err.println("** Failed talking with package manager!");
            return false;
        }
        if (mMainApps.size() == 0) {
            System.out.println("** No activities found to run, monkey aborted.");
            return false;
        }
        return true;
    }
    private int runMonkeyCycles() {
        int eventCounter = 0;
        int cycleCounter = 0;
        boolean systemCrashed = false;
        while (!systemCrashed && cycleCounter < mCount) {
            synchronized (this) {
                if (mRequestProcRank) {
                    reportProcRank();
                    mRequestProcRank = false;
                }
                if (mRequestAnrTraces) {
                    reportAnrTraces();
                    mRequestAnrTraces = false;
                }
                if (mRequestDumpsysMemInfo) {
                    reportDumpsysMemInfo();
                    mRequestDumpsysMemInfo = false;
                }
                if (mMonitorNativeCrashes) {
                    if (checkNativeCrashes() && (eventCounter > 0)) {
                        System.out.println("** New native crash detected.");
                        mAbort = mAbort || !mIgnoreNativeCrashes || mKillProcessAfterError;
                    }
                }
                if (mAbort) {
                    System.out.println("** Monkey aborted due to error.");
                    System.out.println("Events injected: " + eventCounter);
                    return eventCounter;
                }
            }
            if (mSendNoEvents) {
                eventCounter++;
                cycleCounter++;
                continue;
            }
            if ((mVerbose > 0) && (eventCounter % 100) == 0 && eventCounter != 0) {
                String calendarTime = MonkeyUtils.toCalendarTime(System.currentTimeMillis());
                long systemUpTime = SystemClock.elapsedRealtime();
                System.out.println("    
                                   + systemUpTime + "]");
                System.out.println("    
            }
            MonkeyEvent ev = mEventSource.getNextEvent();
            if (ev != null) {
                int injectCode = ev.injectEvent(mWm, mAm, mVerbose);
                if (injectCode == MonkeyEvent.INJECT_FAIL) {
                    if (ev instanceof MonkeyKeyEvent) {
                        mDroppedKeyEvents++;
                    } else if (ev instanceof MonkeyMotionEvent) {
                        mDroppedPointerEvents++;
                    } else if (ev instanceof MonkeyFlipEvent) {
                        mDroppedFlipEvents++;
                    }
                } else if (injectCode == MonkeyEvent.INJECT_ERROR_REMOTE_EXCEPTION) {
                    systemCrashed = true;
                    System.err.println("** Error: RemoteException while injecting event.");
                } else if (injectCode == MonkeyEvent.INJECT_ERROR_SECURITY_EXCEPTION) {
                    systemCrashed = !mIgnoreSecurityExceptions;
                    if (systemCrashed) {
                        System.err.println("** Error: SecurityException while injecting event.");
                    }
                }
                if (!(ev instanceof MonkeyThrottleEvent)) {
                    eventCounter++;
                    if (mCountEvents) {
                        cycleCounter++;
                    }
                }
            } else {
                if (!mCountEvents) {
                    cycleCounter++;
                } else {
                    break;
                }
            }
        }
        System.out.println("Events injected: " + eventCounter);
        return eventCounter;
    }
    private void signalPersistentProcesses() {
        try {
            mAm.signalPersistentProcesses(Process.SIGNAL_USR1);
            synchronized (this) {
                wait(2000);
            }
        } catch (RemoteException e) {
            System.err.println("** Failed talking with activity manager!");
        } catch (InterruptedException e) {
        }
    }
    private boolean checkNativeCrashes() {
        String[] tombstones = TOMBSTONES_PATH.list();
        if ((tombstones == null) || (tombstones.length == 0)) {
            mTombstones = null;
            return false;
        }
        HashSet<String> newStones = new HashSet<String>();
        for (String x : tombstones) {
            newStones.add(x);
        }
        boolean result = (mTombstones == null) || !mTombstones.containsAll(newStones);
        mTombstones = newStones;
        return result;
    }
    private String nextOption() {
        if (mNextArg >= mArgs.length) {
            return null;
        }
        String arg = mArgs[mNextArg];
        if (!arg.startsWith("-")) {
            return null;
        }
        mNextArg++;
        if (arg.equals("--")) {
            return null;
        }
        if (arg.length() > 1 && arg.charAt(1) != '-') {
            if (arg.length() > 2) {
                mCurArgData = arg.substring(2);
                return arg.substring(0, 2);
            } else {
                mCurArgData = null;
                return arg;
            }
        }
        mCurArgData = null;
        return arg;
    }
    private String nextOptionData() {
        if (mCurArgData != null) {
            return mCurArgData;
        }
        if (mNextArg >= mArgs.length) {
            return null;
        }
        String data = mArgs[mNextArg];
        mNextArg++;
        return data;
    }
    private long nextOptionLong(final String opt) {
        long result;
        try {
            result = Long.parseLong(nextOptionData());
        } catch (NumberFormatException e) {
            System.err.println("** Error: " + opt + " is not a number");
            throw e;
        }
        return result;
    }
    private String nextArg() {
        if (mNextArg >= mArgs.length) {
            return null;
        }
        String arg = mArgs[mNextArg];
        mNextArg++;
        return arg;
    }
    private void showUsage() {
        StringBuffer usage = new StringBuffer();
        usage.append("usage: monkey [-p ALLOWED_PACKAGE [-p ALLOWED_PACKAGE] ...]\n");
        usage.append("              [-c MAIN_CATEGORY [-c MAIN_CATEGORY] ...]\n");
        usage.append("              [--ignore-crashes] [--ignore-timeouts]\n");
        usage.append("              [--ignore-security-exceptions]\n");
        usage.append("              [--monitor-native-crashes] [--ignore-native-crashes]\n");
        usage.append("              [--kill-process-after-error] [--hprof]\n");
        usage.append("              [--pct-touch PERCENT] [--pct-motion PERCENT]\n");
        usage.append("              [--pct-trackball PERCENT] [--pct-syskeys PERCENT]\n");
        usage.append("              [--pct-nav PERCENT] [--pct-majornav PERCENT]\n");
        usage.append("              [--pct-appswitch PERCENT] [--pct-flip PERCENT]\n");
        usage.append("              [--pct-anyevent PERCENT]\n");
        usage.append("              [--pkg-blacklist-file PACKAGE_BLACKLIST_FILE]\n");
        usage.append("              [--pkg-whitelist-file PACKAGE_WHITELIST_FILE]\n");
        usage.append("              [--wait-dbg] [--dbg-no-events]\n");
        usage.append("              [--setup scriptfile] [-f scriptfile [-f scriptfile] ...]\n");
        usage.append("              [--port port]\n");
        usage.append("              [-s SEED] [-v [-v] ...]\n");
        usage.append("              [--throttle MILLISEC] [--randomize-throttle]\n");
        usage.append("              COUNT");
        System.err.println(usage.toString());
    }
}
