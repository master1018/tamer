public abstract class Package implements IDescription, Comparable<Package> {
    public static final String PROP_REVISION     = "Pkg.Revision";     
    public static final String PROP_LICENSE      = "Pkg.License";      
    public static final String PROP_DESC         = "Pkg.Desc";         
    public static final String PROP_DESC_URL     = "Pkg.DescUrl";      
    public static final String PROP_RELEASE_NOTE = "Pkg.RelNote";      
    public static final String PROP_RELEASE_URL  = "Pkg.RelNoteUrl";   
    public static final String PROP_SOURCE_URL   = "Pkg.SourceUrl";    
    public static final String PROP_USER_SOURCE  = "Pkg.UserSrc";      
    public static final String PROP_OBSOLETE     = "Pkg.Obsolete";     
    private final int mRevision;
    private final String mObsolete;
    private final String mLicense;
    private final String mDescription;
    private final String mDescUrl;
    private final String mReleaseNote;
    private final String mReleaseUrl;
    private final Archive[] mArchives;
    private final RepoSource mSource;
    public static enum UpdateInfo {
        INCOMPATIBLE,
        NOT_UPDATE,
        UPDATE;
    }
    Package(RepoSource source, Node packageNode, Map<String,String> licenses) {
        mSource = source;
        mRevision    = XmlParserUtils.getXmlInt   (packageNode, SdkRepository.NODE_REVISION, 0);
        mDescription = XmlParserUtils.getXmlString(packageNode, SdkRepository.NODE_DESCRIPTION);
        mDescUrl     = XmlParserUtils.getXmlString(packageNode, SdkRepository.NODE_DESC_URL);
        mReleaseNote = XmlParserUtils.getXmlString(packageNode, SdkRepository.NODE_RELEASE_NOTE);
        mReleaseUrl  = XmlParserUtils.getXmlString(packageNode, SdkRepository.NODE_RELEASE_URL);
        mObsolete    = XmlParserUtils.getOptionalXmlString(
                                                   packageNode, SdkRepository.NODE_OBSOLETE);
        mLicense  = parseLicense(packageNode, licenses);
        mArchives = parseArchives(XmlParserUtils.getFirstChild(
                                  packageNode, SdkRepository.NODE_ARCHIVES));
    }
    public Package(
            RepoSource source,
            Properties props,
            int revision,
            String license,
            String description,
            String descUrl,
            Os archiveOs,
            Arch archiveArch,
            String archiveOsPath) {
        if (description == null) {
            description = "";
        }
        if (descUrl == null) {
            descUrl = "";
        }
        mRevision = Integer.parseInt(getProperty(props, PROP_REVISION, Integer.toString(revision)));
        mLicense     = getProperty(props, PROP_LICENSE,      license);
        mDescription = getProperty(props, PROP_DESC,         description);
        mDescUrl     = getProperty(props, PROP_DESC_URL,     descUrl);
        mReleaseNote = getProperty(props, PROP_RELEASE_NOTE, "");
        mReleaseUrl  = getProperty(props, PROP_RELEASE_URL,  "");
        mObsolete    = getProperty(props, PROP_OBSOLETE,     null);
        String srcUrl = getProperty(props, PROP_SOURCE_URL, null);
        if (props != null && source == null && srcUrl != null) {
            boolean isUser = Boolean.parseBoolean(props.getProperty(PROP_USER_SOURCE,
                                                                    Boolean.TRUE.toString()));
            source = new RepoSource(srcUrl, isUser);
        }
        mSource = source;
        mArchives = new Archive[1];
        mArchives[0] = new Archive(this,
                props,
                archiveOs,
                archiveArch,
                archiveOsPath);
    }
    protected String getProperty(Properties props, String propKey, String defaultValue) {
        if (props == null) {
            return defaultValue;
        }
        return props.getProperty(propKey, defaultValue);
    }
    void saveProperties(Properties props) {
        props.setProperty(PROP_REVISION, Integer.toString(mRevision));
        if (mLicense != null && mLicense.length() > 0) {
            props.setProperty(PROP_LICENSE, mLicense);
        }
        if (mDescription != null && mDescription.length() > 0) {
            props.setProperty(PROP_DESC, mDescription);
        }
        if (mDescUrl != null && mDescUrl.length() > 0) {
            props.setProperty(PROP_DESC_URL, mDescUrl);
        }
        if (mReleaseNote != null && mReleaseNote.length() > 0) {
            props.setProperty(PROP_RELEASE_NOTE, mReleaseNote);
        }
        if (mReleaseUrl != null && mReleaseUrl.length() > 0) {
            props.setProperty(PROP_RELEASE_URL, mReleaseUrl);
        }
        if (mObsolete != null) {
            props.setProperty(PROP_OBSOLETE, mObsolete);
        }
        if (mSource != null) {
            props.setProperty(PROP_SOURCE_URL,  mSource.getUrl());
            props.setProperty(PROP_USER_SOURCE, Boolean.toString(mSource.isUserSource()));
        }
    }
    private String parseLicense(Node packageNode, Map<String, String> licenses) {
        Node usesLicense = XmlParserUtils.getFirstChild(
                                            packageNode, SdkRepository.NODE_USES_LICENSE);
        if (usesLicense != null) {
            Node ref = usesLicense.getAttributes().getNamedItem(SdkRepository.ATTR_REF);
            if (ref != null) {
                String licenseRef = ref.getNodeValue();
                return licenses.get(licenseRef);
            }
        }
        return null;
    }
    private Archive[] parseArchives(Node archivesNode) {
        ArrayList<Archive> archives = new ArrayList<Archive>();
        if (archivesNode != null) {
            String nsUri = archivesNode.getNamespaceURI();
            for(Node child = archivesNode.getFirstChild();
                child != null;
                child = child.getNextSibling()) {
                if (child.getNodeType() == Node.ELEMENT_NODE &&
                        nsUri.equals(child.getNamespaceURI()) &&
                        SdkRepository.NODE_ARCHIVE.equals(child.getLocalName())) {
                    archives.add(parseArchive(child));
                }
            }
        }
        return archives.toArray(new Archive[archives.size()]);
    }
    private Archive parseArchive(Node archiveNode) {
        Archive a = new Archive(
                    this,
                    (Os)   XmlParserUtils.getEnumAttribute(archiveNode, SdkRepository.ATTR_OS,
                            Os.values(), null),
                    (Arch) XmlParserUtils.getEnumAttribute(archiveNode, SdkRepository.ATTR_ARCH,
                            Arch.values(), Arch.ANY),
                    XmlParserUtils.getXmlString(archiveNode, SdkRepository.NODE_URL),
                    XmlParserUtils.getXmlLong  (archiveNode, SdkRepository.NODE_SIZE, 0),
                    XmlParserUtils.getXmlString(archiveNode, SdkRepository.NODE_CHECKSUM)
                );
        return a;
    }
    public RepoSource getParentSource() {
        return mSource;
    }
    public boolean isObsolete() {
        return mObsolete != null;
    }
    public int getRevision() {
        return mRevision;
    }
    public String getLicense() {
        return mLicense;
    }
    public String getDescription() {
        return mDescription;
    }
    public String getDescUrl() {
        return mDescUrl;
    }
    public String getReleaseNote() {
        return mReleaseNote;
    }
    public String getReleaseNoteUrl() {
        return mReleaseUrl;
    }
    public Archive[] getArchives() {
        return mArchives;
    }
    public boolean hasCompatibleArchive() {
        for (Archive archive : mArchives) {
            if (archive.isCompatible()) {
                return true;
            }
        }
        return false;
    }
    public abstract String getShortDescription();
    public String getLongDescription() {
        StringBuilder sb = new StringBuilder();
        String s = getDescription();
        if (s != null) {
            sb.append(s);
        }
        if (sb.length() > 0) {
            sb.append("\n");
        }
        sb.append(String.format("Revision %1$d%2$s",
                getRevision(),
                isObsolete() ? " (Obsolete)" : ""));
        s = getDescUrl();
        if (s != null && s.length() > 0) {
            sb.append(String.format("\n\nMore information at %1$s", s));
        }
        s = getReleaseNote();
        if (s != null && s.length() > 0) {
            sb.append("\n\nRelease note:\n").append(s);
        }
        s = getReleaseNoteUrl();
        if (s != null && s.length() > 0) {
            sb.append("\nRelease note URL: ").append(s);
        }
        return sb.toString();
    }
    public abstract File getInstallFolder(
            String osSdkRoot, String suggestedDir, SdkManager sdkManager);
    public boolean preInstallHook(Archive archive, ITaskMonitor monitor,
            String osSdkRoot, File installFolder) {
        return true;
    }
    public void postInstallHook(Archive archive, ITaskMonitor monitor, File installFolder) {
    }
    public abstract boolean sameItemAs(Package pkg);
    public UpdateInfo canBeUpdatedBy(Package replacementPackage) {
        if (replacementPackage == null) {
            return UpdateInfo.INCOMPATIBLE;
        }
        if (sameItemAs(replacementPackage) == false) {
            return UpdateInfo.INCOMPATIBLE;
        }
        if (replacementPackage.getRevision() > this.getRevision()) {
            return UpdateInfo.UPDATE;
        }
        return UpdateInfo.NOT_UPDATE;
    }
    public int compareTo(Package other) {
        int s1 = this.sortingScore();
        int s2 = other.sortingScore();
        return s1 - s2;
    }
    private int sortingScore() {
        int type = 0;
        int rev = getRevision();
        int offset = 0;
        if (this instanceof ToolPackage) {
            type = 3;
        } else if (this instanceof DocPackage) {
            type = 2;
        } else if (this instanceof PlatformPackage || this instanceof AddonPackage ||
                this instanceof SamplePackage) {
            type = 1;
            AndroidVersion v = ((IPackageVersion) this).getVersion();
            offset = v.getApiLevel();
            offset = offset * 2 + (v.isPreview() ? 1 : 0);
            offset = offset * 2 + ((this instanceof AddonPackage) ? 0 :
                    ((this instanceof SamplePackage) ? 1 : 2));
        } else {
            type = 0;
        }
        int n = (type << 28) + (offset << 12) + rev;
        return 0 - n;
    }
}
