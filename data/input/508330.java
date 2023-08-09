public final class VersionQualifier extends ResourceQualifier {
    private final static int DEFAULT_VERSION = -1;
    private final static Pattern sCountryCodePattern = Pattern.compile("^v(\\d+)$");
    private int mVersion = DEFAULT_VERSION;
    public static final String NAME = "Platform Version";
    public static VersionQualifier getQualifier(String segment) {
        Matcher m = sCountryCodePattern.matcher(segment);
        if (m.matches()) {
            String v = m.group(1);
            int code = -1;
            try {
                code = Integer.parseInt(v);
            } catch (NumberFormatException e) {
                return null;
            }
            VersionQualifier qualifier = new VersionQualifier();
            qualifier.mVersion = code;
            return qualifier;
        }
        return null;
    }
    public static String getFolderSegment(int version) {
        if (version != DEFAULT_VERSION) {
            return String.format("v%1$d", version); 
        }
        return ""; 
    }
    public int getVersion() {
        return mVersion;
    }
    @Override
    public String getName() {
        return NAME;
    }
    @Override
    public String getShortName() {
        return "Version";
    }
    @Override
    public Image getIcon() {
        return IconFactory.getInstance().getIcon("version"); 
    }
    @Override
    public boolean isValid() {
        return mVersion != DEFAULT_VERSION;
    }
    @Override
    public boolean checkAndSet(String value, FolderConfiguration config) {
        VersionQualifier qualifier = getQualifier(value);
        if (qualifier != null) {
            config.setVersionQualifier(qualifier);
            return true;
        }
        return false;
    }
    @Override
    public boolean equals(Object qualifier) {
        if (qualifier instanceof VersionQualifier) {
            return mVersion == ((VersionQualifier)qualifier).mVersion;
        }
        return false;
    }
    @Override
    public int hashCode() {
        return mVersion;
    }
    @Override
    public String getFolderSegment(IAndroidTarget target) {
        if (target == null) {
            return getFolderSegment(mVersion);
        }
        AndroidVersion version = target.getVersion();
        if (version.getApiLevel() >= 3) {
            return getFolderSegment(mVersion);
        }
        return ""; 
    }
    @Override
    public String getStringValue() {
        if (mVersion != DEFAULT_VERSION) {
            return String.format("API %1$d", mVersion);
        }
        return ""; 
    }
}
