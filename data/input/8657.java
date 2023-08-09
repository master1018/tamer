public final class Locale implements Cloneable, Serializable {
    static private final  Cache LOCALECACHE = new Cache();
    static public final Locale ENGLISH = createConstant("en", "");
    static public final Locale FRENCH = createConstant("fr", "");
    static public final Locale GERMAN = createConstant("de", "");
    static public final Locale ITALIAN = createConstant("it", "");
    static public final Locale JAPANESE = createConstant("ja", "");
    static public final Locale KOREAN = createConstant("ko", "");
    static public final Locale CHINESE = createConstant("zh", "");
    static public final Locale SIMPLIFIED_CHINESE = createConstant("zh", "CN");
    static public final Locale TRADITIONAL_CHINESE = createConstant("zh", "TW");
    static public final Locale FRANCE = createConstant("fr", "FR");
    static public final Locale GERMANY = createConstant("de", "DE");
    static public final Locale ITALY = createConstant("it", "IT");
    static public final Locale JAPAN = createConstant("ja", "JP");
    static public final Locale KOREA = createConstant("ko", "KR");
    static public final Locale CHINA = SIMPLIFIED_CHINESE;
    static public final Locale PRC = SIMPLIFIED_CHINESE;
    static public final Locale TAIWAN = TRADITIONAL_CHINESE;
    static public final Locale UK = createConstant("en", "GB");
    static public final Locale US = createConstant("en", "US");
    static public final Locale CANADA = createConstant("en", "CA");
    static public final Locale CANADA_FRENCH = createConstant("fr", "CA");
    static public final Locale ROOT = createConstant("", "");
    static public final char PRIVATE_USE_EXTENSION = 'x';
    static public final char UNICODE_LOCALE_EXTENSION = 'u';
    static final long serialVersionUID = 9149081749638150636L;
    private static final int DISPLAY_LANGUAGE = 0;
    private static final int DISPLAY_COUNTRY  = 1;
    private static final int DISPLAY_VARIANT  = 2;
    private static final int DISPLAY_SCRIPT   = 3;
    private Locale(BaseLocale baseLocale, LocaleExtensions extensions) {
        this.baseLocale = baseLocale;
        this.localeExtensions = extensions;
    }
    public Locale(String language, String country, String variant) {
        if (language== null || country == null || variant == null) {
            throw new NullPointerException();
        }
        baseLocale = BaseLocale.getInstance(convertOldISOCodes(language), "", country, variant);
        localeExtensions = getCompatibilityExtensions(language, "", country, variant);
    }
    public Locale(String language, String country) {
        this(language, country, "");
    }
    public Locale(String language) {
        this(language, "", "");
    }
    private static Locale createConstant(String lang, String country) {
        BaseLocale base = BaseLocale.createInstance(lang, country);
        return getInstance(base, null);
    }
    static Locale getInstance(String language, String country, String variant) {
        return getInstance(language, "", country, variant, null);
    }
    static Locale getInstance(String language, String script, String country,
                                      String variant, LocaleExtensions extensions) {
        if (language== null || script == null || country == null || variant == null) {
            throw new NullPointerException();
        }
        if (extensions == null) {
            extensions = getCompatibilityExtensions(language, script, country, variant);
        }
        BaseLocale baseloc = BaseLocale.getInstance(language, script, country, variant);
        return getInstance(baseloc, extensions);
    }
    static Locale getInstance(BaseLocale baseloc, LocaleExtensions extensions) {
        LocaleKey key = new LocaleKey(baseloc, extensions);
        return LOCALECACHE.get(key);
    }
    private static class Cache extends LocaleObjectCache<LocaleKey, Locale> {
        private Cache() {
        }
        @Override
        protected Locale createObject(LocaleKey key) {
            return new Locale(key.base, key.exts);
        }
    }
    private static final class LocaleKey {
        private final BaseLocale base;
        private final LocaleExtensions exts;
        private final int hash;
        private LocaleKey(BaseLocale baseLocale, LocaleExtensions extensions) {
            base = baseLocale;
            exts = extensions;
            int h = base.hashCode();
            if (exts != null) {
                h ^= exts.hashCode();
            }
            hash = h;
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof LocaleKey)) {
                return false;
            }
            LocaleKey other = (LocaleKey)obj;
            if (hash != other.hash || !base.equals(other.base)) {
                return false;
            }
            if (exts == null) {
                return other.exts == null;
            }
            return exts.equals(other.exts);
        }
        @Override
        public int hashCode() {
            return hash;
        }
    }
    public static Locale getDefault() {
        if (defaultLocale == null) {
            initDefault();
        }
        return defaultLocale;
    }
    public static Locale getDefault(Locale.Category category) {
        switch (category) {
        case DISPLAY:
            if (defaultDisplayLocale == null) {
                initDefault(category);
            }
            return defaultDisplayLocale;
        case FORMAT:
            if (defaultFormatLocale == null) {
                initDefault(category);
            }
            return defaultFormatLocale;
        default:
            assert false: "Unknown Category";
        }
        return getDefault();
    }
    private static void initDefault() {
        String language, region, script, country, variant;
        language = AccessController.doPrivileged(
            new GetPropertyAction("user.language", "en"));
        region = AccessController.doPrivileged(
            new GetPropertyAction("user.region"));
        if (region != null) {
            int i = region.indexOf('_');
            if (i >= 0) {
                country = region.substring(0, i);
                variant = region.substring(i + 1);
            } else {
                country = region;
                variant = "";
            }
            script = "";
        } else {
            script = AccessController.doPrivileged(
                new GetPropertyAction("user.script", ""));
            country = AccessController.doPrivileged(
                new GetPropertyAction("user.country", ""));
            variant = AccessController.doPrivileged(
                new GetPropertyAction("user.variant", ""));
        }
        defaultLocale = getInstance(language, script, country, variant, null);
    }
    private static void initDefault(Locale.Category category) {
        if (defaultLocale == null) {
            initDefault();
        }
        Locale defaultCategoryLocale = getInstance(
            AccessController.doPrivileged(
                new GetPropertyAction(category.languageKey, defaultLocale.getLanguage())),
            AccessController.doPrivileged(
                new GetPropertyAction(category.scriptKey, defaultLocale.getScript())),
            AccessController.doPrivileged(
                new GetPropertyAction(category.countryKey, defaultLocale.getCountry())),
            AccessController.doPrivileged(
                new GetPropertyAction(category.variantKey, defaultLocale.getVariant())),
            null);
        switch (category) {
        case DISPLAY:
            defaultDisplayLocale = defaultCategoryLocale;
            break;
        case FORMAT:
            defaultFormatLocale = defaultCategoryLocale;
            break;
        }
    }
    public static synchronized void setDefault(Locale newLocale) {
        setDefault(Category.DISPLAY, newLocale);
        setDefault(Category.FORMAT, newLocale);
        defaultLocale = newLocale;
    }
    public static synchronized void setDefault(Locale.Category category,
        Locale newLocale) {
        if (category == null)
            throw new NullPointerException("Category cannot be NULL");
        if (newLocale == null)
            throw new NullPointerException("Can't set default locale to NULL");
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(new PropertyPermission
                        ("user.language", "write"));
        switch (category) {
        case DISPLAY:
            defaultDisplayLocale = newLocale;
            break;
        case FORMAT:
            defaultFormatLocale = newLocale;
            break;
        default:
            assert false: "Unknown Category";
        }
    }
    public static Locale[] getAvailableLocales() {
        return LocaleServiceProviderPool.getAllAvailableLocales();
    }
    public static String[] getISOCountries() {
        if (isoCountries == null) {
            isoCountries = getISO2Table(LocaleISOData.isoCountryTable);
        }
        String[] result = new String[isoCountries.length];
        System.arraycopy(isoCountries, 0, result, 0, isoCountries.length);
        return result;
    }
    public static String[] getISOLanguages() {
        if (isoLanguages == null) {
            isoLanguages = getISO2Table(LocaleISOData.isoLanguageTable);
        }
        String[] result = new String[isoLanguages.length];
        System.arraycopy(isoLanguages, 0, result, 0, isoLanguages.length);
        return result;
    }
    private static final String[] getISO2Table(String table) {
        int len = table.length() / 5;
        String[] isoTable = new String[len];
        for (int i = 0, j = 0; i < len; i++, j += 5) {
            isoTable[i] = table.substring(j, j + 2);
        }
        return isoTable;
    }
    public String getLanguage() {
        return baseLocale.getLanguage();
    }
    public String getScript() {
        return baseLocale.getScript();
    }
    public String getCountry() {
        return baseLocale.getRegion();
    }
    public String getVariant() {
        return baseLocale.getVariant();
    }
    public String getExtension(char key) {
        if (!LocaleExtensions.isValidKey(key)) {
            throw new IllegalArgumentException("Ill-formed extension key: " + key);
        }
        return (localeExtensions == null) ? null : localeExtensions.getExtensionValue(key);
    }
    public Set<Character> getExtensionKeys() {
        if (localeExtensions == null) {
            return Collections.emptySet();
        }
        return localeExtensions.getKeys();
    }
    public Set<String> getUnicodeLocaleAttributes() {
        if (localeExtensions == null) {
            return Collections.emptySet();
        }
        return localeExtensions.getUnicodeLocaleAttributes();
    }
    public String getUnicodeLocaleType(String key) {
        if (!UnicodeLocaleExtension.isKey(key)) {
            throw new IllegalArgumentException("Ill-formed Unicode locale key: " + key);
        }
        return (localeExtensions == null) ? null : localeExtensions.getUnicodeLocaleType(key);
    }
    public Set<String> getUnicodeLocaleKeys() {
        if (localeExtensions == null) {
            return Collections.emptySet();
        }
        return localeExtensions.getUnicodeLocaleKeys();
    }
    BaseLocale getBaseLocale() {
        return baseLocale;
    }
     LocaleExtensions getLocaleExtensions() {
         return localeExtensions;
     }
    @Override
    public final String toString() {
        boolean l = (baseLocale.getLanguage().length() != 0);
        boolean s = (baseLocale.getScript().length() != 0);
        boolean r = (baseLocale.getRegion().length() != 0);
        boolean v = (baseLocale.getVariant().length() != 0);
        boolean e = (localeExtensions != null && localeExtensions.getID().length() != 0);
        StringBuilder result = new StringBuilder(baseLocale.getLanguage());
        if (r || (l && (v || s || e))) {
            result.append('_')
                .append(baseLocale.getRegion()); 
        }
        if (v && (l || r)) {
            result.append('_')
                .append(baseLocale.getVariant());
        }
        if (s && (l || r)) {
            result.append("_#")
                .append(baseLocale.getScript());
        }
        if (e && (l || r)) {
            result.append('_');
            if (!s) {
                result.append('#');
            }
            result.append(localeExtensions.getID());
        }
        return result.toString();
    }
    public String toLanguageTag() {
        LanguageTag tag = LanguageTag.parseLocale(baseLocale, localeExtensions);
        StringBuilder buf = new StringBuilder();
        String subtag = tag.getLanguage();
        if (subtag.length() > 0) {
            buf.append(LanguageTag.canonicalizeLanguage(subtag));
        }
        subtag = tag.getScript();
        if (subtag.length() > 0) {
            buf.append(LanguageTag.SEP);
            buf.append(LanguageTag.canonicalizeScript(subtag));
        }
        subtag = tag.getRegion();
        if (subtag.length() > 0) {
            buf.append(LanguageTag.SEP);
            buf.append(LanguageTag.canonicalizeRegion(subtag));
        }
        List<String>subtags = tag.getVariants();
        for (String s : subtags) {
            buf.append(LanguageTag.SEP);
            buf.append(s);
        }
        subtags = tag.getExtensions();
        for (String s : subtags) {
            buf.append(LanguageTag.SEP);
            buf.append(LanguageTag.canonicalizeExtension(s));
        }
        subtag = tag.getPrivateuse();
        if (subtag.length() > 0) {
            if (buf.length() > 0) {
                buf.append(LanguageTag.SEP);
            }
            buf.append(LanguageTag.PRIVATEUSE).append(LanguageTag.SEP);
            buf.append(subtag);
        }
        return buf.toString();
    }
    public static Locale forLanguageTag(String languageTag) {
        LanguageTag tag = LanguageTag.parse(languageTag, null);
        InternalLocaleBuilder bldr = new InternalLocaleBuilder();
        bldr.setLanguageTag(tag);
        BaseLocale base = bldr.getBaseLocale();
        LocaleExtensions exts = bldr.getLocaleExtensions();
        if (exts == null && base.getVariant().length() > 0) {
            exts = getCompatibilityExtensions(base.getLanguage(), base.getScript(),
                                              base.getRegion(), base.getVariant());
        }
        return getInstance(base, exts);
    }
    public String getISO3Language() throws MissingResourceException {
        String lang = baseLocale.getLanguage();
        if (lang.length() == 3) {
            return lang;
        }
        String language3 = getISO3Code(lang, LocaleISOData.isoLanguageTable);
        if (language3 == null) {
            throw new MissingResourceException("Couldn't find 3-letter language code for "
                    + lang, "FormatData_" + toString(), "ShortLanguage");
        }
        return language3;
    }
    public String getISO3Country() throws MissingResourceException {
        String country3 = getISO3Code(baseLocale.getRegion(), LocaleISOData.isoCountryTable);
        if (country3 == null) {
            throw new MissingResourceException("Couldn't find 3-letter country code for "
                    + baseLocale.getRegion(), "FormatData_" + toString(), "ShortCountry");
        }
        return country3;
    }
    private static final String getISO3Code(String iso2Code, String table) {
        int codeLength = iso2Code.length();
        if (codeLength == 0) {
            return "";
        }
        int tableLength = table.length();
        int index = tableLength;
        if (codeLength == 2) {
            char c1 = iso2Code.charAt(0);
            char c2 = iso2Code.charAt(1);
            for (index = 0; index < tableLength; index += 5) {
                if (table.charAt(index) == c1
                    && table.charAt(index + 1) == c2) {
                    break;
                }
            }
        }
        return index < tableLength ? table.substring(index + 2, index + 5) : null;
    }
    public final String getDisplayLanguage() {
        return getDisplayLanguage(getDefault(Category.DISPLAY));
    }
    public String getDisplayLanguage(Locale inLocale) {
        return getDisplayString(baseLocale.getLanguage(), inLocale, DISPLAY_LANGUAGE);
    }
    public String getDisplayScript() {
        return getDisplayScript(getDefault());
    }
    public String getDisplayScript(Locale inLocale) {
        return getDisplayString(baseLocale.getScript(), inLocale, DISPLAY_SCRIPT);
    }
    public final String getDisplayCountry() {
        return getDisplayCountry(getDefault(Category.DISPLAY));
    }
    public String getDisplayCountry(Locale inLocale) {
        return getDisplayString(baseLocale.getRegion(), inLocale, DISPLAY_COUNTRY);
    }
    private String getDisplayString(String code, Locale inLocale, int type) {
        if (code.length() == 0) {
            return "";
        }
        if (inLocale == null) {
            throw new NullPointerException();
        }
        try {
            OpenListResourceBundle bundle = LocaleData.getLocaleNames(inLocale);
            String key = (type == DISPLAY_VARIANT ? "%%"+code : code);
            String result = null;
            LocaleServiceProviderPool pool =
                LocaleServiceProviderPool.getPool(LocaleNameProvider.class);
            if (pool.hasProviders()) {
                result = pool.getLocalizedObject(
                                    LocaleNameGetter.INSTANCE,
                                    inLocale, bundle, key,
                                    type, code);
            }
            if (result == null) {
                result = bundle.getString(key);
            }
            if (result != null) {
                return result;
            }
        }
        catch (Exception e) {
        }
        return code;
    }
    public final String getDisplayVariant() {
        return getDisplayVariant(getDefault(Category.DISPLAY));
    }
    public String getDisplayVariant(Locale inLocale) {
        if (baseLocale.getVariant().length() == 0)
            return "";
        OpenListResourceBundle bundle = LocaleData.getLocaleNames(inLocale);
        String names[] = getDisplayVariantArray(bundle, inLocale);
        String listPattern = null;
        String listCompositionPattern = null;
        try {
            listPattern = bundle.getString("ListPattern");
            listCompositionPattern = bundle.getString("ListCompositionPattern");
        } catch (MissingResourceException e) {
        }
        return formatList(names, listPattern, listCompositionPattern);
    }
    public final String getDisplayName() {
        return getDisplayName(getDefault(Category.DISPLAY));
    }
    public String getDisplayName(Locale inLocale) {
        OpenListResourceBundle bundle = LocaleData.getLocaleNames(inLocale);
        String languageName = getDisplayLanguage(inLocale);
        String scriptName = getDisplayScript(inLocale);
        String countryName = getDisplayCountry(inLocale);
        String[] variantNames = getDisplayVariantArray(bundle, inLocale);
        String displayNamePattern = null;
        String listPattern = null;
        String listCompositionPattern = null;
        try {
            displayNamePattern = bundle.getString("DisplayNamePattern");
            listPattern = bundle.getString("ListPattern");
            listCompositionPattern = bundle.getString("ListCompositionPattern");
        } catch (MissingResourceException e) {
        }
        String   mainName       = null;
        String[] qualifierNames = null;
        if (languageName.length() == 0 && scriptName.length() == 0 && countryName.length() == 0) {
            if (variantNames.length == 0) {
                return "";
            } else {
                return formatList(variantNames, listPattern, listCompositionPattern);
            }
        }
        ArrayList<String> names = new ArrayList<>(4);
        if (languageName.length() != 0) {
            names.add(languageName);
        }
        if (scriptName.length() != 0) {
            names.add(scriptName);
        }
        if (countryName.length() != 0) {
            names.add(countryName);
        }
        if (variantNames.length != 0) {
            for (String var : variantNames) {
                names.add(var);
            }
        }
        mainName = names.get(0);
        int numNames = names.size();
        qualifierNames = (numNames > 1) ?
                names.subList(1, numNames).toArray(new String[numNames - 1]) : new String[0];
        Object[] displayNames = {
            new Integer(qualifierNames.length != 0 ? 2 : 1),
            mainName,
            qualifierNames.length != 0 ? formatList(qualifierNames, listPattern, listCompositionPattern) : null
        };
        if (displayNamePattern != null) {
            return new MessageFormat(displayNamePattern).format(displayNames);
        }
        else {
            StringBuilder result = new StringBuilder();
            result.append((String)displayNames[1]);
            if (displayNames.length > 2) {
                result.append(" (");
                result.append((String)displayNames[2]);
                result.append(')');
            }
            return result.toString();
        }
    }
    public Object clone()
    {
        try {
            Locale that = (Locale)super.clone();
            return that;
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }
    @Override
    public int hashCode() {
        int hc = hashCodeValue;
        if (hc == 0) {
            hc = baseLocale.hashCode();
            if (localeExtensions != null) {
                hc ^= localeExtensions.hashCode();
            }
            hashCodeValue = hc;
        }
        return hc;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)                      
            return true;
        if (!(obj instanceof Locale))
            return false;
        BaseLocale otherBase = ((Locale)obj).baseLocale;
        if (!baseLocale.equals(otherBase)) {
            return false;
        }
        if (localeExtensions == null) {
            return ((Locale)obj).localeExtensions == null;
        }
        return localeExtensions.equals(((Locale)obj).localeExtensions);
    }
    private transient BaseLocale baseLocale;
    private transient LocaleExtensions localeExtensions;
    private transient volatile int hashCodeValue = 0;
    private static Locale defaultLocale = null;
    private static Locale defaultDisplayLocale = null;
    private static Locale defaultFormatLocale = null;
    private String[] getDisplayVariantArray(OpenListResourceBundle bundle, Locale inLocale) {
        StringTokenizer tokenizer = new StringTokenizer(baseLocale.getVariant(), "_");
        String[] names = new String[tokenizer.countTokens()];
        for (int i=0; i<names.length; ++i) {
            names[i] = getDisplayString(tokenizer.nextToken(),
                                inLocale, DISPLAY_VARIANT);
        }
        return names;
    }
    private static String formatList(String[] stringList, String listPattern, String listCompositionPattern) {
        if (listPattern == null || listCompositionPattern == null) {
            StringBuffer result = new StringBuffer();
            for (int i=0; i<stringList.length; ++i) {
                if (i>0) result.append(',');
                result.append(stringList[i]);
            }
            return result.toString();
        }
        if (stringList.length > 3) {
            MessageFormat format = new MessageFormat(listCompositionPattern);
            stringList = composeList(format, stringList);
        }
        Object[] args = new Object[stringList.length + 1];
        System.arraycopy(stringList, 0, args, 1, stringList.length);
        args[0] = new Integer(stringList.length);
        MessageFormat format = new MessageFormat(listPattern);
        return format.format(args);
    }
    private static String[] composeList(MessageFormat format, String[] list) {
        if (list.length <= 3) return list;
        String[] listItems = { list[0], list[1] };
        String newItem = format.format(listItems);
        String[] newList = new String[list.length-1];
        System.arraycopy(list, 2, newList, 1, newList.length-1);
        newList[0] = newItem;
        return composeList(format, newList);
    }
    private static final ObjectStreamField[] serialPersistentFields = {
        new ObjectStreamField("language", String.class),
        new ObjectStreamField("country", String.class),
        new ObjectStreamField("variant", String.class),
        new ObjectStreamField("hashcode", int.class),
        new ObjectStreamField("script", String.class),
        new ObjectStreamField("extensions", String.class),
    };
    private void writeObject(ObjectOutputStream out) throws IOException {
        ObjectOutputStream.PutField fields = out.putFields();
        fields.put("language", baseLocale.getLanguage());
        fields.put("script", baseLocale.getScript());
        fields.put("country", baseLocale.getRegion());
        fields.put("variant", baseLocale.getVariant());
        fields.put("extensions", localeExtensions == null ? "" : localeExtensions.getID());
        fields.put("hashcode", -1); 
        out.writeFields();
    }
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        ObjectInputStream.GetField fields = in.readFields();
        String language = (String)fields.get("language", "");
        String script = (String)fields.get("script", "");
        String country = (String)fields.get("country", "");
        String variant = (String)fields.get("variant", "");
        String extStr = (String)fields.get("extensions", "");
        baseLocale = BaseLocale.getInstance(convertOldISOCodes(language), script, country, variant);
        if (extStr.length() > 0) {
            try {
                InternalLocaleBuilder bldr = new InternalLocaleBuilder();
                bldr.setExtensions(extStr);
                localeExtensions = bldr.getLocaleExtensions();
            } catch (LocaleSyntaxException e) {
                throw new IllformedLocaleException(e.getMessage());
            }
        } else {
            localeExtensions = null;
        }
    }
    private Object readResolve() throws java.io.ObjectStreamException {
        return getInstance(baseLocale.getLanguage(), baseLocale.getScript(),
                baseLocale.getRegion(), baseLocale.getVariant(), localeExtensions);
    }
    private static volatile String[] isoLanguages = null;
    private static volatile String[] isoCountries = null;
    private static String convertOldISOCodes(String language) {
        language = LocaleUtils.toLowerString(language).intern();
        if (language == "he") {
            return "iw";
        } else if (language == "yi") {
            return "ji";
        } else if (language == "id") {
            return "in";
        } else {
            return language;
        }
    }
    private static LocaleExtensions getCompatibilityExtensions(String language,
                                                               String script,
                                                               String country,
                                                               String variant) {
        LocaleExtensions extensions = null;
        if (LocaleUtils.caseIgnoreMatch(language, "ja")
                && script.length() == 0
                && LocaleUtils.caseIgnoreMatch(country, "jp")
                && "JP".equals(variant)) {
            extensions = LocaleExtensions.CALENDAR_JAPANESE;
        } else if (LocaleUtils.caseIgnoreMatch(language, "th")
                && script.length() == 0
                && LocaleUtils.caseIgnoreMatch(country, "th")
                && "TH".equals(variant)) {
            extensions = LocaleExtensions.NUMBER_THAI;
        }
        return extensions;
    }
    private static class LocaleNameGetter
        implements LocaleServiceProviderPool.LocalizedObjectGetter<LocaleNameProvider, String> {
        private static final LocaleNameGetter INSTANCE = new LocaleNameGetter();
        public String getObject(LocaleNameProvider localeNameProvider,
                                Locale locale,
                                String key,
                                Object... params) {
            assert params.length == 2;
            int type = (Integer)params[0];
            String code = (String)params[1];
            switch(type) {
            case DISPLAY_LANGUAGE:
                return localeNameProvider.getDisplayLanguage(code, locale);
            case DISPLAY_COUNTRY:
                return localeNameProvider.getDisplayCountry(code, locale);
            case DISPLAY_VARIANT:
                return localeNameProvider.getDisplayVariant(code, locale);
            case DISPLAY_SCRIPT:
                return localeNameProvider.getDisplayScript(code, locale);
            default:
                assert false; 
            }
            return null;
        }
    }
    public enum Category {
        DISPLAY("user.language.display",
                "user.script.display",
                "user.country.display",
                "user.variant.display"),
        FORMAT("user.language.format",
               "user.script.format",
               "user.country.format",
               "user.variant.format");
        Category(String languageKey, String scriptKey, String countryKey, String variantKey) {
            this.languageKey = languageKey;
            this.scriptKey = scriptKey;
            this.countryKey = countryKey;
            this.variantKey = variantKey;
        }
        final String languageKey;
        final String scriptKey;
        final String countryKey;
        final String variantKey;
    }
    public static final class Builder {
        private final InternalLocaleBuilder localeBuilder;
        public Builder() {
            localeBuilder = new InternalLocaleBuilder();
        }
        public Builder setLocale(Locale locale) {
            try {
                localeBuilder.setLocale(locale.baseLocale, locale.localeExtensions);
            } catch (LocaleSyntaxException e) {
                throw new IllformedLocaleException(e.getMessage(), e.getErrorIndex());
            }
            return this;
        }
        public Builder setLanguageTag(String languageTag) {
            ParseStatus sts = new ParseStatus();
            LanguageTag tag = LanguageTag.parse(languageTag, sts);
            if (sts.isError()) {
                throw new IllformedLocaleException(sts.getErrorMessage(), sts.getErrorIndex());
            }
            localeBuilder.setLanguageTag(tag);
            return this;
        }
        public Builder setLanguage(String language) {
            try {
                localeBuilder.setLanguage(language);
            } catch (LocaleSyntaxException e) {
                throw new IllformedLocaleException(e.getMessage(), e.getErrorIndex());
            }
            return this;
        }
        public Builder setScript(String script) {
            try {
                localeBuilder.setScript(script);
            } catch (LocaleSyntaxException e) {
                throw new IllformedLocaleException(e.getMessage(), e.getErrorIndex());
            }
            return this;
        }
        public Builder setRegion(String region) {
            try {
                localeBuilder.setRegion(region);
            } catch (LocaleSyntaxException e) {
                throw new IllformedLocaleException(e.getMessage(), e.getErrorIndex());
            }
            return this;
        }
        public Builder setVariant(String variant) {
            try {
                localeBuilder.setVariant(variant);
            } catch (LocaleSyntaxException e) {
                throw new IllformedLocaleException(e.getMessage(), e.getErrorIndex());
            }
            return this;
        }
        public Builder setExtension(char key, String value) {
            try {
                localeBuilder.setExtension(key, value);
            } catch (LocaleSyntaxException e) {
                throw new IllformedLocaleException(e.getMessage(), e.getErrorIndex());
            }
            return this;
        }
        public Builder setUnicodeLocaleKeyword(String key, String type) {
            try {
                localeBuilder.setUnicodeLocaleKeyword(key, type);
            } catch (LocaleSyntaxException e) {
                throw new IllformedLocaleException(e.getMessage(), e.getErrorIndex());
            }
            return this;
        }
        public Builder addUnicodeLocaleAttribute(String attribute) {
            try {
                localeBuilder.addUnicodeLocaleAttribute(attribute);
            } catch (LocaleSyntaxException e) {
                throw new IllformedLocaleException(e.getMessage(), e.getErrorIndex());
            }
            return this;
        }
        public Builder removeUnicodeLocaleAttribute(String attribute) {
            try {
                localeBuilder.removeUnicodeLocaleAttribute(attribute);
            } catch (LocaleSyntaxException e) {
                throw new IllformedLocaleException(e.getMessage(), e.getErrorIndex());
            }
            return this;
        }
        public Builder clear() {
            localeBuilder.clear();
            return this;
        }
        public Builder clearExtensions() {
            localeBuilder.clearExtensions();
            return this;
        }
        public Locale build() {
            BaseLocale baseloc = localeBuilder.getBaseLocale();
            LocaleExtensions extensions = localeBuilder.getLocaleExtensions();
            if (extensions == null && baseloc.getVariant().length() > 0) {
                extensions = getCompatibilityExtensions(baseloc.getLanguage(), baseloc.getScript(),
                        baseloc.getRegion(), baseloc.getVariant());
            }
            return Locale.getInstance(baseloc, extensions);
        }
    }
}
