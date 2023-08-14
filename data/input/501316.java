public class LocaleCache {
    private static final ThreadLocalCache<LocaleCache> cache = new ThreadLocalCache<LocaleCache>();
    private NumberFormat numberFormat = null;
    private final Locale locale;
    private LocaleCache(Locale locale) {
        this.locale = locale;
    }
    private static LocaleCache getLocaleCache(Locale locale) {
        LocaleCache lc = cache.get();
        if (lc == null || !lc.locale.equals(locale)) {
            lc = new LocaleCache(locale);
            cache.set(lc);
        }
        return lc;
    }
    public static NumberFormat getNumberFormat(Locale locale) {
        LocaleCache lc = getLocaleCache(locale);
        if (lc.numberFormat == null) {
            lc.numberFormat = NumberFormat.getInstance(locale);
        }
        return (NumberFormat) lc.numberFormat.clone();
    }
}
