public class AdtTestData {
    private static AdtTestData sInstance = null;
    private static final Logger sLogger = Logger.getLogger(AdtTestData.class.getName());
    private String mOsRootDataPath;
    private AdtTestData() {
        mOsRootDataPath = System.getProperty("test_data");
        if (mOsRootDataPath == null) {
            sLogger.info("Cannot find test_data environment variable, init to class loader");
            URL url = this.getClass().getClassLoader().getResource(".");  
            if (Platform.isRunning()) {
                sLogger.info("Running as an Eclipse Plug-in JUnit test, using FileLocator");
                try {
                    mOsRootDataPath = FileLocator.resolve(url).getFile();
                } catch (IOException e) {
                    sLogger.warning("IOException while using FileLocator, reverting to url");
                    mOsRootDataPath = url.getFile();
                }
            } else {
                sLogger.info("Running as an plain JUnit test, using url as-is");
                mOsRootDataPath = url.getFile();
            }
        }
        if (mOsRootDataPath.equals(AndroidConstants.WS_SEP)) {
            sLogger.warning("Resource data not found using class loader!, Defaulting to no path");
        }
        if (!mOsRootDataPath.endsWith(File.separator)) {
            sLogger.info("Fixing test_data env variable (does not end with path separator)");
            mOsRootDataPath = mOsRootDataPath.concat(File.separator);
        }
    }
    public static AdtTestData getInstance() {
        if (sInstance == null) {
            sInstance = new AdtTestData();
        }
        return sInstance;
    }
    public String getTestFilePath(String osRelativePath) {
        return mOsRootDataPath + osRelativePath;
    }
}
