public class TestHost extends XMLResourceHandler implements SessionObserver {
    public static final String TEMP_PLAN_NAME = "tempPlan";
    enum ActionType {
        RUN_SINGLE_TEST, RUN_SINGLE_JAVA_PACKAGE, START_NEW_SESSION, RESUME_SESSION
    }
    enum MODE {
        UNINITIALIZED, RUN, CONSOLE
    }
    static private ArrayList<TestSession> sSessions = new ArrayList<TestSession>();
    static private DeviceManager sDeviceManager = new DeviceManager();
    static private Object sTestSessionSync = new Object();
    static private ConsoleUi sConsoleUi;
    static private HostConfig sConfig;
    private static TestHost sInstance;
    static MODE sMode = MODE.UNINITIALIZED;
    public static void main(final String[] mainArgs) {
        CUIOutputStream.println("Android CTS version " + Version.asString());
        if (HostLock.lock() == false) {
            Log.e("Error: CTS is being used at the moment."
                    + " No more than one CTS instance is allowed simultaneously", null);
            exit();
        }
        sDeviceManager.initAdb();
        sConsoleUi = new ConsoleUi(getInstance());
        CommandParser cp = init(sConsoleUi, mainArgs);
        if (sMode == MODE.RUN) {
            try {
                Thread.sleep(3000);
                cp.removeKey(CTSCommand.OPTION_CFG);
                sConsoleUi.processCommand(cp);
            } catch (InterruptedException e) {
                Log.e("Met InterruptedException", e);
            } catch (Exception e) {
                Log.e("Met exception when processing command", e);
            }
        } else if (sMode == MODE.CONSOLE) {
            sConsoleUi.startUi();
        }
        exit();
    }
    private static void exit() {
        Log.closeLog();
        HostLock.release();
        System.exit(-1);
    }
    static private MODE getMode(final CommandParser cp) {
        String action = cp.getAction();
        if ((action != null) && (action.equals(CTSCommand.START))) {
            return MODE.RUN;
        } else {
            return MODE.CONSOLE;
        }
    }
    public void startZippedPackage(final String pathName)
                throws FileNotFoundException,
                       IOException,
                       ParserConfigurationException,
                       TransformerFactoryConfigurationError,
                       TransformerException,
                       DeviceNotAvailableException,
                       TestNotFoundException,
                       SAXException,
                       TestPlanNotFoundException,
                       IllegalTestNameException,
                       InterruptedException, DeviceDisconnectedException,
                       NoSuchAlgorithmException, InvalidNameSpaceException,
                       InvalidApkPathException {
        if (!addPackage(pathName)) {
            return;
        }
        ArrayList<String> packages = new ArrayList<String>();
        String pkgName = pathName.substring(pathName
                .lastIndexOf(File.separator) + 1, pathName.lastIndexOf("."));
        packages.add(pkgName);
        HashMap<String, ArrayList<String>> selectedResult =
                       new HashMap<String, ArrayList<String>>();
        selectedResult.put(pkgName, null);
        TestSessionBuilder.getInstance().serialize(TEMP_PLAN_NAME, packages, selectedResult);
        TestSession ts = startSession(TEMP_PLAN_NAME, getFirstAvailableDevice().getSerialNumber(),
                null);
        String resultName = pathName.substring(0, pathName.lastIndexOf("."))
                + ".zip";
        TestSessionLog log = ts.getSessionLog();
        copyFile(log.getResultPath() + ".zip", resultName);
        removePlans(TEMP_PLAN_NAME);
        Thread.sleep(1000);
        removePackages(pkgName);
        Thread.sleep(1000);
    }
    private void copyFile(final String srcFileName, final String dstFileName) throws IOException {
        FileReader input = new FileReader(new File(srcFileName));
        BufferedWriter output = new BufferedWriter(new FileWriter(dstFileName));
        int c;
        while ((c = input.read()) != -1) {
            output.write(c);
        }
        input.close();
        output.flush();
        output.close();
    }
    public boolean addPackage(final String pathName) throws FileNotFoundException,
            IOException, NoSuchAlgorithmException {
        CaseRepository caseRepo = sConfig.getCaseRepository();
        if (!HostUtils.isFileExist(pathName)) {
            Log.e("Package error: package file " + pathName + " doesn't exist.", null);
            return false;
        }
        if (!caseRepo.isValidPackageName(pathName)) {
            return false;
        }
        caseRepo.addPackage(pathName);
        return true;
    }
    public void removePlans(final String name) {
        if ((name == null) || (name.length() == 0)) {
            CUIOutputStream.println("Please add plan name or all as parameter.");
            return;
        }
        PlanRepository planRepo = sConfig.getPlanRepository();
        if (name.equals(HostConfig.ALL)) {
            ArrayList<String> plans = planRepo.getAllPlanNames();
            for (String plan : plans) {
                removePlan(plan, planRepo);
            }
        } else {
            if (!planRepo.getAllPlanNames().contains(name)) {
                Log.e("No plan named " + name + " in repository!", null);
                return;
            }
            removePlan(name, planRepo);
        }
    }
    private void removePlan(final String planName, final PlanRepository planRepo) {
        File planFile = new File(planRepo.getPlanPath(planName));
        if (!planFile.isFile() || !planFile.exists()) {
            Log.e("Can't locate the file of the plan, please check your repository!", null);
            return;
        }
        if (!planFile.canWrite()) {
            Log.e("Can't delete this plan, permission denied!", null);
            return;
        }
        if (!planFile.delete()) {
            Log.e(planName + " plan file delete failed", null);
        }
    }
    public void removePackages(final String packageName)
            throws IndexOutOfBoundsException {
        CaseRepository caseRepo = sConfig.getCaseRepository();
        if ((packageName == null) || (packageName.length() == 0)) {
            CUIOutputStream.println("Please add package name or all as parameter.");
            return;
        }
        caseRepo.removePackages(packageName);
    }
    static CommandParser init(final ConsoleUi cui, final String[] mainArgs) {
        CommandParser cp = null;
        String cfgPath= null;
        if (mainArgs.length == 0) {
            sMode = MODE.CONSOLE;
            cfgPath = System.getProperty("HOST_CONFIG");
            if ((cfgPath == null) || (cfgPath.length() == 0)) {
                Log.e("Please make sure environment variable CTS_HOST_CFG is "
                       + "set as {cts install path}[/host_config.xml].", null);
                exit();
            }
        } else if (mainArgs.length == 1) {
            sMode = MODE.CONSOLE;
            cfgPath = mainArgs[0];
        } else {
            String cmdLine = "";
            for (int i = 0; i < mainArgs.length; i ++) {
                cmdLine += mainArgs[i] + " ";
            }
            try {
                cp = CommandParser.parse(cmdLine);
                if (!cui.validateCommandParams(cp)) {
                    Log.e("Please type in arguments correctly to activate CTS.", null);
                    exit();
                }
            } catch (UnknownCommandException e1) {
                Log.e("Please type in arguments correctly to activate CTS.", null);
                exit();
            } catch (CommandNotFoundException e1) {
                Log.e("Please type in arguments correctly to activate CTS.", null);
                exit();
            }
            sMode = getMode(cp);
            if (sMode == MODE.RUN) {
                if (cp.containsKey(CTSCommand.OPTION_CFG)) {
                    cfgPath = cp.getValue(CTSCommand.OPTION_CFG);
                } else {
                    cfgPath = System.getProperty("HOST_CONFIG");
                    if ((cfgPath == null) || (cfgPath.length() == 0)) {
                        Log.e("Please make sure environment variable CTS_HOST_CFG "
                               + "is set as {cts install path}[/host_config.xml].", null);
                        exit();
                    }
                }
            }
        }
        if ((cfgPath == null) || (cfgPath.length() == 0)) {
            Log.e("Please type in arguments correctly to activate CTS.", null);
            exit();
        }
        String filePath = getConfigFilePath(cfgPath);
        try {
            if (loadConfig(filePath) == false) {
                exit();
            }
            Log.initLog(sConfig.getConfigRoot());
            sConfig.loadRepositories();
        } catch (Exception e) {
            Log.e("Error while parsing cts config file", e);
            exit();
        }
        return cp;
    }
    public static TestHost getInstance() {
        if (sInstance == null) {
            sInstance = new TestHost();
        }
        return sInstance;
    }
    static private String getConfigFilePath(final String filePath) {
        if (filePath != null) {
            if (!HostUtils.isFileExist(filePath)) {
                Log.e("Configuration file \"" + filePath + "\" doesn't exist.", null);
                exit();
            }
        } else {
            Log.e("Configuration file doesn't exist.", null);
            exit();
        }
        return filePath;
    }
    static boolean loadConfig(final String configPath) throws SAXException,
            IOException, ParserConfigurationException {
        sConfig = HostConfig.getInstance();
        return sConfig.load(configPath);
    }
    public HostConfig.CaseRepository getCaseRepository() {
        return sConfig.getCaseRepository();
    }
    public HostConfig.PlanRepository getPlanRepository() {
        return sConfig.getPlanRepository();
    }
    static private void runTest(final TestSession ts, final String deviceId,
            final String testFullName, final String javaPkgName, ActionType type) throws
            DeviceNotAvailableException, TestNotFoundException, IllegalTestNameException,
            DeviceDisconnectedException, InvalidNameSpaceException,
            InvalidApkPathException {
        if (ts == null) {
            return;
        }
        ts.setObserver(getInstance());
        TestDevice device = sDeviceManager.allocateFreeDeviceById(deviceId);
        TestSessionLog sessionLog = ts.getSessionLog();
        ts.setTestDevice(device);
        ts.getDevice().installDeviceSetupApp();
        sessionLog.setDeviceInfo(ts.getDevice().getDeviceInfo());
        boolean finish = false;
        while (!finish) {
            ts.getDevice().disableKeyguard();
            try {
                switch (type) {
                case RUN_SINGLE_TEST:
                    ts.start(testFullName);
                    break;
                case RUN_SINGLE_JAVA_PACKAGE:
                    ts.start(javaPkgName);
                    break;
                case START_NEW_SESSION:
                    ts.start();
                    break;
                case RESUME_SESSION:
                    ts.resume();
                    break;
                }
                finish = true;
            } catch (ADBServerNeedRestartException e) {
                Log.d(e.getMessage());
                Log.i("Max ADB operations reached. Restarting ADB...");
                TestSession.setADBServerRestartedMode();
                sDeviceManager.restartADBServer(ts);
                type = ActionType.RESUME_SESSION;
            }
        }
        TestSession.resetADBServerRestartedMode();
        if (HostConfig.getMaxTestCount() > 0) {
            sDeviceManager.resetTestDevice(ts.getDevice());
        }
        ts.getDevice().uninstallDeviceSetupApp();
    }
    static public TestSession createSession(final String testPlanName)
            throws IOException, TestNotFoundException, SAXException,
            ParserConfigurationException, TestPlanNotFoundException, NoSuchAlgorithmException {
        String testPlanPath = sConfig.getPlanRepository().getPlanPath(testPlanName);
        TestSession ts = TestSessionBuilder.getInstance().build(testPlanPath);
        sSessions.add(ts);
        return ts;
    }
    public void notifyFinished(final TestSession ts) {
        Log.d("Session " + ts.getId() + " finished.");
        synchronized (sTestSessionSync) {
            sTestSessionSync.notify();
        }
        ts.getSessionLog().sessionComplete();
    }
    public void tearDown() {
        AndroidDebugBridge.disconnectBridge();
        AndroidDebugBridge.terminate();
    }
    public Collection<TestSession> getSessions() {
        return sSessions;
    }
    public TestSession getSession(final int sessionId) {
        for (TestSession session : sSessions) {
            if (session.getId() == sessionId) {
                return session;
            }
        }
        return null;
    }
    public ArrayList<TestSession> getSessionList(final String testPlanName) {
        ArrayList<TestSession> list = new ArrayList<TestSession>();
        for (TestSession session : sSessions) {
            if (testPlanName.equals(session.getSessionLog().getTestPlanName())) {
                list.add(session);
            }
        }
        return list;
    }
    public String[] listDevices() {
        ArrayList<String> deviceList = new ArrayList<String>();
        TestDevice[] devices = sDeviceManager.getDeviceList();
        for (TestDevice device : devices) {
            deviceList.add(device.getSerialNumber() + "\t" + device.getStatusAsString());
        }
        return deviceList.toArray(new String[deviceList.size()]);
    }
    public TestDevice[] getDeviceList() {
        return sDeviceManager.getDeviceList();
    }
    public TestDevice getFirstAvailableDevice() {
        for (TestDevice td : sDeviceManager.getDeviceList()) {
            if (td.getStatus() == TestDevice.STATUS_IDLE) {
                return td;
            }
        }
        return null;
    }
    public Collection<TestSessionLog> getSessionLogs() {
        ArrayList<TestSessionLog> sessionLogs = new ArrayList<TestSessionLog>();
        for (TestSession session : sSessions) {
            sessionLogs.add(session.getSessionLog());
        }
        return sessionLogs;
    }
    public TestSession startSession(final String testPlanName,
            String deviceId, final String javaPkgName)
            throws IOException, DeviceNotAvailableException,
            TestNotFoundException, SAXException, ParserConfigurationException,
            TestPlanNotFoundException, IllegalTestNameException,
            DeviceDisconnectedException, NoSuchAlgorithmException,
            InvalidNameSpaceException, InvalidApkPathException {
        TestSession ts = createSession(testPlanName);
        if ((javaPkgName != null) && (javaPkgName.length() != 0)) {
            runTest(ts, deviceId, null, javaPkgName, ActionType.RUN_SINGLE_JAVA_PACKAGE);
        } else {
            runTest(ts, deviceId, null, javaPkgName, ActionType.START_NEW_SESSION);
        }
        ts.getSessionLog().sessionComplete();
        return ts;
    }
    public TestSession startSession(final TestSession ts, String deviceId,
            final String testFullName, final String javaPkgName, ActionType type)
            throws DeviceNotAvailableException,
            TestNotFoundException, IllegalTestNameException,
            DeviceDisconnectedException, InvalidNameSpaceException,
            InvalidApkPathException {
        runTest(ts, deviceId, testFullName, javaPkgName, type);
        ts.getSessionLog().sessionComplete();
        return ts;
    }
    public String getPlanName(final String rawPlanName) {
        if (rawPlanName.indexOf("\\") != -1) {
            return rawPlanName.replaceAll("\\\\", "");
        }
        if (rawPlanName.indexOf("\"") != -1) {
            return rawPlanName.replaceAll("\"", "");
        }
        return rawPlanName;
    }
    public void addSession(TestSession ts) {
        sSessions.add(ts);
    }
}
