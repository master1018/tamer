public class UrlQuerySanitizer {
    public class ParameterValuePair {
        public ParameterValuePair(String parameter,
                String value) {
            mParameter = parameter;
            mValue = value;
        }
        public String mParameter;
        public String mValue;
    }
    final private HashMap<String, ValueSanitizer> mSanitizers =
        new HashMap<String, ValueSanitizer>();
    final private HashMap<String, String> mEntries =
        new HashMap<String, String>();
    final private ArrayList<ParameterValuePair> mEntriesList =
        new ArrayList<ParameterValuePair>();
    private boolean mAllowUnregisteredParamaters;
    private boolean mPreferFirstRepeatedParameter;
    private ValueSanitizer mUnregisteredParameterValueSanitizer =
        getAllIllegal();
    public static interface ValueSanitizer {
        public String sanitize(String value);
    }
    public static class IllegalCharacterValueSanitizer implements
        ValueSanitizer {
        private int mFlags;
        public final static int SPACE_OK =              1 << 0;
        public final static int OTHER_WHITESPACE_OK =  1 << 1;
        public final static int NON_7_BIT_ASCII_OK =    1 << 2;
        public final static int DQUOTE_OK =             1 << 3;
        public final static int SQUOTE_OK =             1 << 4;
        public final static int LT_OK =                 1 << 5;
        public final static int GT_OK =                 1 << 6;
        public final static int AMP_OK =                1 << 7;
        public final static int PCT_OK =                1 << 8;
        public final static int NUL_OK =                1 << 9;
        public final static int SCRIPT_URL_OK =         1 << 10;
        public final static int ALL_OK =                0x7ff;
        public final static int ALL_WHITESPACE_OK =
            SPACE_OK | OTHER_WHITESPACE_OK;
        public final static int ALL_ILLEGAL =
            0;
        public final static int ALL_BUT_NUL_LEGAL =
            ALL_OK & ~NUL_OK;
        public final static int ALL_BUT_WHITESPACE_LEGAL =
            ALL_OK & ~(ALL_WHITESPACE_OK | NUL_OK);
        public final static int URL_LEGAL =
            NON_7_BIT_ASCII_OK | SQUOTE_OK | AMP_OK | PCT_OK;
        public final static int URL_AND_SPACE_LEGAL =
            URL_LEGAL | SPACE_OK;
        public final static int AMP_LEGAL =
            AMP_OK;
        public final static int AMP_AND_SPACE_LEGAL =
            AMP_OK | SPACE_OK;
        public final static int SPACE_LEGAL =
            SPACE_OK;
        public final static int ALL_BUT_NUL_AND_ANGLE_BRACKETS_LEGAL =
            ALL_OK & ~(NUL_OK | LT_OK | GT_OK);
        private final static String JAVASCRIPT_PREFIX = "javascript:";
        private final static String VBSCRIPT_PREFIX = "vbscript:";
        private final static int MIN_SCRIPT_PREFIX_LENGTH = Math.min(
                JAVASCRIPT_PREFIX.length(), VBSCRIPT_PREFIX.length());
        public IllegalCharacterValueSanitizer(
            int flags) {
            mFlags = flags;
        }
        public String sanitize(String value) {
            if (value == null) {
                return null;
            }
            int length = value.length();
            if ((mFlags & SCRIPT_URL_OK) != 0) {
                if (length >= MIN_SCRIPT_PREFIX_LENGTH) {
                    String asLower = value.toLowerCase();
                    if (asLower.startsWith(JAVASCRIPT_PREFIX)  ||
                        asLower.startsWith(VBSCRIPT_PREFIX)) {
                        return "";
                    }
                }
            }
            if ( (mFlags & ALL_WHITESPACE_OK) == 0) {
                value = trimWhitespace(value);
                length = value.length();
            }
            StringBuilder stringBuilder = new StringBuilder(length);
            for(int i = 0; i < length; i++) {
                char c = value.charAt(i);
                if (!characterIsLegal(c)) {
                    if ((mFlags & SPACE_OK) != 0) {
                        c = ' ';
                    }
                    else {
                        c = '_';
                    }
                }
                stringBuilder.append(c);
            }
            return stringBuilder.toString();
        }
        private String trimWhitespace(String value) {
            int start = 0;
            int last = value.length() - 1;
            int end = last;
            while (start <= end && isWhitespace(value.charAt(start))) {
                start++;
            }
            while (end >= start && isWhitespace(value.charAt(end))) {
                end--;
            }
            if (start == 0 && end == last) {
                return value;
            }
            return value.substring(start, end + 1);
        }
        private boolean isWhitespace(char c) {
            switch(c) {
            case ' ':
            case '\t':
            case '\f':
            case '\n':
            case '\r':
            case 11: 
                return true;
            default:
                return false;
            }
        }
        private boolean characterIsLegal(char c) {
            switch(c) {
            case ' ' : return (mFlags & SPACE_OK) != 0;
            case '\t': case '\f': case '\n': case '\r': case 11: 
              return (mFlags & OTHER_WHITESPACE_OK) != 0;
            case '\"': return (mFlags & DQUOTE_OK) != 0;
            case '\'': return (mFlags & SQUOTE_OK) != 0;
            case '<' : return (mFlags & LT_OK) != 0;
            case '>' : return (mFlags & GT_OK) != 0;
            case '&' : return (mFlags & AMP_OK) != 0;
            case '%' : return (mFlags & PCT_OK) != 0;
            case '\0': return (mFlags & NUL_OK) != 0;
            default  : return (c >= 32 && c < 127) ||
                ((c >= 128) && ((mFlags & NON_7_BIT_ASCII_OK) != 0));
            }
        }
    }
    public ValueSanitizer getUnregisteredParameterValueSanitizer() {
        return mUnregisteredParameterValueSanitizer;
    }
    public void setUnregisteredParameterValueSanitizer(
            ValueSanitizer sanitizer) {
        mUnregisteredParameterValueSanitizer = sanitizer;
    }
    private static final ValueSanitizer sAllIllegal =
        new IllegalCharacterValueSanitizer(
                IllegalCharacterValueSanitizer.ALL_ILLEGAL);
    private static final ValueSanitizer sAllButNulLegal =
        new IllegalCharacterValueSanitizer(
                IllegalCharacterValueSanitizer.ALL_BUT_NUL_LEGAL);
    private static final ValueSanitizer sAllButWhitespaceLegal =
        new IllegalCharacterValueSanitizer(
                IllegalCharacterValueSanitizer.ALL_BUT_WHITESPACE_LEGAL);
    private static final ValueSanitizer sURLLegal =
        new IllegalCharacterValueSanitizer(
                IllegalCharacterValueSanitizer.URL_LEGAL);
    private static final ValueSanitizer sUrlAndSpaceLegal =
        new IllegalCharacterValueSanitizer(
                IllegalCharacterValueSanitizer.URL_AND_SPACE_LEGAL);
    private static final ValueSanitizer sAmpLegal =
        new IllegalCharacterValueSanitizer(
                IllegalCharacterValueSanitizer.AMP_LEGAL);
    private static final ValueSanitizer sAmpAndSpaceLegal =
        new IllegalCharacterValueSanitizer(
                IllegalCharacterValueSanitizer.AMP_AND_SPACE_LEGAL);
    private static final ValueSanitizer sSpaceLegal =
        new IllegalCharacterValueSanitizer(
                IllegalCharacterValueSanitizer.SPACE_LEGAL);
    private static final ValueSanitizer sAllButNulAndAngleBracketsLegal =
        new IllegalCharacterValueSanitizer(
                IllegalCharacterValueSanitizer.ALL_BUT_NUL_AND_ANGLE_BRACKETS_LEGAL);
    public static final ValueSanitizer getAllIllegal() {
        return sAllIllegal;
    }
    public static final ValueSanitizer getAllButNulLegal() {
        return sAllButNulLegal;
    }
    public static final ValueSanitizer getAllButWhitespaceLegal() {
        return sAllButWhitespaceLegal;
    }
    public static final ValueSanitizer getUrlLegal() {
        return sURLLegal;
    }
    public static final ValueSanitizer getUrlAndSpaceLegal() {
        return sUrlAndSpaceLegal;
    }
    public static final ValueSanitizer getAmpLegal() {
        return sAmpLegal;
    }
    public static final ValueSanitizer getAmpAndSpaceLegal() {
        return sAmpAndSpaceLegal;
    }
    public static final ValueSanitizer getSpaceLegal() {
        return sSpaceLegal;
    }
    public static final ValueSanitizer getAllButNulAndAngleBracketsLegal() {
        return sAllButNulAndAngleBracketsLegal;
    }
    public UrlQuerySanitizer() {
    }
    public UrlQuerySanitizer(String url) {
        setAllowUnregisteredParamaters(true);
        parseUrl(url);
    }
    public void parseUrl(String url) {
        int queryIndex = url.indexOf('?');
        String query;
        if (queryIndex >= 0) {
            query = url.substring(queryIndex + 1);
        }
        else {
            query = "";
        }
        parseQuery(query);
    }
    public void parseQuery(String query) {
        clear();
        StringTokenizer tokenizer = new StringTokenizer(query, "&");
        while(tokenizer.hasMoreElements()) {
            String attributeValuePair = tokenizer.nextToken();
            if (attributeValuePair.length() > 0) {
                int assignmentIndex = attributeValuePair.indexOf('=');
                if (assignmentIndex < 0) {
                    parseEntry(attributeValuePair, "");
                }
                else {
                    parseEntry(attributeValuePair.substring(0, assignmentIndex),
                            attributeValuePair.substring(assignmentIndex + 1));
                }
            }
        }
    }
    public Set<String> getParameterSet() {
        return mEntries.keySet();
    }
    public List<ParameterValuePair> getParameterList() {
        return mEntriesList;
    }
    public boolean hasParameter(String parameter) {
        return mEntries.containsKey(parameter);
    }
    public String getValue(String parameter) {
        return mEntries.get(parameter);
    }
    public void registerParameter(String parameter,
            ValueSanitizer valueSanitizer) {
        if (valueSanitizer == null) {
            mSanitizers.remove(parameter);
        }
        mSanitizers.put(parameter, valueSanitizer);
    }
    public void registerParameters(String[] parameters,
            ValueSanitizer valueSanitizer) {
        int length = parameters.length;
        for(int i = 0; i < length; i++) {
            mSanitizers.put(parameters[i], valueSanitizer);
        }
    }
    public void setAllowUnregisteredParamaters(
            boolean allowUnregisteredParamaters) {
        mAllowUnregisteredParamaters = allowUnregisteredParamaters;
    }
    public boolean getAllowUnregisteredParamaters() {
        return mAllowUnregisteredParamaters;
    }
    public void setPreferFirstRepeatedParameter(
            boolean preferFirstRepeatedParameter) {
        mPreferFirstRepeatedParameter = preferFirstRepeatedParameter;
    }
    public boolean getPreferFirstRepeatedParameter() {
        return mPreferFirstRepeatedParameter;
    }
    protected void parseEntry(String parameter, String value) {
        String unescapedParameter = unescape(parameter);
         ValueSanitizer valueSanitizer =
            getEffectiveValueSanitizer(unescapedParameter);
        if (valueSanitizer == null) {
            return;
        }
        String unescapedValue = unescape(value);
        String sanitizedValue = valueSanitizer.sanitize(unescapedValue);
        addSanitizedEntry(unescapedParameter, sanitizedValue);
    }
    protected void addSanitizedEntry(String parameter, String value) {
        mEntriesList.add(
                new ParameterValuePair(parameter, value));
        if (mPreferFirstRepeatedParameter) {
            if (mEntries.containsKey(parameter)) {
                return;
            }
        }
        mEntries.put(parameter, value);
    }
    public ValueSanitizer getValueSanitizer(String parameter) {
        return mSanitizers.get(parameter);
    }
    public ValueSanitizer getEffectiveValueSanitizer(String parameter) {
        ValueSanitizer sanitizer = getValueSanitizer(parameter);
        if (sanitizer == null && mAllowUnregisteredParamaters) {
            sanitizer = getUnregisteredParameterValueSanitizer();
        }
        return sanitizer;
    }
    public String unescape(String string) {
        int firstEscape = string.indexOf('%');
        if ( firstEscape < 0) {
            firstEscape = string.indexOf('+');
            if (firstEscape < 0) {
                return string;
            }
        }
        int length = string.length();
        StringBuilder stringBuilder = new StringBuilder(length);
        stringBuilder.append(string.substring(0, firstEscape));
        for (int i = firstEscape; i < length; i++) {
            char c = string.charAt(i);
            if (c == '+') {
                c = ' ';
            }
            else if ( c == '%' && i + 2 < length) {
                char c1 = string.charAt(i + 1);
                char c2 = string.charAt(i + 2);
                if (isHexDigit(c1) && isHexDigit(c2)) {
                    c = (char) (decodeHexDigit(c1) * 16 + decodeHexDigit(c2));
                    i += 2;
                }
            }
            stringBuilder.append(c);
        }
        return stringBuilder.toString();
    }
    protected boolean isHexDigit(char c) {
        return decodeHexDigit(c) >= 0;
    }
    protected int decodeHexDigit(char c) {
        if (c >= '0' && c <= '9') {
            return c - '0';
        }
        else if (c >= 'A' && c <= 'F') {
            return c - 'A' + 10;
        }
        else if (c >= 'a' && c <= 'f') {
            return c - 'a' + 10;
        }
        else {
            return -1;
        }
    }
    protected void clear() {
        mEntries.clear();
        mEntriesList.clear();
    }
}
