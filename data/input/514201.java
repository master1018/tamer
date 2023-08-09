public class ApiList {
    private HashMap<String,PackageInfo> mPackageList;
    private String mDebugString;
    private int mWarnings, mErrors;
    public ApiList(String debugString) {
        mPackageList = new HashMap<String,PackageInfo>();
        mDebugString = debugString;
    }
    public String getDebugString() {
        return mDebugString;
    }
    public void incrWarnings() {
        mWarnings++;
    }
    public void incrErrors() {
        mErrors++;
    }
    public int getWarningCount() {
        return mWarnings;
    }
    public int getErrorCount() {
        return mErrors;
    }
    public PackageInfo getPackage(String name) {
        return mPackageList.get(name);
    }
    public PackageInfo getOrCreatePackage(String name) {
        PackageInfo pkgInfo = mPackageList.get(name);
        if (pkgInfo == null) {
            pkgInfo = new PackageInfo(name);
            mPackageList.put(name, pkgInfo);
        }
        return pkgInfo;
    }
    public Iterator<PackageInfo> getPackageIterator() {
        return mPackageList.values().iterator();
    }
}
