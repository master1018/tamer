public final class LanguageQualifier extends ResourceQualifier {
    private final static Pattern sLanguagePattern = Pattern.compile("^[a-z]{2}$"); 
    public static final String NAME = "Language";
    private String mValue;
    public static LanguageQualifier getQualifier(String segment) {
        if (sLanguagePattern.matcher(segment).matches()) {
            LanguageQualifier qualifier = new LanguageQualifier();
            qualifier.mValue = segment;
            return qualifier;
        }
        return null;
    }
    public static String getFolderSegment(String value) {
        String segment = value.toLowerCase();
        if (sLanguagePattern.matcher(segment).matches()) {
            return segment;
        }
        return null;
    }
    public LanguageQualifier() {
    }
    public LanguageQualifier(String value) {
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
        return IconFactory.getInstance().getIcon("language"); 
    }
    @Override
    public boolean isValid() {
        return mValue != null;
    }
    @Override
    public boolean checkAndSet(String value, FolderConfiguration config) {
        LanguageQualifier qualifier = getQualifier(value);
        if (qualifier != null) {
            config.setLanguageQualifier(qualifier);
            return true;
        }
        return false;
    }
    @Override
    public boolean equals(Object qualifier) {
        if (qualifier instanceof LanguageQualifier) {
            if (mValue == null) {
                return ((LanguageQualifier)qualifier).mValue == null;
            }
            return mValue.equals(((LanguageQualifier)qualifier).mValue);
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
        if (mValue != null) {
            return getFolderSegment(mValue);
        }
        return ""; 
    }
    @Override
    public String getStringValue() {
        if (mValue != null) {
            return mValue;
        }
        return ""; 
    }
}
