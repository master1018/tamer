public final class Locale implements Cloneable, Serializable {
    private static final long serialVersionUID = 9149081749638150636L;
    private static volatile Locale[] availableLocales;
    private static Locale defaultLocale = new Locale();
    public static final Locale CANADA = new Locale("en", "CA"); 
    public static final Locale CANADA_FRENCH = new Locale("fr", "CA"); 
    public static final Locale CHINA = new Locale("zh", "CN"); 
    public static final Locale CHINESE = new Locale("zh", ""); 
    public static final Locale ENGLISH = new Locale("en", ""); 
    public static final Locale FRANCE = new Locale("fr", "FR"); 
    public static final Locale FRENCH = new Locale("fr", ""); 
    public static final Locale GERMAN = new Locale("de", ""); 
    public static final Locale GERMANY = new Locale("de", "DE"); 
    public static final Locale ITALIAN = new Locale("it", ""); 
    public static final Locale ITALY = new Locale("it", "IT"); 
    public static final Locale JAPAN = new Locale("ja", "JP"); 
    public static final Locale JAPANESE = new Locale("ja", ""); 
    public static final Locale KOREA = new Locale("ko", "KR"); 
    public static final Locale KOREAN = new Locale("ko", ""); 
    public static final Locale PRC = new Locale("zh", "CN"); 
    public static final Locale SIMPLIFIED_CHINESE = new Locale("zh", "CN"); 
    public static final Locale TAIWAN = new Locale("zh", "TW"); 
    public static final Locale TRADITIONAL_CHINESE = new Locale("zh", "TW"); 
    public static final Locale UK = new Locale("en", "GB"); 
    public static final Locale US = new Locale("en", "US"); 
    private static final PropertyPermission setLocalePermission = new PropertyPermission(
            "user.language", "write"); 
    static {
        String language = AccessController
                .doPrivileged(new PriviAction<String>("user.language", "en")); 
        String region = AccessController.doPrivileged(new PriviAction<String>(
                "user.region", "US")); 
        String variant = AccessController.doPrivileged(new PriviAction<String>(
                "user.variant", "")); 
        defaultLocale = new Locale(language, region, variant);
    }
    private transient String countryCode;
    private transient String languageCode;
    private transient String variantCode;
    private transient String cachedToStringResult;
	private Locale() {
		languageCode = "en"; 
		countryCode = "US"; 
		variantCode = ""; 
	}
    public Locale(String language) {
        this(language, "", ""); 
    }
    public Locale(String language, String country) {
        this(language, country, ""); 
    }
    public Locale(String language, String country, String variant) {
        if (language == null || country == null || variant == null) {
            throw new NullPointerException();
        }
        if(language.length() == 0 && country.length() == 0){
            languageCode = "";
            countryCode = "";
            variantCode = variant;
            return;
        }
        languageCode = Util.toASCIILowerCase(language);
        if (languageCode.equals("he")) {
            languageCode = "iw"; 
        } else if (languageCode.equals("id")) {
            languageCode = "in"; 
        } else if (languageCode.equals("yi")) {
            languageCode = "ji"; 
        }
        countryCode = Util.toASCIIUpperCase(country);
        variantCode = variant;
    }
    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e); 
        }
    }
    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (object instanceof Locale) {
            Locale o = (Locale) object;
            return languageCode.equals(o.languageCode)
                    && countryCode.equals(o.countryCode)
                    && variantCode.equals(o.variantCode);
        }
        return false;
    }
    static Locale[] find() {
        String[] locales = Resources.getAvailableLocales();
        ArrayList<Locale> temp = new ArrayList<Locale>();
        for (int i = 0; i < locales.length; i++) {
            String s = locales[i];
            int first = s.indexOf('_');
            int second = s.indexOf('_', first + 1);
            if (first == -1) {
                temp.add(new Locale(s));
            } else if (second == -1) {
                temp.add(new Locale(s.substring(0, first), s.substring(first + 1)));
            } else {
                temp.add(new Locale(s.substring(0, first), s.substring(first + 1, second), s.substring(second + 1)));
            }
        }
        Locale[] result = new Locale[temp.size()];
        return temp.toArray(result);
    }
    public static Locale[] getAvailableLocales() {
        if (availableLocales == null) {
            availableLocales = find();
        }
        return availableLocales.clone();
    }
    public String getCountry() {
        return countryCode;
    }
    public static Locale getDefault() {
        return defaultLocale;
    }
    public final String getDisplayCountry() {
        return getDisplayCountry(getDefault());
    }
    public String getDisplayCountry(Locale locale) {
        if (countryCode.length() == 0) {
            return countryCode;
        }
        String result = Resources.getDisplayCountryNative(toString(), locale.toString());
        if (result == null) { 
            result = Resources.getDisplayCountryNative(toString(), Locale.getDefault().toString());
        }
        return result;
    }
    public final String getDisplayLanguage() {
        return getDisplayLanguage(getDefault());
    }
    public String getDisplayLanguage(Locale locale) {
        if (languageCode.length() == 0) {
            return languageCode;
        }
        String result = Resources.getDisplayLanguageNative(toString(), locale.toString());
        if (result == null) { 
            result = Resources.getDisplayLanguageNative(toString(), Locale.getDefault().toString());
        }
        return result;
    }
    public final String getDisplayName() {
        return getDisplayName(getDefault());
    }
    public String getDisplayName(Locale locale) {
        int count = 0;
        StringBuilder buffer = new StringBuilder();
        if (languageCode.length() > 0) {
            buffer.append(getDisplayLanguage(locale));
            count++;
        }
        if (countryCode.length() > 0) {
            if (count == 1) {
                buffer.append(" ("); 
            }
            buffer.append(getDisplayCountry(locale));
            count++;
        }
        if (variantCode.length() > 0) {
            if (count == 1) {
                buffer.append(" ("); 
            } else if (count == 2) {
                buffer.append(","); 
            }
            buffer.append(getDisplayVariant(locale));
            count++;
        }
        if (count > 1) {
            buffer.append(")"); 
        }
        return buffer.toString();
    }
    public final String getDisplayVariant() {
        return getDisplayVariant(getDefault());
    }
    public String getDisplayVariant(Locale locale) {
        if (variantCode.length() == 0) {
            return variantCode;
        }
        String result = Resources.getDisplayVariantNative(toString(), locale.toString());
        if (result == null) { 
            result = Resources.getDisplayVariantNative(toString(), Locale.getDefault().toString());
        }
        return result;
    }
    public String getISO3Country() throws MissingResourceException {
        if (countryCode.length() == 0) {
            return countryCode;
        }
        return Resources.getISO3CountryNative(toString());
    }
    public String getISO3Language() throws MissingResourceException {
        if (languageCode.length() == 0) {
            return languageCode;
        }
        return Resources.getISO3LanguageNative(toString());
    }
    public static String[] getISOCountries() {
        return Resources.getISOCountries();
    }
    public static String[] getISOLanguages() {
        return Resources.getISOLanguages();
    }
    public String getLanguage() {
        return languageCode;
    }
    public String getVariant() {
        return variantCode;
    }
    @Override
    public synchronized int hashCode() {
        return countryCode.hashCode() + languageCode.hashCode()
                + variantCode.hashCode();
    }
    public synchronized static void setDefault(Locale locale) {
        if (locale != null) {
            SecurityManager security = System.getSecurityManager();
            if (security != null) {
                security.checkPermission(setLocalePermission);
            }
            defaultLocale = locale;
        } else {
            throw new NullPointerException();
        }
    }
    @Override
    public final String toString() {
        String result = cachedToStringResult;
        return (result == null) ? (cachedToStringResult = toNewString()) : result;
    }
    private String toNewString() {
        if (languageCode.length() == 0 && countryCode.length() == 0) {
            return "";
        }
        StringBuilder result = new StringBuilder(11);
        result.append(languageCode);
        if (countryCode.length() > 0 || variantCode.length() > 0) {
            result.append('_');
        }
        result.append(countryCode);
        if (variantCode.length() > 0) {
            result.append('_');
        }
        result.append(variantCode);
        return result.toString();
    }
    private static final ObjectStreamField[] serialPersistentFields = {
            new ObjectStreamField("country", String.class), 
            new ObjectStreamField("hashcode", Integer.TYPE), 
            new ObjectStreamField("language", String.class), 
            new ObjectStreamField("variant", String.class) }; 
    private void writeObject(ObjectOutputStream stream) throws IOException {
        ObjectOutputStream.PutField fields = stream.putFields();
        fields.put("country", countryCode); 
        fields.put("hashcode", -1); 
        fields.put("language", languageCode); 
        fields.put("variant", variantCode); 
        stream.writeFields();
    }
    private void readObject(ObjectInputStream stream) throws IOException,
            ClassNotFoundException {
        ObjectInputStream.GetField fields = stream.readFields();
        countryCode = (String) fields.get("country", ""); 
        languageCode = (String) fields.get("language", ""); 
        variantCode = (String) fields.get("variant", ""); 
    }
}
