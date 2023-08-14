public class AddonPackage extends Package
    implements IPackageVersion, IPlatformDependency {
    private static final String PROP_NAME      = "Addon.Name";      
    private static final String PROP_VENDOR    = "Addon.Vendor";    
    private final String mVendor;
    private final String mName;
    private final AndroidVersion mVersion;
    public static class Lib {
        private final String mName;
        private final String mDescription;
        public Lib(String name, String description) {
            mName = name;
            mDescription = description;
        }
        public String getName() {
            return mName;
        }
        public String getDescription() {
            return mDescription;
        }
    }
    private final Lib[] mLibs;
    AddonPackage(RepoSource source, Node packageNode, Map<String,String> licenses) {
        super(source, packageNode, licenses);
        mVendor   = XmlParserUtils.getXmlString(packageNode, SdkRepository.NODE_VENDOR);
        mName     = XmlParserUtils.getXmlString(packageNode, SdkRepository.NODE_NAME);
        int apiLevel = XmlParserUtils.getXmlInt   (packageNode, SdkRepository.NODE_API_LEVEL, 0);
        String codeName = XmlParserUtils.getXmlString(packageNode, SdkRepository.NODE_CODENAME);
        if (codeName.length() == 0) {
            codeName = null;
        }
        mVersion = new AndroidVersion(apiLevel, codeName);
        mLibs = parseLibs(XmlParserUtils.getFirstChild(packageNode, SdkRepository.NODE_LIBS));
    }
    AddonPackage(IAndroidTarget target, Properties props) {
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
        mName     = target.getName();
        mVendor   = target.getVendor();
        IOptionalLibrary[] optLibs = target.getOptionalLibraries();
        if (optLibs == null || optLibs.length == 0) {
            mLibs = new Lib[0];
        } else {
            mLibs = new Lib[optLibs.length];
            for (int i = 0; i < optLibs.length; i++) {
                mLibs[i] = new Lib(optLibs[i].getName(), optLibs[i].getDescription());
            }
        }
    }
    @Override
    void saveProperties(Properties props) {
        super.saveProperties(props);
        mVersion.saveProperties(props);
        if (mName != null) {
            props.setProperty(PROP_NAME, mName);
        }
        if (mVendor != null) {
            props.setProperty(PROP_VENDOR, mVendor);
        }
    }
    private Lib[] parseLibs(Node libsNode) {
        ArrayList<Lib> libs = new ArrayList<Lib>();
        if (libsNode != null) {
            String nsUri = libsNode.getNamespaceURI();
            for(Node child = libsNode.getFirstChild();
                child != null;
                child = child.getNextSibling()) {
                if (child.getNodeType() == Node.ELEMENT_NODE &&
                        nsUri.equals(child.getNamespaceURI()) &&
                        SdkRepository.NODE_LIB.equals(child.getLocalName())) {
                    libs.add(parseLib(child));
                }
            }
        }
        return libs.toArray(new Lib[libs.size()]);
    }
    private Lib parseLib(Node libNode) {
        return new Lib(XmlParserUtils.getXmlString(libNode, SdkRepository.NODE_NAME),
                       XmlParserUtils.getXmlString(libNode, SdkRepository.NODE_DESCRIPTION));
    }
    public String getVendor() {
        return mVendor;
    }
    public String getName() {
        return mName;
    }
    public AndroidVersion getVersion() {
        return mVersion;
    }
    public Lib[] getLibs() {
        return mLibs;
    }
    @Override
    public String getShortDescription() {
        return String.format("%1$s by %2$s, Android API %3$s, revision %4$s%5$s",
                getName(),
                getVendor(),
                mVersion.getApiString(),
                getRevision(),
                isObsolete() ? " (Obsolete)" : "");
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
        s += String.format("\nRequires SDK Platform Android API %1$s",
                mVersion.getApiString());
        return s;
    }
    @Override
    public File getInstallFolder(String osSdkRoot, String suggestedDir, SdkManager sdkManager) {
        File addons = new File(osSdkRoot, SdkConstants.FD_ADDONS);
        for (IAndroidTarget target : sdkManager.getTargets()) {
            if (!target.isPlatform() &&
                    target.getVersion().equals(mVersion) &&
                    target.getName().equals(getName()) &&
                    target.getVendor().equals(getVendor())) {
                return new File(target.getLocation());
            }
        }
        String name = String.format("addon_%s_%s_%s",     
                                    getName(), getVendor(), mVersion.getApiString());
        name = name.toLowerCase();
        name = name.replaceAll("[^a-z0-9_-]+", "_");      
        name = name.replaceAll("_+", "_");                
        for (int i = 0; i < 100; i++) {
            String name2 = i == 0 ? name : String.format("%s-%d", name, i); 
            File folder = new File(addons, name2);
            if (!folder.exists()) {
                return folder;
            }
        }
        return null;
    }
    @Override
    public boolean preInstallHook(Archive archive,
            ITaskMonitor monitor,
            String osSdkRoot,
            File installFolder) {
        File addonsRoot = new File(osSdkRoot, SdkConstants.FD_ADDONS);
        if (!addonsRoot.isDirectory()) {
            addonsRoot.mkdir();
        }
        return super.preInstallHook(archive, monitor, osSdkRoot, installFolder);
    }
    @Override
    public boolean sameItemAs(Package pkg) {
        if (pkg instanceof AddonPackage) {
            AddonPackage newPkg = (AddonPackage)pkg;
            return getName().equals(newPkg.getName()) &&
                    getVendor().equals(newPkg.getVendor()) &&
                    getVersion().equals(newPkg.getVersion());
        }
        return false;
    }
}
