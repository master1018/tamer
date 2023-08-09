public final class NetworkCodeQualifier extends ResourceQualifier {
    private final static int DEFAULT_CODE = -1;
    private final static Pattern sNetworkCodePattern = Pattern.compile("^mnc(\\d{1,3})$"); 
    private final int mCode;
    public final static String NAME = "Mobile Network Code";
    public static NetworkCodeQualifier getQualifier(String segment) {
        Matcher m = sNetworkCodePattern.matcher(segment);
        if (m.matches()) {
            String v = m.group(1);
            int code = -1;
            try {
                code = Integer.parseInt(v);
            } catch (NumberFormatException e) {
                return null;
            }
            NetworkCodeQualifier qualifier = new NetworkCodeQualifier(code);
            return qualifier;
        }
        return null;
    }
    public static String getFolderSegment(int code) {
        if (code != DEFAULT_CODE && code >= 1 && code <= 999) { 
            return String.format("mnc%1$d", code); 
        }
        return ""; 
    }
    public NetworkCodeQualifier() {
        this(DEFAULT_CODE);
    }
    public NetworkCodeQualifier(int code) {
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
        return "Network Code";
    }
    @Override
    public Image getIcon() {
        return IconFactory.getInstance().getIcon("mnc"); 
    }
    @Override
    public boolean isValid() {
        return mCode != DEFAULT_CODE;
    }
    @Override
    public boolean checkAndSet(String value, FolderConfiguration config) {
        Matcher m = sNetworkCodePattern.matcher(value);
        if (m.matches()) {
            String v = m.group(1);
            int code = -1;
            try {
                code = Integer.parseInt(v);
            } catch (NumberFormatException e) {
                return false;
            }
            NetworkCodeQualifier qualifier = new NetworkCodeQualifier(code);
            config.setNetworkCodeQualifier(qualifier);
            return true;
        }
        return false;
    }
    @Override
    public boolean equals(Object qualifier) {
        if (qualifier instanceof NetworkCodeQualifier) {
            return mCode == ((NetworkCodeQualifier)qualifier).mCode;
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
            return String.format("MNC %1$d", mCode);
        }
        return ""; 
    }
}
