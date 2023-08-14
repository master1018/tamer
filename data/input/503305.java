public abstract class CtsTestBase extends TestCase {
    public static final String APK_SUFFIX = ".apk";
    public static final String DESCRITION_SUFFIX = ".xml";
    protected static final String ROOT = "tmp";
    protected static final String CONFIG_PATH = ROOT + File.separator + "host_config.xml";
    private static final String CASE_REPOSITORY = "case_rep_demo";
    private static final String RESULT_REPOSITORY = "result_rep_demo";
    private static final String PLAN_REPOSITORY = "plan_rep_demo";
    @Override
    public void setUp() {
        new File(ROOT).mkdirs();
        initConfig();
        Log.initLog(ROOT);
    }
    @Override
    public void tearDown() {
        Log.closeLog();
        clearDirectory(ROOT);
    }
    private void initConfig() {
        StringBuilder buf = new StringBuilder();
        buf.append("<HostConfiguration>");
        buf.append("\t<Repository root=\"" + ROOT + "\" >");
        buf.append("\t\t<TestPlan path=\"" + PLAN_REPOSITORY + "\" />");
        buf.append("\t\t<TestCase path=\"" + CASE_REPOSITORY + "\" />");
        buf.append("\t\t<TestResult path=\"" + RESULT_REPOSITORY + "\" />");
        buf.append("\t</Repository>");
        buf.append("</HostConfiguration>");
        try {
            new File(ROOT + File.separator + PLAN_REPOSITORY).mkdirs();
            new File(ROOT + File.separator + CASE_REPOSITORY).mkdirs();
            new File(ROOT + File.separator + RESULT_REPOSITORY).mkdirs();
            createFile(buf.toString(), CONFIG_PATH);
        } catch (IOException e1) {
            fail("Can't create config file");
        }
        try {
            TestHost.loadConfig(CONFIG_PATH);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Can't initiate config");
        }
    }
    protected void createTestPackage(String xmlMsg, String packageName) throws IOException {
        String caseRoot = HostConfig.getInstance().getCaseRepository()
                .getRoot();
        String apkPath = caseRoot + File.separator + packageName + APK_SUFFIX;
        String xmlPath = caseRoot + File.separator + packageName
                + DESCRITION_SUFFIX;
        createFile(null, apkPath);
        createFile(xmlMsg, xmlPath);
    }
    protected void deleteTestPackage(String path) {
        String apkPath = path + File.separator + APK_SUFFIX;
        String desPath = path + File.separator + DESCRITION_SUFFIX;
        deleteFile(apkPath);
        deleteFile(desPath);
    }
    protected void createFile(String content, String filePath)
            throws IOException {
        BufferedWriter out = new BufferedWriter(new FileWriter(filePath));
        if (content != null) {
            out.write(content);
        }
        out.close();
    }
    protected void deleteFile(String path) {
        File f = new File(path);
        if (f.exists() && f.canWrite()) {
            f.delete();
        }
    }
    private void clearDirectory(String path) {
        File root = new File(path);
        for (File f : root.listFiles()) {
            if (f.isFile()) {
                f.delete();
            } else {
                deleteDirectory(f);
            }
        }
        root.delete();
    }
    private void deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                } else {
                    files[i].delete();
                }
            }
            path.delete();
        }
    }
}
