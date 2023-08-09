public class HostConfig extends XMLResourceHandler {
    public static boolean DEBUG = false;
    public static final String ALL = "all";
    static final String SIGNATURE_TEST_PACKAGE_NAME = "SignatureTest";
    static final String DEFAULT_HOST_CONFIG_FILE_NAME = "host_config.xml";
    static final String FILE_SUFFIX_XML = ".xml";
    static final String FILE_SUFFIX_APK = ".apk";
    static final String FILE_SUFFIX_ZIP = ".zip";
    static final String FILE_SUFFIX_JAR = ".jar";
    static final String[] CTS_RESULT_RESOURCES = {"cts_result.xsl", "cts_result.css",
                                                  "logo.gif", "newrule-green.png"};
    private String mConfigRoot;
    private CaseRepository mCaseRepos;
    private ResultRepository mResultRepos;
    private PlanRepository mPlanRepos;
    private HashMap<String, TestPackage> mTestPackageMap;
    enum Ints {
        maxTestCount (200),
        maxTestsInBatchMode (0),
        testStatusTimeoutMs (5 * 60 * 1000),
        batchStartTimeoutMs (30 * 60 * 1000),
        individualStartTimeoutMs (5 * 60 * 1000),
        signatureTestTimeoutMs (10 * 60 * 1000),
        packageInstallTimeoutMs (2 * 60 * 1000),
        postInstallWaitMs (30 * 1000);
        private int value;
        Ints(int value) {
            this.value = value;
        }
        int value() {
            return value;
        }
        void setValue(int value) {
            this.value = value;
        }
    }
    private final static HostConfig sInstance = new HostConfig();
    private HostConfig() {
        mTestPackageMap = new HashMap<String, TestPackage>();
    }
    public static HostConfig getInstance() {
        return sInstance;
    }
    public static int getMaxTestCount() {
        return Ints.maxTestCount.value();
    }
    public boolean load(String configPath) throws SAXException, IOException,
            ParserConfigurationException {
        String fileName = null;
        String[] subDirs = configPath.split("\\" + File.separator);
        for (String d : subDirs) {
            if (d.contains(FILE_SUFFIX_XML)) {
                fileName = d;
            }
        }
        String configFile = null;
        if (fileName == null) {
            if (File.separatorChar == configPath.charAt(configPath.length() - 1)) {
                configPath = configPath.substring(0, configPath.length() - 1);
            }
            mConfigRoot = configPath;
            fileName = DEFAULT_HOST_CONFIG_FILE_NAME;
        } else {
            mConfigRoot = configPath.substring(0, configPath.length() - fileName.length() - 1);
        }
        configFile = mConfigRoot + File.separator + fileName;
        Document doc = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder().parse(new File(configFile));
        String repositoryRoot = getStringAttributeValue(doc
                .getElementsByTagName("Repository").item(0), "root");
        if ((null == repositoryRoot) || (repositoryRoot.length() == 0)) {
            repositoryRoot = mConfigRoot;
        }
        String caseCfg = getStringAttributeValue(doc, "TestCase", "path", fileName);
        String planCfg = getStringAttributeValue(doc, "TestPlan", "path", fileName);
        String resCfg = getStringAttributeValue(doc, "TestResult", "path", fileName);
        if ((caseCfg == null) || (planCfg == null) || (resCfg == null)) {
            return false;
        }
        getConfigValues(doc);
        String caseRoot = repositoryRoot + File.separator + caseCfg;
        String planRoot = repositoryRoot + File.separator + planCfg;
        String resRoot = repositoryRoot + File.separator + resCfg;
        boolean validCase = true;
        if (!validateDirectory(caseRoot)) {
            validCase = new File(caseRoot).mkdirs();
        }
        boolean validRes = true;
        if (!validateDirectory(resRoot)) {
            validRes = new File(resRoot).mkdirs();
        }
        if (validRes) {
            extractResultResources(resRoot);
        }
        boolean validPlan = true;
        if (!validateDirectory(planRoot)) {
            validPlan = new File(planRoot).mkdirs();
        }
        mCaseRepos = new CaseRepository(caseRoot);
        mResultRepos = new ResultRepository(resRoot);
        mPlanRepos = new PlanRepository(planRoot);
        return validCase && validRes && validPlan;
    }
    public void extractResultResources(String resRoot) {
        for (String res: CTS_RESULT_RESOURCES) {
            extractResource(res, resRoot);
        }
    }
    public Collection<TestPackage> getTestPackages() {
        return mTestPackageMap.values();
    }
    public TestPackage getTestPackage(final String packageName) {
        return mTestPackageMap.get(packageName);
    }
    public void loadRepositories() throws NoSuchAlgorithmException {
        loadTestPackages();
        loadTestResults();
    }
    private void loadTestResults() {
        getResultRepository().loadTestResults();
    }
    public void loadTestPackages() throws NoSuchAlgorithmException {
        if (mTestPackageMap.size() == 0) {
            mCaseRepos.loadTestPackages();
        }
    }
    public void removeTestPacakges() {
        mTestPackageMap.clear();
    }
    public String getPackageBinaryName(String appPackageName) {
        for (TestPackage pkg : mTestPackageMap.values()) {
            if (appPackageName.equals(pkg.getAppPackageName())) {
                return pkg.getAppBinaryName();
            }
        }
        return null;
    }
    public String getConfigRoot() {
        return mConfigRoot;
    }
    private String getStringAttributeValue(final Document doc,
            final String tagName, final String attrName, final String fileName) {
        String cfgStr = null;
        try {
            cfgStr = getStringAttributeValue(doc
                    .getElementsByTagName(tagName).item(0), attrName);
            if ((null == cfgStr) || (cfgStr.length() == 0)) {
                Log.e("Configure error (in " + fileName
                        + "), pls make sure <" + tagName + ">'s attribute <"
                        + attrName + ">'s value is correctly set.", null);
                return null;
            }
        } catch (Exception e) {
            Log.e("Configure error (in " + fileName
                    + "), pls make sure <" + tagName
                    + ">'s value is correctly set.", null);
            return null;
        }
        return cfgStr;
    }
    private void getConfigValues(final Document doc) {
        NodeList intValues = doc.getElementsByTagName("IntValue");
        for (int i = 0; i < intValues.getLength(); i++) {
            Node n = intValues.item(i);
            String name = getStringAttributeValue(n, "name");
            String value = getStringAttributeValue(n, "value");
            try {
                Integer v = Integer.parseInt(value);
                Ints.valueOf(name).setValue(v);
            } catch (NumberFormatException e) {
                Log.e("Configuration error. Illegal value for " + name, e);
            } catch (IllegalArgumentException e) {
                Log.e("Unknown configuration value " + name, e);
            }
        }
    }
    private boolean validateDirectory(final String path) {
        File pathFile = new File(path);
        if ((null == pathFile) || (pathFile.exists() == false)
                || (pathFile.isDirectory() == false)) {
            return false;
        }
        return true;
    }
    private boolean extractResource(String name, String dest) {
        File file = new File(dest, name);
        if (!file.exists()) {
            InputStream in = getClass().getResourceAsStream(File.separator + name);
            if (in != null) {
                try {
                    FileOutputStream fout = new FileOutputStream(file);
                    byte[] data = new byte[512];
                    int len = in.read(data);
                    while (len > 0) {
                        fout.write(data, 0, len);
                        len = in.read(data);
                    }
                    fout.flush();
                    fout.close();
                    in.close();
                } catch (FileNotFoundException e) {
                    return false;
                } catch (IOException e) {
                    return false;
                }
            }
        }
        return true;
    }
    public CaseRepository getCaseRepository() {
        return mCaseRepos;
    }
    public PlanRepository getPlanRepository() {
        return mPlanRepos;
    }
    public ResultRepository getResultRepository() {
        return mResultRepos;
    }
    class Repository {
        protected String mRoot;
        Repository(String root) {
            mRoot = root;
        }
        public String getRoot() {
            return mRoot;
        }
        public boolean isValidXmlFile(File f) {
            if (f.getPath().endsWith(FILE_SUFFIX_XML)) {
                return true;
            }
            return false;
        }
    }
    class ResultRepository extends Repository {
        ResultRepository(String root) {
            super(root);
        }
        public void loadTestResults() {
            for (File f : new File(mRoot).listFiles()) {
                if (f.isDirectory()) {
                    String pathName = mRoot + File.separator + f.getName()
                                + File.separator + TestSessionLog.CTS_RESULT_FILE_NAME;
                    if (HostUtils.isFileExist(pathName)) {
                        try {
                            TestSessionLog log =
                                TestSessionLogBuilder.getInstance().build(pathName);
                            TestSession ts = TestSessionBuilder.getInstance().build(log);
                            if (ts != null) {
                                TestHost.getInstance().addSession(ts);
                            }
                        } catch (Exception e) {
                            Log.e("Error importing existing result from " + pathName, e);
                        }
                    }
                }
            }
        }
     }
    class CaseRepository extends Repository {
        CaseRepository(String root) {
            super(root);
        }
        public ArrayList<String> getPackageNames() {
            ArrayList<String> packageNames = new ArrayList<String>();
            for (TestPackage pkg : mTestPackageMap.values()) {
                String binaryName = pkg.getAppBinaryName();
                if (binaryName.equals(SIGNATURE_TEST_PACKAGE_NAME)) {
                    packageNames.add(0, binaryName);
                } else {
                    packageNames.add(pkg.getAppPackageName());
                }
            }
            return packageNames;
        }
        public ArrayList<String> getPackageBinaryNames() {
            ArrayList<String> pkgBinaryNames = new ArrayList<String>();
            for (TestPackage pkg : mTestPackageMap.values()) {
                String pkgBinaryName = pkg.getAppBinaryName();
                if (pkgBinaryName.equals(SIGNATURE_TEST_PACKAGE_NAME)) {
                    pkgBinaryNames.add(0, pkgBinaryName);
                } else {
                    pkgBinaryNames.add(pkg.getAppBinaryName());
                }
            }
            return pkgBinaryNames;
        }
        public List<String> loadPackageXmlFileNames() {
            ArrayList<String> packageXmlFileNames = new ArrayList<String>();
            for (File f : new File(mRoot).listFiles()) {
                if (isValidXmlFile(f)) {
                    String fileName = f.getName();
                    String name = fileName.substring(0, fileName.lastIndexOf("."));
                    packageXmlFileNames.add(name);
                }
            }
            return packageXmlFileNames;
        }
        public void loadTestPackages() throws NoSuchAlgorithmException {
            List<String> pkgXmlFileNameList = loadPackageXmlFileNames();
            for (String pkgXmlFileName : pkgXmlFileNameList) {
                String xmlPath = getRoot() + File.separator
                        + pkgXmlFileName + FILE_SUFFIX_XML;
                TestPackage pkg = loadPackage(xmlPath);
                if (isValidPackage(pkg)) {
                    mTestPackageMap.put(pkg.getAppPackageName(), pkg);
                }
            }
        }
        private String getPackageBinaryName(String packagePath) {
            return packagePath.substring(packagePath.lastIndexOf(File.separator) + 1,
                    packagePath.lastIndexOf("."));
        }
        private boolean isValidPackage(TestPackage pkg) {
            if (pkg == null) {
                return false;
            }
            String pkgFileName = pkg.getAppBinaryName();
            String apkFilePath = mRoot + File.separator + pkgFileName + FILE_SUFFIX_APK;
            String xmlFilePath = mRoot + File.separator + pkgFileName + FILE_SUFFIX_XML;
            File xmlFile = new File(xmlFilePath);
            if (pkg.isHostSideOnly()) {
                if (xmlFile.exists() && xmlFile.isFile()) {
                    return true;
                }
            } else {
                File apkFile = new File(apkFilePath);
                if (xmlFile.exists() && xmlFile.isFile()
                        && apkFile.exists() && apkFile.isFile()) {
                    return true;
                }
            }
            return false;
        }
        public boolean addPackage(String packagePath) throws FileNotFoundException,
                IOException, NoSuchAlgorithmException {
            ZipFile zipFile = new ZipFile(packagePath);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            ArrayList<String> filePathList = new ArrayList<String>();
            String xmlFilePath = null;
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String name = entry.getName();
                if (name.endsWith(FILE_SUFFIX_APK)
                        || name.endsWith(FILE_SUFFIX_XML)
                        || name.endsWith(FILE_SUFFIX_JAR)) {
                    int index = name.lastIndexOf(File.separator);
                    String fileName = name;
                    if (index != -1) {
                        fileName = name.substring(index + 1);
                    }
                    String filePath = mRoot + File.separator + fileName;
                    writeToFile(zipFile.getInputStream(entry), filePath);
                    filePathList.add(filePath);
                    if (name.endsWith(FILE_SUFFIX_XML)) {
                        xmlFilePath = filePath;
                    }
                }
            }
            String packageName = getPackageBinaryName(packagePath);
            PackageZipFileValidator zipValidator = new PackageZipFileValidator();
            if (zipValidator.validate(filePathList, packageName, xmlFilePath) == false) {
                for (String filePath : filePathList) {
                    deleteFile(filePath);
                }
                return false;
            }
            TestPackage pkg = loadPackage(xmlFilePath);
            if (pkg != null) {
                mTestPackageMap.put(pkg.getAppPackageName(), pkg);
            }
            return true;
        }
        private TestPackage loadPackage(String xmlFileName) throws NoSuchAlgorithmException {
            if ((xmlFileName == null) || (xmlFileName.length() == 0)) {
                return null;
            }
            File xmlFile = new File(xmlFileName);
            TestSessionBuilder sessionBuilder;
            TestPackage pkg = null;
            try {
                sessionBuilder = TestSessionBuilder.getInstance();
                pkg = sessionBuilder.loadPackage(xmlFile, null);
            } catch (ParserConfigurationException e) {
            } catch (SAXException e) {
            } catch (IOException e) {
            }
            return pkg;
        }
        public boolean isValidPackageName(String packagePath) {
            if (!packagePath.endsWith(FILE_SUFFIX_ZIP)) {
                Log.e("Package error: package name " + packagePath + " is not a zip file.", null);
                return false;
            }
            String fileName = packagePath.substring(packagePath.lastIndexOf(File.separator) + 1,
                       packagePath.length() - FILE_SUFFIX_ZIP.length());
            String path = mRoot + File.separator + fileName;
            if (HostUtils.isFileExist(path + FILE_SUFFIX_APK)
                    || HostUtils.isFileExist(path + FILE_SUFFIX_XML)) {
                Log.e("Package error: package name " + fileName + " exists already.", null);
                return false;
            }
            return true;
        }
        class PackageZipFileValidator {
            public boolean validate(ArrayList<String> filePathList, String packageName,
                    String xmlFilePath) throws NoSuchAlgorithmException {
                if (xmlFilePath == null) {
                    Log.e("Package error: package doesn't contain XML file: "
                            + packageName + FILE_SUFFIX_XML, null);
                    return false;
                } else {
                    TestPackage pkg = loadPackage(xmlFilePath);
                    if (pkg == null) {
                        Log.e("Package error: the description XML file contained in : "
                                + packageName + FILE_SUFFIX_APK + " is invalid.", null);
                        return false;
                    } else {
                        if (!validateTargetApk(filePathList, pkg.getTargetBinaryName())) {
                            return false;
                        }
                        if (!validateHostControllerJar(filePathList, pkg.getJarPath())) {
                            return false;
                        }
                        String apkFilePath = mRoot + File.separator
                                + packageName + FILE_SUFFIX_APK;
                        if (!filePathList.contains(apkFilePath)) {
                            Log.e("Package error: package doesn't contain APK file: "
                                            + packageName + FILE_SUFFIX_APK, null);
                            return false;
                        }
                    }
                }
                return true;
            }
            private boolean validateHostControllerJar(ArrayList<String> filePathList,
                    String hostControllerJarPath) {
                if ((hostControllerJarPath != null) && (hostControllerJarPath.length() != 0)) {
                    String targetFilePath =
                        mRoot + File.separator + hostControllerJarPath + FILE_SUFFIX_JAR;
                    if (filePathList.contains(targetFilePath)) {
                        return true;
                    }
                } else {
                    return true;
                }
                Log.e("Package error: host controler jar file "
                        + hostControllerJarPath + FILE_SUFFIX_JAR
                        + " is not contained in the package zip file.", null);
                return false;
            }
            private boolean validateTargetApk(ArrayList<String> filePathList, String targetName) {
                if ((targetName != null) && (targetName.length() != 0)) {
                    String targetFileName = mRoot + File.separator + targetName + FILE_SUFFIX_APK;
                    if (filePathList.contains(targetFileName)) {
                        return true;
                    }
                } else {
                    return true;
                }
                Log.e("Package error: target file " + targetName + FILE_SUFFIX_APK
                        + " is not contained in the package zip file.", null);
                return false;
            }
        }
        private void writeToFile(InputStream in, String path) throws IOException {
            OutputStream out = new BufferedOutputStream(new FileOutputStream(path));
            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) >= 0) {
                out.write(buffer, 0, len);
            }
            in.close();
            out.close();
        }
        public void removePackages(String packageName) {
            if ((packageName == null) || (packageName.length() == 0)) {
                return;
            }
            if (packageName.equals(ALL)) {
                ArrayList<String> packageNames = getCaseRepository().getPackageNames();
                for (String pkgName : packageNames) {
                    removePackage(pkgName);
                }
            } else {
                if (!getPackageNames().contains(packageName)) {
                    Log.e("Package " + packageName + " doesn't exist in repository!", null);
                    return;
                }
                removePackage(packageName);
            }
        }
        private void removePackage(String packageName) {
            TestPackage pkg = getTestPackage(packageName);
            if (pkg != null) {
                ArrayList<String> targetBinaryNames = getTargetBinaryNames();
                String targetBinaryName = pkg.getTargetBinaryName();
                if ((targetBinaryName != null) && (targetBinaryName.length() != 0)
                        && (getReferenceCount(targetBinaryNames, targetBinaryName) == 1)) {
                    String targetBinaryFileName = mRoot + File.separator + targetBinaryName
                            + FILE_SUFFIX_APK;
                    deleteFile(targetBinaryFileName);
                }
                ArrayList<String> hostControllers = getHostControllers();
                String hostControllerPath = pkg.getJarPath();
                if ((hostControllerPath != null) && (hostControllerPath.length() != 0)
                        && (getReferenceCount(hostControllers, hostControllerPath) == 1)) {
                    String jarFilePath = mRoot + File.separator
                            + hostControllerPath + FILE_SUFFIX_JAR;
                    deleteFile(jarFilePath);
                }
            }
            String packageBinaryName = pkg.getAppBinaryName();
            mTestPackageMap.remove(pkg.getAppPackageName());
            String apkPath = mRoot + File.separator + packageBinaryName + FILE_SUFFIX_APK;
            String xmlPath = mRoot + File.separator + packageBinaryName + FILE_SUFFIX_XML;
            deleteFile(apkPath);
            deleteFile(xmlPath);
        }
        private int getReferenceCount(ArrayList<String> list, String value) {
            if ((list == null) || (list.size() == 0) || (value == null)) {
                return 0;
            }
            int count = 0;
            for (String str : list) {
                if (value.equals(str)) {
                    count ++;
                }
            }
            return count;
        }
        private ArrayList<String> getTargetBinaryNames() {
            ArrayList<String> targetBinaryNames = new ArrayList<String>();
            for (TestPackage pkg : mTestPackageMap.values()) {
                targetBinaryNames.add(pkg.getTargetBinaryName());
            }
            return targetBinaryNames;
        }
        private ArrayList<String> getHostControllers() {
            ArrayList<String> hostControllers = new ArrayList<String>();
            for (TestPackage pkg : mTestPackageMap.values()) {
                hostControllers.add(pkg.getJarPath());
            }
            return hostControllers;
        }
        private void deleteFile(String filepath) {
            File file = new File(filepath);
            if (file.exists() && file.isFile()) {
                file.delete();
            }
        }
        public String getApkPath(String packageName) {
            return mRoot + File.separator + packageName + FILE_SUFFIX_APK;
        }
        public String getXmlPath(String packageName) {
            return mRoot + File.separator + packageName + FILE_SUFFIX_XML;
        }
        @SuppressWarnings("unchecked")
        public List<ArrayList<String>> listAvailablePackage(String expectPackage) {
            ArrayList<String> packageList = new ArrayList<String>();
            ArrayList<String> suiteList = new ArrayList<String>();
            ArrayList<String> caseList = new ArrayList<String>();
            ArrayList<String> testList = new ArrayList<String>();
            for (TestPackage testPackage : mTestPackageMap.values()) {
                String appPackageName = testPackage.getAppPackageName();
                if (expectPackage.equals(appPackageName)) {
                    testPackage.getTestSuiteNames(appPackageName, suiteList, caseList);
                } else if (appPackageName.startsWith(expectPackage)) {
                    packageList.add(appPackageName);
                } else {
                    if (expectPackage.indexOf(Test.METHOD_SEPARATOR) == -1) {
                        testPackage.getTestCaseNames(expectPackage, caseList, testList);
                    } else {
                        testPackage.getTestNames(expectPackage, testList);
                    }
                }
            }
            return Arrays.asList(packageList, suiteList, caseList, testList);
        }
    }
    class PlanRepository extends Repository {
        PlanRepository(String root) {
            super(root);
        }
        public String getPlanPath(String name) {
            if (mRoot == null) {
                Log.e("Repository uninitialized!", null);
                return null;
            }
            return mRoot + File.separator + name + FILE_SUFFIX_XML;
        }
        public ArrayList<String> getAllPlanNames() {
            ArrayList<String> plans = new ArrayList<String>();
            if (mRoot == null) {
                Log.e("Not specify repository, please check your cts config",
                        null);
                return plans;
            }
            File planRepository = new File(mRoot);
            if (!planRepository.exists()) {
                Log.e("Plan Repository doesn't exist: " + mRoot, null);
                return null;
            }
            for (File f : planRepository.listFiles()) {
                String name = f.getName();
                if (name.endsWith(FILE_SUFFIX_XML)) {
                    plans.add(name.substring(0, name.length() - FILE_SUFFIX_XML.length()));
                }
            }
            return plans;
        }
    }
}
