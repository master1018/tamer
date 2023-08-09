public class MockPlatformPackage extends PlatformPackage {
    private final IAndroidTarget mTarget;
    public MockPlatformPackage(int apiLevel, int revision) {
        this(new MockPlatformTarget(apiLevel, revision), null );
    }
    public MockPlatformPackage(int apiLevel, int revision, int min_tools_rev) {
        this(new MockPlatformTarget(apiLevel, revision), createProps(min_tools_rev));
    }
    private MockPlatformPackage(IAndroidTarget target, Properties props) {
        super(target, props);
        mTarget = target;
    }
    private static Properties createProps(int min_tools_rev) {
        Properties props = new Properties();
        props.setProperty(PlatformPackage.PROP_MIN_TOOLS_REV, Integer.toString((min_tools_rev)));
        return props;
    }
    public IAndroidTarget getTarget() {
        return mTarget;
    }
    static class MockPlatformTarget implements IAndroidTarget {
        private final int mApiLevel;
        private final int mRevision;
        public MockPlatformTarget(int apiLevel, int revision) {
            mApiLevel = apiLevel;
            mRevision = revision;
        }
        public String getClasspathName() {
            return null;
        }
        public String getDefaultSkin() {
            return null;
        }
        public String getDescription() {
            return "mock platform target";
        }
        public String getFullName() {
            return "mock platform target";
        }
        public String getLocation() {
            return "";
        }
        public String getName() {
            return "mock platform target";
        }
        public IOptionalLibrary[] getOptionalLibraries() {
            return null;
        }
        public IAndroidTarget getParent() {
            return null;
        }
        public String getPath(int pathId) {
            return null;
        }
        public String[] getPlatformLibraries() {
            return null;
        }
        public String getProperty(String name) {
            return null;
        }
        public Integer getProperty(String name, Integer defaultValue) {
            return defaultValue;
        }
        public Boolean getProperty(String name, Boolean defaultValue) {
            return defaultValue;
        }
        public Map<String, String> getProperties() {
            return null;
        }
        public int getRevision() {
            return mRevision;
        }
        public String[] getSkins() {
            return null;
        }
        public int getUsbVendorId() {
            return 0;
        }
        public String getVendor() {
            return null;
        }
        public AndroidVersion getVersion() {
            return new AndroidVersion(mApiLevel, null );
        }
        public String getVersionName() {
            return String.format("android-%1$d", mApiLevel);
        }
        public String hashString() {
            return getVersionName();
        }
        public boolean isPlatform() {
            return true;
        }
        public boolean canRunOn(IAndroidTarget target) {
            throw new UnsupportedOperationException("Implement this as needed for tests");
        }
        public int compareTo(IAndroidTarget o) {
            throw new UnsupportedOperationException("Implement this as needed for tests");
        }
    }
}
