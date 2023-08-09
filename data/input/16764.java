public class UnicodeLocaleExtension extends Extension {
    public static final char SINGLETON = 'u';
    private final Set<String> attributes;
    private final Map<String, String> keywords;
    public static final UnicodeLocaleExtension CA_JAPANESE
        = new UnicodeLocaleExtension("ca", "japanese");
    public static final UnicodeLocaleExtension NU_THAI
        = new UnicodeLocaleExtension("nu", "thai");
    private UnicodeLocaleExtension(String key, String value) {
        super(SINGLETON, key + "-" + value);
        attributes = Collections.emptySet();
        keywords = Collections.singletonMap(key, value);
    }
    UnicodeLocaleExtension(SortedSet<String> attributes, SortedMap<String, String> keywords) {
        super(SINGLETON);
        if (attributes != null) {
            this.attributes = attributes;
        } else {
            this.attributes = Collections.emptySet();
        }
        if (keywords != null) {
            this.keywords = keywords;
        } else {
            this.keywords = Collections.emptyMap();
        }
        if (!this.attributes.isEmpty() || !this.keywords.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (String attribute : this.attributes) {
                sb.append(LanguageTag.SEP).append(attribute);
            }
            for (Entry<String, String> keyword : this.keywords.entrySet()) {
                String key = keyword.getKey();
                String value = keyword.getValue();
                sb.append(LanguageTag.SEP).append(key);
                if (value.length() > 0) {
                    sb.append(LanguageTag.SEP).append(value);
                }
            }
            setValue(sb.substring(1));   
        }
    }
    public Set<String> getUnicodeLocaleAttributes() {
        if (attributes == Collections.EMPTY_SET) {
            return attributes;
        }
        return Collections.unmodifiableSet(attributes);
    }
    public Set<String> getUnicodeLocaleKeys() {
        if (keywords == Collections.EMPTY_MAP) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(keywords.keySet());
    }
    public String getUnicodeLocaleType(String unicodeLocaleKey) {
        return keywords.get(unicodeLocaleKey);
    }
    public static boolean isSingletonChar(char c) {
        return (SINGLETON == LocaleUtils.toLower(c));
    }
    public static boolean isAttribute(String s) {
        int len = s.length();
        return (len >= 3) && (len <= 8) && LocaleUtils.isAlphaNumericString(s);
    }
    public static boolean isKey(String s) {
        return (s.length() == 2) && LocaleUtils.isAlphaNumericString(s);
    }
    public static boolean isTypeSubtag(String s) {
        int len = s.length();
        return (len >= 3) && (len <= 8) && LocaleUtils.isAlphaNumericString(s);
    }
}
