public abstract class BreakIterator implements Cloneable
{
    protected BreakIterator()
    {
    }
    public Object clone()
    {
        try {
            return super.clone();
        }
        catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }
    public static final int DONE = -1;
    public abstract int first();
    public abstract int last();
    public abstract int next(int n);
    public abstract int next();
    public abstract int previous();
    public abstract int following(int offset);
    public int preceding(int offset) {
        int pos = following(offset);
        while (pos >= offset && pos != DONE)
            pos = previous();
        return pos;
    }
    public boolean isBoundary(int offset) {
        if (offset == 0) {
            return true;
        }
        int boundary = following(offset - 1);
        if (boundary == DONE) {
            throw new IllegalArgumentException();
        }
        return boundary == offset;
    }
    public abstract int current();
    public abstract CharacterIterator getText();
    public void setText(String newText)
    {
        setText(new StringCharacterIterator(newText));
    }
    public abstract void setText(CharacterIterator newText);
    private static final int CHARACTER_INDEX = 0;
    private static final int WORD_INDEX = 1;
    private static final int LINE_INDEX = 2;
    private static final int SENTENCE_INDEX = 3;
    private static final SoftReference[] iterCache = new SoftReference[4];
    public static BreakIterator getWordInstance()
    {
        return getWordInstance(Locale.getDefault());
    }
    public static BreakIterator getWordInstance(Locale locale)
    {
        return getBreakInstance(locale,
                                WORD_INDEX,
                                "WordData",
                                "WordDictionary");
    }
    public static BreakIterator getLineInstance()
    {
        return getLineInstance(Locale.getDefault());
    }
    public static BreakIterator getLineInstance(Locale locale)
    {
        return getBreakInstance(locale,
                                LINE_INDEX,
                                "LineData",
                                "LineDictionary");
    }
    public static BreakIterator getCharacterInstance()
    {
        return getCharacterInstance(Locale.getDefault());
    }
    public static BreakIterator getCharacterInstance(Locale locale)
    {
        return getBreakInstance(locale,
                                CHARACTER_INDEX,
                                "CharacterData",
                                "CharacterDictionary");
    }
    public static BreakIterator getSentenceInstance()
    {
        return getSentenceInstance(Locale.getDefault());
    }
    public static BreakIterator getSentenceInstance(Locale locale)
    {
        return getBreakInstance(locale,
                                SENTENCE_INDEX,
                                "SentenceData",
                                "SentenceDictionary");
    }
    private static BreakIterator getBreakInstance(Locale locale,
                                                  int type,
                                                  String dataName,
                                                  String dictionaryName) {
        if (iterCache[type] != null) {
            BreakIteratorCache cache = (BreakIteratorCache) iterCache[type].get();
            if (cache != null) {
                if (cache.getLocale().equals(locale)) {
                    return cache.createBreakInstance();
                }
            }
        }
        BreakIterator result = createBreakInstance(locale,
                                                   type,
                                                   dataName,
                                                   dictionaryName);
        BreakIteratorCache cache = new BreakIteratorCache(locale, result);
        iterCache[type] = new SoftReference(cache);
        return result;
    }
    private static ResourceBundle getBundle(final String baseName, final Locale locale) {
         return (ResourceBundle) AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                return ResourceBundle.getBundle(baseName, locale);
            }
        });
    }
    private static BreakIterator createBreakInstance(Locale locale,
                                                     int type,
                                                     String dataName,
                                                     String dictionaryName) {
        LocaleServiceProviderPool pool =
            LocaleServiceProviderPool.getPool(BreakIteratorProvider.class);
        if (pool.hasProviders()) {
            BreakIterator providersInstance = pool.getLocalizedObject(
                                                    BreakIteratorGetter.INSTANCE,
                                                    locale, type);
            if (providersInstance != null) {
                return providersInstance;
            }
        }
        ResourceBundle bundle = getBundle(
                        "sun.text.resources.BreakIteratorInfo", locale);
        String[] classNames = bundle.getStringArray("BreakIteratorClasses");
        String dataFile = bundle.getString(dataName);
        try {
            if (classNames[type].equals("RuleBasedBreakIterator")) {
                return new RuleBasedBreakIterator(dataFile);
            }
            else if (classNames[type].equals("DictionaryBasedBreakIterator")) {
                String dictionaryFile = bundle.getString(dictionaryName);
                return new DictionaryBasedBreakIterator(dataFile, dictionaryFile);
            }
            else {
                throw new IllegalArgumentException("Invalid break iterator class \"" +
                                classNames[type] + "\"");
            }
        }
        catch (Exception e) {
            throw new InternalError(e.toString());
        }
    }
    public static synchronized Locale[] getAvailableLocales()
    {
        LocaleServiceProviderPool pool =
            LocaleServiceProviderPool.getPool(BreakIteratorProvider.class);
        return pool.getAvailableLocales();
    }
    private static final class BreakIteratorCache {
        private BreakIterator iter;
        private Locale locale;
        BreakIteratorCache(Locale locale, BreakIterator iter) {
            this.locale = locale;
            this.iter = (BreakIterator) iter.clone();
        }
        Locale getLocale() {
            return locale;
        }
        BreakIterator createBreakInstance() {
            return (BreakIterator) iter.clone();
        }
    }
    static long getLong(byte[] buf, int offset) {
        long num = buf[offset]&0xFF;
        for (int i = 1; i < 8; i++) {
            num = num<<8 | (buf[offset+i]&0xFF);
        }
        return num;
    }
    static int getInt(byte[] buf, int offset) {
        int num = buf[offset]&0xFF;
        for (int i = 1; i < 4; i++) {
            num = num<<8 | (buf[offset+i]&0xFF);
        }
        return num;
    }
    static short getShort(byte[] buf, int offset) {
        short num = (short)(buf[offset]&0xFF);
        num = (short)(num<<8 | (buf[offset+1]&0xFF));
        return num;
    }
    private static class BreakIteratorGetter
        implements LocaleServiceProviderPool.LocalizedObjectGetter<BreakIteratorProvider, BreakIterator> {
        private static final BreakIteratorGetter INSTANCE =
            new BreakIteratorGetter();
        public BreakIterator getObject(BreakIteratorProvider breakIteratorProvider,
                                Locale locale,
                                String key,
                                Object... params) {
            assert params.length == 1;
            switch ((Integer)params[0]) {
            case CHARACTER_INDEX:
                return breakIteratorProvider.getCharacterInstance(locale);
            case WORD_INDEX:
                return breakIteratorProvider.getWordInstance(locale);
            case LINE_INDEX:
                return breakIteratorProvider.getLineInstance(locale);
            case SENTENCE_INDEX:
                return breakIteratorProvider.getSentenceInstance(locale);
            default:
                assert false : "should not happen";
            }
            return null;
        }
    }
}
