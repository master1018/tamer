public class DateFormatSymbols implements Serializable, Cloneable {
    public DateFormatSymbols()
    {
        initializeData(Locale.getDefault(Locale.Category.FORMAT));
    }
    public DateFormatSymbols(Locale locale)
    {
        initializeData(locale);
    }
    String eras[] = null;
    String months[] = null;
    String shortMonths[] = null;
    String weekdays[] = null;
    String shortWeekdays[] = null;
    String ampms[] = null;
    String zoneStrings[][] = null;
    transient boolean isZoneStringsSet = false;
    static final String  patternChars = "GyMdkHmsSEDFwWahKzZYuX";
    static final int PATTERN_ERA                  =  0; 
    static final int PATTERN_YEAR                 =  1; 
    static final int PATTERN_MONTH                =  2; 
    static final int PATTERN_DAY_OF_MONTH         =  3; 
    static final int PATTERN_HOUR_OF_DAY1         =  4; 
    static final int PATTERN_HOUR_OF_DAY0         =  5; 
    static final int PATTERN_MINUTE               =  6; 
    static final int PATTERN_SECOND               =  7; 
    static final int PATTERN_MILLISECOND          =  8; 
    static final int PATTERN_DAY_OF_WEEK          =  9; 
    static final int PATTERN_DAY_OF_YEAR          = 10; 
    static final int PATTERN_DAY_OF_WEEK_IN_MONTH = 11; 
    static final int PATTERN_WEEK_OF_YEAR         = 12; 
    static final int PATTERN_WEEK_OF_MONTH        = 13; 
    static final int PATTERN_AM_PM                = 14; 
    static final int PATTERN_HOUR1                = 15; 
    static final int PATTERN_HOUR0                = 16; 
    static final int PATTERN_ZONE_NAME            = 17; 
    static final int PATTERN_ZONE_VALUE           = 18; 
    static final int PATTERN_WEEK_YEAR            = 19; 
    static final int PATTERN_ISO_DAY_OF_WEEK      = 20; 
    static final int PATTERN_ISO_ZONE             = 21; 
    String  localPatternChars = null;
    Locale locale = null;
    static final long serialVersionUID = -5987973545549424702L;
    public static Locale[] getAvailableLocales() {
        LocaleServiceProviderPool pool=
            LocaleServiceProviderPool.getPool(DateFormatSymbolsProvider.class);
        return pool.getAvailableLocales();
    }
    public static final DateFormatSymbols getInstance() {
        return getInstance(Locale.getDefault(Locale.Category.FORMAT));
    }
    public static final DateFormatSymbols getInstance(Locale locale) {
        DateFormatSymbols dfs = getProviderInstance(locale);
        if (dfs != null) {
            return dfs;
        }
        return (DateFormatSymbols) getCachedInstance(locale).clone();
    }
    static final DateFormatSymbols getInstanceRef(Locale locale) {
        DateFormatSymbols dfs = getProviderInstance(locale);
        if (dfs != null) {
            return dfs;
        }
        return getCachedInstance(locale);
    }
    private static DateFormatSymbols getProviderInstance(Locale locale) {
        DateFormatSymbols providersInstance = null;
        LocaleServiceProviderPool pool =
            LocaleServiceProviderPool.getPool(DateFormatSymbolsProvider.class);
        if (pool.hasProviders()) {
            providersInstance = pool.getLocalizedObject(
                                    DateFormatSymbolsGetter.INSTANCE, locale);
        }
        return providersInstance;
    }
    private static DateFormatSymbols getCachedInstance(Locale locale) {
        SoftReference<DateFormatSymbols> ref = cachedInstances.get(locale);
        DateFormatSymbols dfs = null;
        if (ref == null || (dfs = ref.get()) == null) {
            dfs = new DateFormatSymbols(locale);
            ref = new SoftReference<DateFormatSymbols>(dfs);
            SoftReference<DateFormatSymbols> x = cachedInstances.putIfAbsent(locale, ref);
            if (x != null) {
                DateFormatSymbols y = x.get();
                if (y != null) {
                    dfs = y;
                } else {
                    cachedInstances.put(locale, ref);
                }
            }
        }
        return dfs;
    }
    public String[] getEras() {
        return Arrays.copyOf(eras, eras.length);
    }
    public void setEras(String[] newEras) {
        eras = Arrays.copyOf(newEras, newEras.length);
    }
    public String[] getMonths() {
        return Arrays.copyOf(months, months.length);
    }
    public void setMonths(String[] newMonths) {
        months = Arrays.copyOf(newMonths, newMonths.length);
    }
    public String[] getShortMonths() {
        return Arrays.copyOf(shortMonths, shortMonths.length);
    }
    public void setShortMonths(String[] newShortMonths) {
        shortMonths = Arrays.copyOf(newShortMonths, newShortMonths.length);
    }
    public String[] getWeekdays() {
        return Arrays.copyOf(weekdays, weekdays.length);
    }
    public void setWeekdays(String[] newWeekdays) {
        weekdays = Arrays.copyOf(newWeekdays, newWeekdays.length);
    }
    public String[] getShortWeekdays() {
        return Arrays.copyOf(shortWeekdays, shortWeekdays.length);
    }
    public void setShortWeekdays(String[] newShortWeekdays) {
        shortWeekdays = Arrays.copyOf(newShortWeekdays, newShortWeekdays.length);
    }
    public String[] getAmPmStrings() {
        return Arrays.copyOf(ampms, ampms.length);
    }
    public void setAmPmStrings(String[] newAmpms) {
        ampms = Arrays.copyOf(newAmpms, newAmpms.length);
    }
    public String[][] getZoneStrings() {
        return getZoneStringsImpl(true);
    }
    public void setZoneStrings(String[][] newZoneStrings) {
        String[][] aCopy = new String[newZoneStrings.length][];
        for (int i = 0; i < newZoneStrings.length; ++i) {
            int len = newZoneStrings[i].length;
            if (len < 5) {
                throw new IllegalArgumentException();
            }
            aCopy[i] = Arrays.copyOf(newZoneStrings[i], len);
        }
        zoneStrings = aCopy;
        isZoneStringsSet = true;
    }
    public String getLocalPatternChars() {
        return localPatternChars;
    }
    public void setLocalPatternChars(String newLocalPatternChars) {
        localPatternChars = newLocalPatternChars.toString();
    }
    public Object clone()
    {
        try
        {
            DateFormatSymbols other = (DateFormatSymbols)super.clone();
            copyMembers(this, other);
            return other;
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }
    public int hashCode() {
        int hashcode = 0;
        String[][] zoneStrings = getZoneStringsWrapper();
        for (int index = 0; index < zoneStrings[0].length; ++index)
            hashcode ^= zoneStrings[0][index].hashCode();
        return hashcode;
    }
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        DateFormatSymbols that = (DateFormatSymbols) obj;
        return (Arrays.equals(eras, that.eras)
                && Arrays.equals(months, that.months)
                && Arrays.equals(shortMonths, that.shortMonths)
                && Arrays.equals(weekdays, that.weekdays)
                && Arrays.equals(shortWeekdays, that.shortWeekdays)
                && Arrays.equals(ampms, that.ampms)
                && Arrays.deepEquals(getZoneStringsWrapper(), that.getZoneStringsWrapper())
                && ((localPatternChars != null
                  && localPatternChars.equals(that.localPatternChars))
                 || (localPatternChars == null
                  && that.localPatternChars == null)));
    }
    static final int millisPerHour = 60*60*1000;
    private static final ConcurrentMap<Locale, SoftReference<DateFormatSymbols>> cachedInstances
        = new ConcurrentHashMap<Locale, SoftReference<DateFormatSymbols>>(3);
    private void initializeData(Locale desiredLocale) {
        locale = desiredLocale;
        SoftReference<DateFormatSymbols> ref = cachedInstances.get(locale);
        DateFormatSymbols dfs;
        if (ref != null && (dfs = ref.get()) != null) {
            copyMembers(dfs, this);
            return;
        }
        ResourceBundle resource = LocaleData.getDateFormatData(locale);
        eras = resource.getStringArray("Eras");
        months = resource.getStringArray("MonthNames");
        shortMonths = resource.getStringArray("MonthAbbreviations");
        ampms = resource.getStringArray("AmPmMarkers");
        localPatternChars = resource.getString("DateTimePatternChars");
        weekdays = toOneBasedArray(resource.getStringArray("DayNames"));
        shortWeekdays = toOneBasedArray(resource.getStringArray("DayAbbreviations"));
    }
    private static String[] toOneBasedArray(String[] src) {
        int len = src.length;
        String[] dst = new String[len + 1];
        dst[0] = "";
        for (int i = 0; i < len; i++) {
            dst[i + 1] = src[i];
        }
        return dst;
    }
    final int getZoneIndex(String ID)
    {
        String[][] zoneStrings = getZoneStringsWrapper();
        for (int index=0; index<zoneStrings.length; index++)
        {
            if (ID.equals(zoneStrings[index][0])) return index;
        }
        return -1;
    }
    final String[][] getZoneStringsWrapper() {
        if (isSubclassObject()) {
            return getZoneStrings();
        } else {
            return getZoneStringsImpl(false);
        }
    }
    private final String[][] getZoneStringsImpl(boolean needsCopy) {
        if (zoneStrings == null) {
            zoneStrings = TimeZoneNameUtility.getZoneStrings(locale);
        }
        if (!needsCopy) {
            return zoneStrings;
        }
        int len = zoneStrings.length;
        String[][] aCopy = new String[len][];
        for (int i = 0; i < len; i++) {
            aCopy[i] = Arrays.copyOf(zoneStrings[i], zoneStrings[i].length);
        }
        return aCopy;
    }
    private final boolean isSubclassObject() {
        return !getClass().getName().equals("java.text.DateFormatSymbols");
    }
    private final void copyMembers(DateFormatSymbols src, DateFormatSymbols dst)
    {
        dst.eras = Arrays.copyOf(src.eras, src.eras.length);
        dst.months = Arrays.copyOf(src.months, src.months.length);
        dst.shortMonths = Arrays.copyOf(src.shortMonths, src.shortMonths.length);
        dst.weekdays = Arrays.copyOf(src.weekdays, src.weekdays.length);
        dst.shortWeekdays = Arrays.copyOf(src.shortWeekdays, src.shortWeekdays.length);
        dst.ampms = Arrays.copyOf(src.ampms, src.ampms.length);
        if (src.zoneStrings != null) {
            dst.zoneStrings = src.getZoneStringsImpl(true);
        } else {
            dst.zoneStrings = null;
        }
        dst.localPatternChars = src.localPatternChars;
    }
    private void writeObject(ObjectOutputStream stream) throws IOException {
        if (zoneStrings == null) {
            zoneStrings = TimeZoneNameUtility.getZoneStrings(locale);
        }
        stream.defaultWriteObject();
    }
    private static class DateFormatSymbolsGetter
        implements LocaleServiceProviderPool.LocalizedObjectGetter<DateFormatSymbolsProvider,
                                                                   DateFormatSymbols> {
        private static final DateFormatSymbolsGetter INSTANCE =
            new DateFormatSymbolsGetter();
        public DateFormatSymbols getObject(DateFormatSymbolsProvider dateFormatSymbolsProvider,
                                Locale locale,
                                String key,
                                Object... params) {
            assert params.length == 0;
            return dateFormatSymbolsProvider.getInstance(locale);
        }
    }
}
