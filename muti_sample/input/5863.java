public abstract class Collator
    implements java.util.Comparator<Object>, Cloneable
{
    public final static int PRIMARY = 0;
    public final static int SECONDARY = 1;
    public final static int TERTIARY = 2;
    public final static int IDENTICAL = 3;
    public final static int NO_DECOMPOSITION = 0;
    public final static int CANONICAL_DECOMPOSITION = 1;
    public final static int FULL_DECOMPOSITION = 2;
    public static synchronized Collator getInstance() {
        return getInstance(Locale.getDefault());
    }
    public static synchronized
    Collator getInstance(Locale desiredLocale)
    {
        Collator result = (Collator) cache.get(desiredLocale);
        if (result != null) {
                 return (Collator)result.clone();  
        }
        LocaleServiceProviderPool pool =
            LocaleServiceProviderPool.getPool(CollatorProvider.class);
        if (pool.hasProviders()) {
            Collator providersInstance = pool.getLocalizedObject(
                                            CollatorGetter.INSTANCE,
                                            desiredLocale,
                                            desiredLocale);
            if (providersInstance != null) {
                return providersInstance;
            }
        }
        String colString = "";
        try {
            ResourceBundle resource = LocaleData.getCollationData(desiredLocale);
            colString = resource.getString("Rule");
        } catch (MissingResourceException e) {
        }
        try
        {
            result = new RuleBasedCollator( CollationRules.DEFAULTRULES +
                                            colString,
                                            CANONICAL_DECOMPOSITION );
        }
        catch(ParseException foo)
        {
            try {
                result = new RuleBasedCollator( CollationRules.DEFAULTRULES );
            } catch (ParseException bar) {
            }
        }
        result.setDecomposition(NO_DECOMPOSITION);
        cache.put(desiredLocale,result);
        return (Collator)result.clone();
    }
    public abstract int compare(String source, String target);
    public int compare(Object o1, Object o2) {
    return compare((String)o1, (String)o2);
    }
    public abstract CollationKey getCollationKey(String source);
    public boolean equals(String source, String target)
    {
        return (compare(source, target) == Collator.EQUAL);
    }
    public synchronized int getStrength()
    {
        return strength;
    }
    public synchronized void setStrength(int newStrength) {
        if ((newStrength != PRIMARY) &&
            (newStrength != SECONDARY) &&
            (newStrength != TERTIARY) &&
            (newStrength != IDENTICAL))
            throw new IllegalArgumentException("Incorrect comparison level.");
        strength = newStrength;
    }
    public synchronized int getDecomposition()
    {
        return decmp;
    }
    public synchronized void setDecomposition(int decompositionMode) {
        if ((decompositionMode != NO_DECOMPOSITION) &&
            (decompositionMode != CANONICAL_DECOMPOSITION) &&
            (decompositionMode != FULL_DECOMPOSITION))
            throw new IllegalArgumentException("Wrong decomposition mode.");
        decmp = decompositionMode;
    }
    public static synchronized Locale[] getAvailableLocales() {
        LocaleServiceProviderPool pool =
            LocaleServiceProviderPool.getPool(CollatorProvider.class);
        return pool.getAvailableLocales();
    }
    public Object clone()
    {
        try {
            return (Collator)super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }
    public boolean equals(Object that)
    {
        if (this == that) return true;
        if (that == null) return false;
        if (getClass() != that.getClass()) return false;
        Collator other = (Collator) that;
        return ((strength == other.strength) &&
                (decmp == other.decmp));
    }
    abstract public int hashCode();
    protected Collator()
    {
        strength = TERTIARY;
        decmp = CANONICAL_DECOMPOSITION;
    }
    private int strength = 0;
    private int decmp = 0;
    private static SoftCache cache = new SoftCache();
    final static int LESS = -1;
    final static int EQUAL = 0;
    final static int GREATER = 1;
    private static class CollatorGetter
        implements LocaleServiceProviderPool.LocalizedObjectGetter<CollatorProvider, Collator> {
        private static final CollatorGetter INSTANCE = new CollatorGetter();
        public Collator getObject(CollatorProvider collatorProvider,
                                Locale locale,
                                String key,
                                Object... params) {
            assert params.length == 1;
            Collator result = collatorProvider.getInstance(locale);
            if (result != null) {
                cache.put((Locale)params[0], result);
                cache.put(locale, result);
                return (Collator)result.clone();
            }
            return null;
        }
    }
 }
