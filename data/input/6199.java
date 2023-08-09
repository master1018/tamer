public class LanguageTag {
    public static final String SEP = "-";
    public static final String PRIVATEUSE = "x";
    public static final String UNDETERMINED = "und";
    public static final String PRIVUSE_VARIANT_PREFIX = "lvariant";
    private String language = "";      
    private String script = "";        
    private String region = "";        
    private String privateuse = "";    
    private List<String> extlangs = Collections.emptyList();   
    private List<String> variants = Collections.emptyList();   
    private List<String> extensions = Collections.emptyList(); 
    private static final Map<String, String[]> GRANDFATHERED = new HashMap<>();
    static {
        final String[][] entries = {
            {"art-lojban",  "jbo"},
            {"cel-gaulish", "xtg-x-cel-gaulish"},   
            {"en-GB-oed",   "en-GB-x-oed"},         
            {"i-ami",       "ami"},
            {"i-bnn",       "bnn"},
            {"i-default",   "en-x-i-default"},      
            {"i-enochian",  "und-x-i-enochian"},    
            {"i-hak",       "hak"},
            {"i-klingon",   "tlh"},
            {"i-lux",       "lb"},
            {"i-mingo",     "see-x-i-mingo"},       
            {"i-navajo",    "nv"},
            {"i-pwn",       "pwn"},
            {"i-tao",       "tao"},
            {"i-tay",       "tay"},
            {"i-tsu",       "tsu"},
            {"no-bok",      "nb"},
            {"no-nyn",      "nn"},
            {"sgn-BE-FR",   "sfb"},
            {"sgn-BE-NL",   "vgt"},
            {"sgn-CH-DE",   "sgg"},
            {"zh-guoyu",    "cmn"},
            {"zh-hakka",    "hak"},
            {"zh-min",      "nan-x-zh-min"},        
            {"zh-min-nan",  "nan"},
            {"zh-xiang",    "hsn"},
        };
        for (String[] e : entries) {
            GRANDFATHERED.put(LocaleUtils.toLowerString(e[0]), e);
        }
    }
    private LanguageTag() {
    }
    public static LanguageTag parse(String languageTag, ParseStatus sts) {
        if (sts == null) {
            sts = new ParseStatus();
        } else {
            sts.reset();
        }
        StringTokenIterator itr;
        String[] gfmap = GRANDFATHERED.get(LocaleUtils.toLowerString(languageTag));
        if (gfmap != null) {
            itr = new StringTokenIterator(gfmap[1], SEP);
        } else {
            itr = new StringTokenIterator(languageTag, SEP);
        }
        LanguageTag tag = new LanguageTag();
        if (tag.parseLanguage(itr, sts)) {
            tag.parseExtlangs(itr, sts);
            tag.parseScript(itr, sts);
            tag.parseRegion(itr, sts);
            tag.parseVariants(itr, sts);
            tag.parseExtensions(itr, sts);
        }
        tag.parsePrivateuse(itr, sts);
        if (!itr.isDone() && !sts.isError()) {
            String s = itr.current();
            sts.errorIndex = itr.currentStart();
            if (s.length() == 0) {
                sts.errorMsg = "Empty subtag";
            } else {
                sts.errorMsg = "Invalid subtag: " + s;
            }
        }
        return tag;
    }
    private boolean parseLanguage(StringTokenIterator itr, ParseStatus sts) {
        if (itr.isDone() || sts.isError()) {
            return false;
        }
        boolean found = false;
        String s = itr.current();
        if (isLanguage(s)) {
            found = true;
            language = s;
            sts.parseLength = itr.currentEnd();
            itr.next();
        }
        return found;
    }
    private boolean parseExtlangs(StringTokenIterator itr, ParseStatus sts) {
        if (itr.isDone() || sts.isError()) {
            return false;
        }
        boolean found = false;
        while (!itr.isDone()) {
            String s = itr.current();
            if (!isExtlang(s)) {
                break;
            }
            found = true;
            if (extlangs.isEmpty()) {
                extlangs = new ArrayList<>(3);
            }
            extlangs.add(s);
            sts.parseLength = itr.currentEnd();
            itr.next();
            if (extlangs.size() == 3) {
                break;
            }
        }
        return found;
    }
    private boolean parseScript(StringTokenIterator itr, ParseStatus sts) {
        if (itr.isDone() || sts.isError()) {
            return false;
        }
        boolean found = false;
        String s = itr.current();
        if (isScript(s)) {
            found = true;
            script = s;
            sts.parseLength = itr.currentEnd();
            itr.next();
        }
        return found;
    }
    private boolean parseRegion(StringTokenIterator itr, ParseStatus sts) {
        if (itr.isDone() || sts.isError()) {
            return false;
        }
        boolean found = false;
        String s = itr.current();
        if (isRegion(s)) {
            found = true;
            region = s;
            sts.parseLength = itr.currentEnd();
            itr.next();
        }
        return found;
    }
    private boolean parseVariants(StringTokenIterator itr, ParseStatus sts) {
        if (itr.isDone() || sts.isError()) {
            return false;
        }
        boolean found = false;
        while (!itr.isDone()) {
            String s = itr.current();
            if (!isVariant(s)) {
                break;
            }
            found = true;
            if (variants.isEmpty()) {
                variants = new ArrayList<>(3);
            }
            variants.add(s);
            sts.parseLength = itr.currentEnd();
            itr.next();
        }
        return found;
    }
    private boolean parseExtensions(StringTokenIterator itr, ParseStatus sts) {
        if (itr.isDone() || sts.isError()) {
            return false;
        }
        boolean found = false;
        while (!itr.isDone()) {
            String s = itr.current();
            if (isExtensionSingleton(s)) {
                int start = itr.currentStart();
                String singleton = s;
                StringBuilder sb = new StringBuilder(singleton);
                itr.next();
                while (!itr.isDone()) {
                    s = itr.current();
                    if (isExtensionSubtag(s)) {
                        sb.append(SEP).append(s);
                        sts.parseLength = itr.currentEnd();
                    } else {
                        break;
                    }
                    itr.next();
                }
                if (sts.parseLength <= start) {
                    sts.errorIndex = start;
                    sts.errorMsg = "Incomplete extension '" + singleton + "'";
                    break;
                }
                if (extensions.isEmpty()) {
                    extensions = new ArrayList<>(4);
                }
                extensions.add(sb.toString());
                found = true;
            } else {
                break;
            }
        }
        return found;
    }
    private boolean parsePrivateuse(StringTokenIterator itr, ParseStatus sts) {
        if (itr.isDone() || sts.isError()) {
            return false;
        }
        boolean found = false;
        String s = itr.current();
        if (isPrivateusePrefix(s)) {
            int start = itr.currentStart();
            StringBuilder sb = new StringBuilder(s);
            itr.next();
            while (!itr.isDone()) {
                s = itr.current();
                if (!isPrivateuseSubtag(s)) {
                    break;
                }
                sb.append(SEP).append(s);
                sts.parseLength = itr.currentEnd();
                itr.next();
            }
            if (sts.parseLength <= start) {
                sts.errorIndex = start;
                sts.errorMsg = "Incomplete privateuse";
            } else {
                privateuse = sb.toString();
                found = true;
            }
        }
        return found;
    }
    public static LanguageTag parseLocale(BaseLocale baseLocale, LocaleExtensions localeExtensions) {
        LanguageTag tag = new LanguageTag();
        String language = baseLocale.getLanguage();
        String script = baseLocale.getScript();
        String region = baseLocale.getRegion();
        String variant = baseLocale.getVariant();
        boolean hasSubtag = false;
        String privuseVar = null;   
        if (isLanguage(language)) {
            if (language.equals("iw")) {
                language = "he";
            } else if (language.equals("ji")) {
                language = "yi";
            } else if (language.equals("in")) {
                language = "id";
            }
            tag.language = language;
        }
        if (isScript(script)) {
            tag.script = canonicalizeScript(script);
            hasSubtag = true;
        }
        if (isRegion(region)) {
            tag.region = canonicalizeRegion(region);
            hasSubtag = true;
        }
        if (tag.language.equals("no") && tag.region.equals("NO") && variant.equals("NY")) {
            tag.language = "nn";
            variant = "";
        }
        if (variant.length() > 0) {
            List<String> variants = null;
            StringTokenIterator varitr = new StringTokenIterator(variant, BaseLocale.SEP);
            while (!varitr.isDone()) {
                String var = varitr.current();
                if (!isVariant(var)) {
                    break;
                }
                if (variants == null) {
                    variants = new ArrayList<>();
                }
                variants.add(var);  
                varitr.next();
            }
            if (variants != null) {
                tag.variants = variants;
                hasSubtag = true;
            }
            if (!varitr.isDone()) {
                StringBuilder buf = new StringBuilder();
                while (!varitr.isDone()) {
                    String prvv = varitr.current();
                    if (!isPrivateuseSubtag(prvv)) {
                        break;
                    }
                    if (buf.length() > 0) {
                        buf.append(SEP);
                    }
                    buf.append(prvv);
                    varitr.next();
                }
                if (buf.length() > 0) {
                    privuseVar = buf.toString();
                }
            }
        }
        List<String> extensions = null;
        String privateuse = null;
        if (localeExtensions != null) {
            Set<Character> locextKeys = localeExtensions.getKeys();
            for (Character locextKey : locextKeys) {
                Extension ext = localeExtensions.getExtension(locextKey);
                if (isPrivateusePrefixChar(locextKey)) {
                    privateuse = ext.getValue();
                } else {
                    if (extensions == null) {
                        extensions = new ArrayList<>();
                    }
                    extensions.add(locextKey.toString() + SEP + ext.getValue());
                }
            }
        }
        if (extensions != null) {
            tag.extensions = extensions;
            hasSubtag = true;
        }
        if (privuseVar != null) {
            if (privateuse == null) {
                privateuse = PRIVUSE_VARIANT_PREFIX + SEP + privuseVar;
            } else {
                privateuse = privateuse + SEP + PRIVUSE_VARIANT_PREFIX
                             + SEP + privuseVar.replace(BaseLocale.SEP, SEP);
            }
        }
        if (privateuse != null) {
            tag.privateuse = privateuse;
        }
        if (tag.language.length() == 0 && (hasSubtag || privateuse == null)) {
            tag.language = UNDETERMINED;
        }
        return tag;
    }
    public String getLanguage() {
        return language;
    }
    public List<String> getExtlangs() {
        if (extlangs.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(extlangs);
    }
    public String getScript() {
        return script;
    }
    public String getRegion() {
        return region;
    }
    public List<String> getVariants() {
        if (variants.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(variants);
    }
    public List<String> getExtensions() {
        if (extensions.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(extensions);
    }
    public String getPrivateuse() {
        return privateuse;
    }
    public static boolean isLanguage(String s) {
        int len = s.length();
        return (len >= 2) && (len <= 8) && LocaleUtils.isAlphaString(s);
    }
    public static boolean isExtlang(String s) {
        return (s.length() == 3) && LocaleUtils.isAlphaString(s);
    }
    public static boolean isScript(String s) {
        return (s.length() == 4) && LocaleUtils.isAlphaString(s);
    }
    public static boolean isRegion(String s) {
        return ((s.length() == 2) && LocaleUtils.isAlphaString(s))
                || ((s.length() == 3) && LocaleUtils.isNumericString(s));
    }
    public static boolean isVariant(String s) {
        int len = s.length();
        if (len >= 5 && len <= 8) {
            return LocaleUtils.isAlphaNumericString(s);
        }
        if (len == 4) {
            return LocaleUtils.isNumeric(s.charAt(0))
                    && LocaleUtils.isAlphaNumeric(s.charAt(1))
                    && LocaleUtils.isAlphaNumeric(s.charAt(2))
                    && LocaleUtils.isAlphaNumeric(s.charAt(3));
        }
        return false;
    }
    public static boolean isExtensionSingleton(String s) {
        return (s.length() == 1)
                && LocaleUtils.isAlphaString(s)
                && !LocaleUtils.caseIgnoreMatch(PRIVATEUSE, s);
    }
    public static boolean isExtensionSingletonChar(char c) {
        return isExtensionSingleton(String.valueOf(c));
    }
    public static boolean isExtensionSubtag(String s) {
        int len = s.length();
        return (len >= 2) && (len <= 8) && LocaleUtils.isAlphaNumericString(s);
    }
    public static boolean isPrivateusePrefix(String s) {
        return (s.length() == 1)
                && LocaleUtils.caseIgnoreMatch(PRIVATEUSE, s);
    }
    public static boolean isPrivateusePrefixChar(char c) {
        return (LocaleUtils.caseIgnoreMatch(PRIVATEUSE, String.valueOf(c)));
    }
    public static boolean isPrivateuseSubtag(String s) {
        int len = s.length();
        return (len >= 1) && (len <= 8) && LocaleUtils.isAlphaNumericString(s);
    }
    public static String canonicalizeLanguage(String s) {
        return LocaleUtils.toLowerString(s);
    }
    public static String canonicalizeExtlang(String s) {
        return LocaleUtils.toLowerString(s);
    }
    public static String canonicalizeScript(String s) {
        return LocaleUtils.toTitleString(s);
    }
    public static String canonicalizeRegion(String s) {
        return LocaleUtils.toUpperString(s);
    }
    public static String canonicalizeVariant(String s) {
        return LocaleUtils.toLowerString(s);
    }
    public static String canonicalizeExtension(String s) {
        return LocaleUtils.toLowerString(s);
    }
    public static String canonicalizeExtensionSingleton(String s) {
        return LocaleUtils.toLowerString(s);
    }
    public static String canonicalizeExtensionSubtag(String s) {
        return LocaleUtils.toLowerString(s);
    }
    public static String canonicalizePrivateuse(String s) {
        return LocaleUtils.toLowerString(s);
    }
    public static String canonicalizePrivateuseSubtag(String s) {
        return LocaleUtils.toLowerString(s);
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (language.length() > 0) {
            sb.append(language);
            for (String extlang : extlangs) {
                sb.append(SEP).append(extlang);
            }
            if (script.length() > 0) {
                sb.append(SEP).append(script);
            }
            if (region.length() > 0) {
                sb.append(SEP).append(region);
            }
            for (String variant : variants) {
                sb.append(SEP).append(variant);
            }
            for (String extension : extensions) {
                sb.append(SEP).append(extension);
            }
        }
        if (privateuse.length() > 0) {
            if (sb.length() > 0) {
                sb.append(SEP);
            }
            sb.append(privateuse);
        }
        return sb.toString();
    }
}
