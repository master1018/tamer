final class PlatformTarget implements IAndroidTarget {
    private final static String PLATFORM_HASH = "android-%s";
    private final static String PLATFORM_VENDOR = "Android Open Source Project";
    private final static String PLATFORM_NAME = "Android %s";
    private final static String PLATFORM_NAME_PREVIEW = "Android %s (Preview)";
    private final String mLocation;
    private final String mName;
    private final AndroidVersion mVersion;
    private final String mVersionName;
    private final int mRevision;
    private final Map<String, String> mProperties;
    private final Map<Integer, String> mPaths = new HashMap<Integer, String>();
    private String[] mSkins;
    PlatformTarget(String location, Map<String, String> properties,
            int apiLevel, String codeName, String versionName, int revision) {
        if (location.endsWith(File.separator) == false) {
            location = location + File.separator;
        }
        mLocation = location;
        mProperties = Collections.unmodifiableMap(properties);
        mVersion = new AndroidVersion(apiLevel, codeName);
        mVersionName = versionName;
        mRevision = revision;
        if (mVersion.isPreview()) {
            mName =  String.format(PLATFORM_NAME_PREVIEW, mVersionName);
        } else {
            mName = String.format(PLATFORM_NAME, mVersionName);
        }
        mPaths.put(ANDROID_JAR, mLocation + SdkConstants.FN_FRAMEWORK_LIBRARY);
        mPaths.put(SOURCES, mLocation + SdkConstants.FD_ANDROID_SOURCES);
        mPaths.put(ANDROID_AIDL, mLocation + SdkConstants.FN_FRAMEWORK_AIDL);
        mPaths.put(IMAGES, mLocation + SdkConstants.OS_IMAGES_FOLDER);
        mPaths.put(SAMPLES, mLocation + SdkConstants.OS_PLATFORM_SAMPLES_FOLDER);
        mPaths.put(SKINS, mLocation + SdkConstants.OS_SKINS_FOLDER);
        mPaths.put(TEMPLATES, mLocation + SdkConstants.OS_PLATFORM_TEMPLATES_FOLDER);
        mPaths.put(DATA, mLocation + SdkConstants.OS_PLATFORM_DATA_FOLDER);
        mPaths.put(ATTRIBUTES, mLocation + SdkConstants.OS_PLATFORM_ATTRS_XML);
        mPaths.put(MANIFEST_ATTRIBUTES, mLocation + SdkConstants.OS_PLATFORM_ATTRS_MANIFEST_XML);
        mPaths.put(RESOURCES, mLocation + SdkConstants.OS_PLATFORM_RESOURCES_FOLDER);
        mPaths.put(FONTS, mLocation + SdkConstants.OS_PLATFORM_FONTS_FOLDER);
        mPaths.put(LAYOUT_LIB, mLocation + SdkConstants.OS_PLATFORM_DATA_FOLDER +
                SdkConstants.FN_LAYOUTLIB_JAR);
        mPaths.put(WIDGETS, mLocation + SdkConstants.OS_PLATFORM_DATA_FOLDER +
                SdkConstants.FN_WIDGETS);
        mPaths.put(ACTIONS_ACTIVITY, mLocation + SdkConstants.OS_PLATFORM_DATA_FOLDER +
                SdkConstants.FN_INTENT_ACTIONS_ACTIVITY);
        mPaths.put(ACTIONS_BROADCAST, mLocation + SdkConstants.OS_PLATFORM_DATA_FOLDER +
                SdkConstants.FN_INTENT_ACTIONS_BROADCAST);
        mPaths.put(ACTIONS_SERVICE, mLocation + SdkConstants.OS_PLATFORM_DATA_FOLDER +
                SdkConstants.FN_INTENT_ACTIONS_SERVICE);
        mPaths.put(CATEGORIES, mLocation + SdkConstants.OS_PLATFORM_DATA_FOLDER +
                SdkConstants.FN_INTENT_CATEGORIES);
        mPaths.put(AAPT, mLocation + SdkConstants.OS_SDK_TOOLS_FOLDER + SdkConstants.FN_AAPT);
        mPaths.put(AIDL, mLocation + SdkConstants.OS_SDK_TOOLS_FOLDER + SdkConstants.FN_AIDL);
        mPaths.put(DX, mLocation + SdkConstants.OS_SDK_TOOLS_FOLDER + SdkConstants.FN_DX);
        mPaths.put(DX_JAR, mLocation + SdkConstants.OS_SDK_TOOLS_LIB_FOLDER +
                SdkConstants.FN_DX_JAR);
        mPaths.put(ANT, mLocation + SdkConstants.OS_PLATFORM_ANT_FOLDER);
    }
    public String getLocation() {
        return mLocation;
    }
    public String getVendor() {
        return PLATFORM_VENDOR;
    }
    public String getName() {
        return mName;
    }
    public String getFullName() {
        return mName;
    }
    public String getClasspathName() {
        return mName;
    }
    public String getDescription() {
        return String.format("Standard Android platform %s", mVersionName);
    }
    public AndroidVersion getVersion() {
        return mVersion;
    }
    public String getVersionName() {
        return mVersionName;
    }
    public int getRevision() {
        return mRevision;
    }
    public boolean isPlatform() {
        return true;
    }
    public IAndroidTarget getParent() {
        return null;
    }
    public String getPath(int pathId) {
        return mPaths.get(pathId);
    }
    public String[] getSkins() {
        return mSkins;
    }
    public String getDefaultSkin() {
        return "HVGA";
    }
    public IOptionalLibrary[] getOptionalLibraries() {
        return null;
    }
    public String[] getPlatformLibraries() {
        return new String[] { SdkConstants.ANDROID_TEST_RUNNER_LIB };
    }
    public int getUsbVendorId() {
        return NO_USB_ID;
    }
    public boolean canRunOn(IAndroidTarget target) {
        if (target == this) {
            return true;
        }
        if (mVersion.getCodename() != null) {
            return mVersion.equals(target.getVersion());
        }
        return target.getVersion().getApiLevel() >= mVersion.getApiLevel();
    }
    public String hashString() {
        return String.format(PLATFORM_HASH, mVersion.getApiString());
    }
    @Override
    public int hashCode() {
        return hashString().hashCode();
    }
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PlatformTarget) {
            PlatformTarget platform = (PlatformTarget)obj;
            return mVersion.equals(platform.getVersion());
        }
        return false;
    }
    public int compareTo(IAndroidTarget target) {
        if (this == target) {
            return 0;
        }
        int versionDiff = mVersion.compareTo(target.getVersion());
        if (versionDiff == 0) {
            if (target.isPlatform() == false) {
                return -1;
            }
        }
        return versionDiff;
    }
    public String getProperty(String name) {
        return mProperties.get(name);
    }
    public Integer getProperty(String name, Integer defaultValue) {
        try {
            String value = getProperty(name);
            if (value != null) {
                return Integer.decode(value);
            }
        } catch (NumberFormatException e) {
        }
        return defaultValue;
    }
    public Boolean getProperty(String name, Boolean defaultValue) {
        String value = getProperty(name);
        if (value != null) {
            return Boolean.valueOf(value);
        }
        return defaultValue;
    }
    public Map<String, String> getProperties() {
        return mProperties; 
    }
    void setSkins(String[] skins) {
        mSkins = skins;
    }
    void setSamplesPath(String osLocation) {
        mPaths.put(SAMPLES, osLocation);
    }
}
