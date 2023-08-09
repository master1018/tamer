public class MockAddonPackage extends AddonPackage {
    public MockAddonPackage(MockPlatformPackage basePlatform, int revision) {
        super(new MockAddonTarget(basePlatform.getTarget(), revision), null );
    }
    static class MockAddonTarget implements IAndroidTarget {
        private final IAndroidTarget mParentTarget;
        private final int mRevision;
        public MockAddonTarget(IAndroidTarget parentTarget, int revision) {
            mParentTarget = parentTarget;
            mRevision = revision;
        }
        public String getClasspathName() {
            return null;
        }
        public String getDefaultSkin() {
            return null;
        }
        public String getDescription() {
            return "mock addon target";
        }
        public String getFullName() {
            return "mock addon target";
        }
        public String getLocation() {
            return "";
        }
        public String getName() {
            return "mock addon target";
        }
        public IOptionalLibrary[] getOptionalLibraries() {
            return null;
        }
        public IAndroidTarget getParent() {
            return mParentTarget;
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
            return mParentTarget.getVersion();
        }
        public String getVersionName() {
            return String.format("mock-addon-%1$d", getVersion().getApiLevel());
        }
        public String hashString() {
            return getVersionName();
        }
        public boolean isPlatform() {
            return false;
        }
        public boolean canRunOn(IAndroidTarget target) {
            throw new UnsupportedOperationException("Implement this as needed for tests");
        }
        public int compareTo(IAndroidTarget o) {
            throw new UnsupportedOperationException("Implement this as needed for tests");
        }
    }
}
