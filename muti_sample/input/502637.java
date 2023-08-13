public class DocPackage extends Package implements IPackageVersion {
    private final AndroidVersion mVersion;
    DocPackage(RepoSource source, Node packageNode, Map<String,String> licenses) {
        super(source, packageNode, licenses);
        int apiLevel = XmlParserUtils.getXmlInt   (packageNode, SdkRepository.NODE_API_LEVEL, 0);
        String codeName = XmlParserUtils.getXmlString(packageNode, SdkRepository.NODE_CODENAME);
        if (codeName.length() == 0) {
            codeName = null;
        }
        mVersion = new AndroidVersion(apiLevel, codeName);
    }
    DocPackage(RepoSource source,
            Properties props,
            int apiLevel,
            String codename,
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
        mVersion = new AndroidVersion(props, apiLevel, codename);
    }
    @Override
    void saveProperties(Properties props) {
        super.saveProperties(props);
        mVersion.saveProperties(props);
    }
    public AndroidVersion getVersion() {
        return mVersion;
    }
    @Override
    public String getShortDescription() {
        if (mVersion.isPreview()) {
            return String.format("Documentation for Android '%1$s' Preview SDK, revision %2$s%3$s",
                    mVersion.getCodename(),
                    getRevision(),
                    isObsolete() ? " (Obsolete)" : "");
        } else {
            return String.format("Documentation for Android SDK, API %1$d, revision %2$s%3$s",
                    mVersion.getApiLevel(),
                    getRevision(),
                    isObsolete() ? " (Obsolete)" : "");
        }
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
        return new File(osSdkRoot, SdkConstants.FD_DOCS);
    }
    @Override
    public boolean sameItemAs(Package pkg) {
        return pkg instanceof DocPackage;
    }
    @Override
    public UpdateInfo canBeUpdatedBy(Package replacementPackage) {
        if (replacementPackage == null) {
            return UpdateInfo.INCOMPATIBLE;
        }
        if (sameItemAs(replacementPackage) == false) {
            return UpdateInfo.INCOMPATIBLE;
        }
        DocPackage replacementDoc = (DocPackage)replacementPackage;
        AndroidVersion replacementVersion = replacementDoc.getVersion();
        if (replacementVersion.getApiLevel() > mVersion.getApiLevel()) {
            return UpdateInfo.UPDATE;
        }
        if (replacementVersion.equals(mVersion)) {
            if (replacementPackage.getRevision() > this.getRevision()) {
                return UpdateInfo.UPDATE;
            }
        } else {
            if (replacementVersion.getApiLevel() == mVersion.getApiLevel() &&
                    replacementVersion.isPreview()) {
                return UpdateInfo.UPDATE;
            }
        }
        return UpdateInfo.NOT_UPDATE;
    }
}
