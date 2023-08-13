public class RemoteAndroidTestRunner implements IRemoteAndroidTestRunner  {
    private final String mPackageName;
    private final  String mRunnerName;
    private IDevice mRemoteDevice;
    private Map<String, String> mArgMap;
    private InstrumentationResultParser mParser;
    private static final String LOG_TAG = "RemoteAndroidTest";
    private static final String DEFAULT_RUNNER_NAME = "android.test.InstrumentationTestRunner";
    private static final char CLASS_SEPARATOR = ',';
    private static final char METHOD_SEPARATOR = '#';
    private static final char RUNNER_SEPARATOR = '/';
    private static final String CLASS_ARG_NAME = "class";
    private static final String LOG_ARG_NAME = "log";
    private static final String DEBUG_ARG_NAME = "debug";
    private static final String COVERAGE_ARG_NAME = "coverage";
    private static final String PACKAGE_ARG_NAME = "package";
    public RemoteAndroidTestRunner(String packageName,
                                   String runnerName,
                                   IDevice remoteDevice) {
        mPackageName = packageName;
        mRunnerName = runnerName;
        mRemoteDevice = remoteDevice;
        mArgMap = new Hashtable<String, String>();
    }
    public RemoteAndroidTestRunner(String packageName,
                                   IDevice remoteDevice) {
        this(packageName, null, remoteDevice);
    }
    public String getPackageName() {
        return mPackageName;
    }
    public String getRunnerName() {
        if (mRunnerName == null) {
            return DEFAULT_RUNNER_NAME;
        }
        return mRunnerName;
    }
    private String getRunnerPath() {
        return getPackageName() + RUNNER_SEPARATOR + getRunnerName();
    }
    public void setClassName(String className) {
        addInstrumentationArg(CLASS_ARG_NAME, className);
    }
    public void setClassNames(String[] classNames) {
        StringBuilder classArgBuilder = new StringBuilder();
        for (int i = 0; i < classNames.length; i++) {
            if (i != 0) {
                classArgBuilder.append(CLASS_SEPARATOR);
            }
            classArgBuilder.append(classNames[i]);
        }
        setClassName(classArgBuilder.toString());
    }
    public void setMethodName(String className, String testName) {
        setClassName(className + METHOD_SEPARATOR + testName);
    }
    public void setTestPackageName(String packageName) {
        addInstrumentationArg(PACKAGE_ARG_NAME, packageName);
    }
    public void addInstrumentationArg(String name, String value) {
        if (name == null || value == null) {
            throw new IllegalArgumentException("name or value arguments cannot be null");
        }
        mArgMap.put(name, value);
    }
    public void addBooleanArg(String name, boolean value) {
        addInstrumentationArg(name, Boolean.toString(value));
    }
    public void setLogOnly(boolean logOnly) {
        addBooleanArg(LOG_ARG_NAME, logOnly);
    }
    public void setDebug(boolean debug) {
        addBooleanArg(DEBUG_ARG_NAME, debug);
    }
    public void setCoverage(boolean coverage) {
        addBooleanArg(COVERAGE_ARG_NAME, coverage);
    }
    public void run(ITestRunListener... listeners) {
        run(Arrays.asList(listeners));
    }
    public void run(Collection<ITestRunListener> listeners) {
        final String runCaseCommandStr = String.format("am instrument -w -r %s %s",
            getArgsCommand(), getRunnerPath());
        Log.d(LOG_TAG, runCaseCommandStr);
        mParser = new InstrumentationResultParser(listeners);
        try {
            mRemoteDevice.executeShellCommand(runCaseCommandStr, mParser);
        } catch (IOException e) {
            Log.e(LOG_TAG, e);
            for (ITestRunListener listener : listeners) {
                listener.testRunFailed(e.toString());
            }
        }
    }
    public void cancel() {
        if (mParser != null) {
            mParser.cancel();
        }
    }
    private String getArgsCommand() {
        StringBuilder commandBuilder = new StringBuilder();
        for (Entry<String, String> argPair : mArgMap.entrySet()) {
            final String argCmd = String.format(" -e %s %s", argPair.getKey(),
                    argPair.getValue());
            commandBuilder.append(argCmd);
        }
        return commandBuilder.toString();
    }
}
