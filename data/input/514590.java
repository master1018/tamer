public abstract class MinToolsPackage extends Package
    implements IMinToolsDependency {
    protected static final String PROP_MIN_TOOLS_REV = "Platform.MinToolsRev";  
    private final int mMinToolsRevision;
    MinToolsPackage(RepoSource source, Node packageNode, Map<String,String> licenses) {
        super(source, packageNode, licenses);
        mMinToolsRevision = XmlParserUtils.getXmlInt(packageNode, SdkRepository.NODE_MIN_TOOLS_REV,
                MIN_TOOLS_REV_NOT_SPECIFIED);
    }
    public MinToolsPackage(
            RepoSource source,
            Properties props,
            int revision,
            String license,
            String description,
            String descUrl,
            Os archiveOs,
            Arch archiveArch,
            String archiveOsPath) {
        super(source, props, revision, license, description, descUrl,
                archiveOs, archiveArch, archiveOsPath);
        mMinToolsRevision = Integer.parseInt(
            getProperty(props, PROP_MIN_TOOLS_REV, Integer.toString(MIN_TOOLS_REV_NOT_SPECIFIED)));
    }
    public int getMinToolsRevision() {
        return mMinToolsRevision;
    }
    @Override
    void saveProperties(Properties props) {
        super.saveProperties(props);
        if (getMinToolsRevision() != MIN_TOOLS_REV_NOT_SPECIFIED) {
            props.setProperty(PROP_MIN_TOOLS_REV, Integer.toString(getMinToolsRevision()));
        }
    }
}
