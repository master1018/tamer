public class ExtraPackage extends MinToolsPackage
    implements IMinApiLevelDependency {
    private static final String PROP_PATH          = "Extra.Path";         
    private static final String PROP_MIN_API_LEVEL = "Extra.MinApiLevel";  
    private final String mPath;
    private final int mMinApiLevel;
    ExtraPackage(RepoSource source, Node packageNode, Map<String,String> licenses) {
        super(source, packageNode, licenses);
        mPath = XmlParserUtils.getXmlString(packageNode, SdkRepository.NODE_PATH);
        mMinApiLevel = XmlParserUtils.getXmlInt(packageNode, SdkRepository.NODE_MIN_API_LEVEL,
                MIN_API_LEVEL_NOT_SPECIFIED);
    }
    ExtraPackage(RepoSource source,
            Properties props,
            String path,
            int revision,
            String license,
            String description,
            String descUrl,
            Os archiveOs,
            Arch archiveArch,
            String archiveOsPath) {
        super(source,
                props,
                revision,
                license,
                description,
                descUrl,
                archiveOs,
                archiveArch,
                archiveOsPath);
        mPath = path != null ? path : getProperty(props, PROP_PATH, path);
        mMinApiLevel = Integer.parseInt(
            getProperty(props, PROP_MIN_API_LEVEL, Integer.toString(MIN_API_LEVEL_NOT_SPECIFIED)));
    }
    @Override
    void saveProperties(Properties props) {
        super.saveProperties(props);
        props.setProperty(PROP_PATH, mPath);
        if (getMinApiLevel() != MIN_API_LEVEL_NOT_SPECIFIED) {
            props.setProperty(PROP_MIN_API_LEVEL, Integer.toString(getMinApiLevel()));
        }
    }
    public int getMinApiLevel() {
        return mMinApiLevel;
    }
    public boolean isPathValid() {
        if (SdkConstants.FD_ADDONS.equals(mPath) ||
                SdkConstants.FD_PLATFORMS.equals(mPath) ||
                SdkConstants.FD_TOOLS.equals(mPath) ||
                SdkConstants.FD_DOCS.equals(mPath)) {
            return false;
        }
        return mPath != null && mPath.indexOf('/') == -1 && mPath.indexOf('\\') == -1;
    }
    public String getPath() {
        return mPath;
    }
    @Override
    public String getShortDescription() {
        String name = getPath();
        if (name != null) {
            name = name.replaceAll("[ _\t\f-]+", " ");     
            boolean changed = false;
            char[] chars = name.toCharArray();
            for (int n = chars.length - 1, i = 0; i < n; i++) {
                if (Character.isLowerCase(chars[i]) && (i == 0 || chars[i - 1] == ' ')) {
                    chars[i] = Character.toUpperCase(chars[i]);
                    changed = true;
                }
            }
            if (changed) {
                name = new String(chars);
            }
        }
        String s = String.format("%1$s package, revision %2$d%3$s",
                name,
                getRevision(),
                isObsolete() ? " (Obsolete)" : "");
        return s;
    }
    @Override
    public String getLongDescription() {
        String s = getDescription();
        if (s == null || s.length() == 0) {
            s = String.format("Extra %1$s package", getPath());
        }
        if (s.indexOf("revision") == -1) {
            s += String.format("\nRevision %1$d%2$s",
                    getRevision(),
                    isObsolete() ? " (Obsolete)" : "");
        }
        if (getMinToolsRevision() != MIN_TOOLS_REV_NOT_SPECIFIED) {
            s += String.format("\nRequires tools revision %1$d", getMinToolsRevision());
        }
        if (getMinApiLevel() != MIN_API_LEVEL_NOT_SPECIFIED) {
            s += String.format("\nRequires SDK Platform Android API %1$s", getMinApiLevel());
        }
        return s;
    }
    @Override
    public File getInstallFolder(String osSdkRoot, String suggestedDir, SdkManager sdkManager) {
        return new File(osSdkRoot, getPath());
    }
    @Override
    public boolean sameItemAs(Package pkg) {
        return pkg instanceof ExtraPackage && ((ExtraPackage)pkg).mPath.equals(mPath);
    }
}
