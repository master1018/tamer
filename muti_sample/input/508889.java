public final class RegionQualifier extends ResourceQualifier {
    private final static Pattern sRegionPattern = Pattern.compile("^r([A-Z]{2})$"); 
    public static final String NAME = "Region";
    private String mValue;
    public static RegionQualifier getQualifier(String segment) {
        Matcher m = sRegionPattern.matcher(segment);
        if (m.matches()) {
            RegionQualifier qualifier = new RegionQualifier();
            qualifier.mValue = m.group(1);
            return qualifier;
        }
        return null;
    }
    public static String getFolderSegment(String value) {
        if (value != null) {
            String segment = "r" + value.toUpperCase(); 
            if (sRegionPattern.matcher(segment).matches()) {
                return segment;
            }
        }
        return "";  
    }
    public RegionQualifier() {
    }
    public RegionQualifier(String value) {
        mValue = value;
    }
    public String getValue() {
        if (mValue != null) {
            return mValue;
        }
        return ""; 
    }
    @Override
    public String getName() {
        return NAME;
    }
    @Override
    public String getShortName() {
        return NAME;
    }
    @Override
    public Image getIcon() {
        return IconFactory.getInstance().getIcon("region"); 
    }
    @Override
    public boolean isValid() {
        return mValue != null;
    }
    @Override
    public boolean checkAndSet(String value, FolderConfiguration config) {
        RegionQualifier qualifier = getQualifier(value);
        if (qualifier != null) {
            config.setRegionQualifier(qualifier);
            return true;
        }
        return false;
    }
    @Override
    public boolean equals(Object qualifier) {
        if (qualifier instanceof RegionQualifier) {
            if (mValue == null) {
                return ((RegionQualifier)qualifier).mValue == null;
            }
            return mValue.equals(((RegionQualifier)qualifier).mValue);
        }
        return false;
    }
    @Override
    public int hashCode() {
        if (mValue != null) {
            return mValue.hashCode();
        }
        return 0;
    }
    @Override
    public String getFolderSegment(IAndroidTarget target) {
        return getFolderSegment(mValue);
    }
    @Override
    public String getStringValue() {
        if (mValue != null) {
            return mValue;
        }
        return ""; 
    }
}
