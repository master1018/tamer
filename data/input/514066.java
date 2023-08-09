public final class CountryCodeQualifier extends ResourceQualifier {
    private final static int DEFAULT_CODE = -1;
    private final static Pattern sCountryCodePattern = Pattern.compile("^mcc(\\d{3})$");
    private final int mCode;
    public static final String NAME = "Mobile Country Code";
    public static CountryCodeQualifier getQualifier(String segment) {
        Matcher m = sCountryCodePattern.matcher(segment);
        if (m.matches()) {
            String v = m.group(1);
            int code = -1;
            try {
                code = Integer.parseInt(v);
            } catch (NumberFormatException e) {
                return null;
            }
            CountryCodeQualifier qualifier = new CountryCodeQualifier(code);
            return qualifier;
        }
        return null;
    }
    public static String getFolderSegment(int code) {
        if (code != DEFAULT_CODE && code >= 100 && code <=999) { 
            return String.format("mcc%1$d", code); 
        }
        return ""; 
    }
    public CountryCodeQualifier() {
        this(DEFAULT_CODE);
    }
    public CountryCodeQualifier(int code) {
        mCode = code;
    }
    public int getCode() {
        return mCode;
    }
    @Override
    public String getName() {
        return NAME;
    }
    @Override
    public String getShortName() {
        return "Country Code";
    }
    @Override
    public Image getIcon() {
        return IconFactory.getInstance().getIcon("mcc"); 
    }
    @Override
    public boolean isValid() {
        return mCode != DEFAULT_CODE;
    }
    @Override
    public boolean checkAndSet(String value, FolderConfiguration config) {
        CountryCodeQualifier qualifier = getQualifier(value);
        if (qualifier != null) {
            config.setCountryCodeQualifier(qualifier);
            return true;
        }
        return false;
    }
    @Override
    public boolean equals(Object qualifier) {
        if (qualifier instanceof CountryCodeQualifier) {
            return mCode == ((CountryCodeQualifier)qualifier).mCode;
        }
        return false;
    }
    @Override
    public int hashCode() {
        return mCode;
    }
    @Override
    public String getFolderSegment(IAndroidTarget target) {
        return getFolderSegment(mCode);
    }
    @Override
    public String getStringValue() {
        if (mCode != DEFAULT_CODE) {
            return String.format("MCC %1$d", mCode);
        }
        return ""; 
    }
}
