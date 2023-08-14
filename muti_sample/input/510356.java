final class AddOnTarget implements IAndroidTarget {
    private final static String ADD_ON_FORMAT = "%s:%s:%s"; 
    private final static class OptionalLibrary implements IOptionalLibrary {
        private final String mJarName;
        private final String mJarPath;
        private final String mName;
        private final String mDescription;
        OptionalLibrary(String jarName, String jarPath, String name, String description) {
            mJarName = jarName;
            mJarPath = jarPath;
            mName = name;
            mDescription = description;
        }
        public String getJarName() {
            return mJarName;
        }
        public String getJarPath() {
            return mJarPath;
        }
        public String getName() {
            return mName;
        }
        public String getDescription() {
            return mDescription;
        }
    }
    private final String mLocation;
    private final PlatformTarget mBasePlatform;
    private final String mName;
    private final String mVendor;
    private final int mRevision;
    private final String mDescription;
    private String[] mSkins;
    private String mDefaultSkin;
    private IOptionalLibrary[] mLibraries;
    private int mVendorId = NO_USB_ID;
    AddOnTarget(String location, String name, String vendor, int revision, String description,
            Map<String, String[]> libMap, PlatformTarget basePlatform) {
        if (location.endsWith(File.separator) == false) {
            location = location + File.separator;
        }
        mLocation = location;
        mName = name;
        mVendor = vendor;
        mRevision = revision;
        mDescription = description;
        mBasePlatform = basePlatform;
        if (libMap != null) {
            mLibraries = new IOptionalLibrary[libMap.size()];
            int index = 0;
            for (Entry<String, String[]> entry : libMap.entrySet()) {
                String jarFile = entry.getValue()[0];
                String desc = entry.getValue()[1];
                mLibraries[index++] = new OptionalLibrary(jarFile,
                        mLocation + SdkConstants.OS_ADDON_LIBS_FOLDER + jarFile,
                        entry.getKey(), desc);
            }
        }
    }
    public String getLocation() {
        return mLocation;
    }
    public String getName() {
        return mName;
    }
    public String getVendor() {
        return mVendor;
    }
    public String getFullName() {
        return String.format("%1$s (%2$s)", mName, mVendor);
    }
    public String getClasspathName() {
        return String.format("%1$s [%2$s]", mName, mBasePlatform.getName());
    }
    public String getDescription() {
        return mDescription;
    }
    public AndroidVersion getVersion() {
        return mBasePlatform.getVersion();
    }
    public String getVersionName() {
        return mBasePlatform.getVersionName();
    }
    public int getRevision() {
        return mRevision;
    }
    public boolean isPlatform() {
        return false;
    }
    public IAndroidTarget getParent() {
        return mBasePlatform;
    }
    public String getPath(int pathId) {
        switch (pathId) {
            case IMAGES:
                return mLocation + SdkConstants.OS_IMAGES_FOLDER;
            case SKINS:
                return mLocation + SdkConstants.OS_SKINS_FOLDER;
            case DOCS:
                return mLocation + SdkConstants.FD_DOCS + File.separator
                        + SdkConstants.FD_DOCS_REFERENCE;
            case SAMPLES:
                File sampleLoc = new File(mLocation, SdkConstants.FD_SAMPLES);
                if (sampleLoc.isDirectory()) {
                    File[] files = sampleLoc.listFiles(new FileFilter() {
                        public boolean accept(File pathname) {
                            return pathname.isDirectory();
                        }
                    });
                    if (files != null && files.length > 0) {
                        return sampleLoc.getAbsolutePath();
                    }
                }
            default :
                return mBasePlatform.getPath(pathId);
        }
    }
    public String[] getSkins() {
        return mSkins;
    }
    public String getDefaultSkin() {
        return mDefaultSkin;
    }
    public IOptionalLibrary[] getOptionalLibraries() {
        return mLibraries;
    }
    public String[] getPlatformLibraries() {
        return mBasePlatform.getPlatformLibraries();
    }
    public String getProperty(String name) {
        return mBasePlatform.getProperty(name);
    }
    public Integer getProperty(String name, Integer defaultValue) {
        return mBasePlatform.getProperty(name, defaultValue);
    }
    public Boolean getProperty(String name, Boolean defaultValue) {
        return mBasePlatform.getProperty(name, defaultValue);
    }
    public Map<String, String> getProperties() {
        return mBasePlatform.getProperties();
    }
    public int getUsbVendorId() {
        return mVendorId;
    }
    public boolean canRunOn(IAndroidTarget target) {
        if (target == this) {
            return true;
        }
        if (mLibraries == null || mLibraries.length == 0) {
            return mBasePlatform.canRunOn(target);
        } else {
            if (mVendor.equals(target.getVendor()) == false ||
                            mName.equals(target.getName()) == false) {
                return false;
            }
            return mBasePlatform.canRunOn(target);
        }
    }
    public String hashString() {
        return String.format(ADD_ON_FORMAT, mVendor, mName,
                mBasePlatform.getVersion().getApiString());
    }
    @Override
    public int hashCode() {
        return hashString().hashCode();
    }
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AddOnTarget) {
            AddOnTarget addon = (AddOnTarget)obj;
            return mVendor.equals(addon.mVendor) && mName.equals(addon.mName) &&
                mBasePlatform.getVersion().equals(addon.mBasePlatform.getVersion());
        }
        return false;
    }
    public int compareTo(IAndroidTarget target) {
        if (this == target) {
            return 0;
        }
        int versionDiff = getVersion().compareTo(target.getVersion());
        if (versionDiff == 0) {
            if (target.isPlatform()) {
                return +1;
            } else {
                AddOnTarget targetAddOn = (AddOnTarget)target;
                int vendorDiff = mVendor.compareTo(targetAddOn.mVendor);
                if (vendorDiff == 0) {
                    return mName.compareTo(targetAddOn.mName);
                } else {
                    return vendorDiff;
                }
            }
        }
        return versionDiff;
    }
    void setSkins(String[] skins, String defaultSkin) {
        mDefaultSkin = defaultSkin;
        HashSet<String> skinSet = new HashSet<String>();
        skinSet.addAll(Arrays.asList(skins));
        skinSet.addAll(Arrays.asList(mBasePlatform.getSkins()));
        mSkins = skinSet.toArray(new String[skinSet.size()]);
    }
    void setUsbVendorId(int vendorId) {
        if (vendorId == 0) {
            throw new IllegalArgumentException( "VendorId must be > 0");
        }
        mVendorId = vendorId;
    }
}
