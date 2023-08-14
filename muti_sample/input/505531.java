public class ApkSettings {
    private boolean mSplitByDpi = false;
    public ApkSettings() {
    }
    public ApkSettings(ProjectProperties properties) {
        boolean splitByDensity = Boolean.parseBoolean(properties.getProperty(
                ProjectProperties.PROPERTY_SPLIT_BY_DENSITY));
        setSplitByDensity(splitByDensity);
    }
    public Map<String, String> getResourceFilters() {
        Map<String, String> map = new HashMap<String, String>();
        if (mSplitByDpi) {
            map.put("hdpi", "hdpi,nodpi");
            map.put("mdpi", "mdpi,nodpi");
            map.put("ldpi", "ldpi,nodpi");
        }
        return map;
    }
    public boolean isSplitByDpi() {
        return mSplitByDpi;
    }
    public void setSplitByDensity(boolean split) {
        mSplitByDpi = split;
    }
    public void write(ProjectProperties properties) {
    }
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ApkSettings) {
            return mSplitByDpi == ((ApkSettings) obj).mSplitByDpi;
        }
        return false;
    }
    @Override
    public int hashCode() {
        return Boolean.valueOf(mSplitByDpi).hashCode();
    }
}
