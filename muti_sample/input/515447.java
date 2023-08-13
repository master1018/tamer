public class PlatformPackage extends MinToolsPackage implements IPackageVersion {
    public static final String PROP_VERSION       = "Platform.Version";      
    private final AndroidVersion mVersion;
    private final String mVersionName;
    PlatformPackage(RepoSource source, Node packageNode, Map<String,String> licenses) {
        super(source, packageNode, licenses);
        mVersionName = XmlParserUtils.getXmlString(packageNode, SdkRepository.NODE_VERSION);
        int apiLevel = XmlParserUtils.getXmlInt   (packageNode, SdkRepository.NODE_API_LEVEL, 0);
        String codeName = XmlParserUtils.getXmlString(packageNode, SdkRepository.NODE_CODENAME);
        if (codeName.length() == 0) {
            codeName = null;
        }
        mVersion = new AndroidVersion(apiLevel, codeName);
    }
    PlatformPackage(IAndroidTarget target, Properties props) {
        super(  null,                       
                props,                      
                target.getRevision(),       
                null,                       
                target.getDescription(),    
                null,                       
                Os.getCurrentOs(),          
                Arch.getCurrentArch(),      
                target.getLocation()        
                );
        mVersion = target.getVersion();
        mVersionName  = target.getVersionName();
    }
    @Override
    void saveProperties(Properties props) {
        super.saveProperties(props);
        mVersion.saveProperties(props);
        if (mVersionName != null) {
            props.setProperty(PROP_VERSION, mVersionName);
        }
    }
    public String getVersionName() {
        return mVersionName;
    }
    public AndroidVersion getVersion() {
        return mVersion;
    }
    @Override
    public String getShortDescription() {
        String s;
        if (mVersion.isPreview()) {
            s = String.format("SDK Platform Android %1$s Preview, revision %2$s%3$s",
                    getVersionName(),
                    getRevision(),
                    isObsolete() ? " (Obsolete)" : "");
        } else {
            s = String.format("SDK Platform Android %1$s, API %2$d, revision %3$s%4$s",
                getVersionName(),
                mVersion.getApiLevel(),
                getRevision(),
                isObsolete() ? " (Obsolete)" : "");
        }
        return s;
    }
    @Override
    public String getLongDescription() {
        String s = getDescription();
        if (s == null || s.length() == 0) {
            s = getShortDescription();
        }
        if (s.indexOf("revision") == -1) {
            s += String.format("\nRevision %1$d%2$s",
                    getRevision(),
                    isObsolete() ? " (Obsolete)" : "");
        }
        return s;
    }
    @Override
    public File getInstallFolder(String osSdkRoot, String suggestedDir, SdkManager sdkManager) {
        for (IAndroidTarget target : sdkManager.getTargets()) {
            if (target.isPlatform() &&
                    target.getVersion().equals(mVersion) &&
                    target.getVersionName().equals(getVersionName())) {
                return new File(target.getLocation());
            }
        }
        File platforms = new File(osSdkRoot, SdkConstants.FD_PLATFORMS);
        File folder = new File(platforms,
                String.format("android-%s", getVersion().getApiString())); 
        return folder;
    }
    @Override
    public boolean preInstallHook(Archive archive,
            ITaskMonitor monitor,
            String osSdkRoot,
            File installFolder) {
        File platformsRoot = new File(osSdkRoot, SdkConstants.FD_PLATFORMS);
        if (!platformsRoot.isDirectory()) {
            platformsRoot.mkdir();
        }
        return super.preInstallHook(archive, monitor, osSdkRoot, installFolder);
    }
    @Override
    public boolean sameItemAs(Package pkg) {
        if (pkg instanceof PlatformPackage) {
            PlatformPackage newPkg = (PlatformPackage)pkg;
            return newPkg.getVersion().equals(this.getVersion());
        }
        return false;
    }
}
