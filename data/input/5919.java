public final class LocaleServiceProviderPool {
    private static Map<Class, LocaleServiceProviderPool> poolOfPools =
        new ConcurrentHashMap<Class, LocaleServiceProviderPool>();
    private Set<LocaleServiceProvider> providers =
        new LinkedHashSet<LocaleServiceProvider>();
    private Map<Locale, LocaleServiceProvider> providersCache =
        new ConcurrentHashMap<Locale, LocaleServiceProvider>();
    private Set<Locale> availableLocales = null;
    private static volatile List<Locale> availableJRELocales = null;
    private Set<Locale> providerLocales = null;
    private static Locale locale_ja_JP_JP = new Locale("ja", "JP", "JP");
    private static Locale locale_th_TH_TH = new Locale("th", "TH", "TH");
    public static LocaleServiceProviderPool getPool(Class<? extends LocaleServiceProvider> providerClass) {
        LocaleServiceProviderPool pool = poolOfPools.get(providerClass);
        if (pool == null) {
            LocaleServiceProviderPool newPool =
                new LocaleServiceProviderPool(providerClass);
            pool = poolOfPools.put(providerClass, newPool);
            if (pool == null) {
                pool = newPool;
            }
        }
        return pool;
    }
    private LocaleServiceProviderPool (final Class<? extends LocaleServiceProvider> c) {
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
                public Object run() {
                    for (LocaleServiceProvider provider : ServiceLoader.loadInstalled(c)) {
                        providers.add(provider);
                    }
                    return null;
                }
            });
        }  catch (PrivilegedActionException e) {
            config(e.toString());
        }
    }
    private static void config(String message) {
        PlatformLogger logger = PlatformLogger.getLogger("sun.util.LocaleServiceProviderPool");
        logger.config(message);
    }
    private static class AllAvailableLocales {
        static final Locale[] allAvailableLocales;
        static {
            Class[] providerClasses = {
                java.text.spi.BreakIteratorProvider.class,
                java.text.spi.CollatorProvider.class,
                java.text.spi.DateFormatProvider.class,
                java.text.spi.DateFormatSymbolsProvider.class,
                java.text.spi.DecimalFormatSymbolsProvider.class,
                java.text.spi.NumberFormatProvider.class,
                java.util.spi.CurrencyNameProvider.class,
                java.util.spi.LocaleNameProvider.class,
                java.util.spi.TimeZoneNameProvider.class };
            Locale[] allLocales = LocaleData.getAvailableLocales();
            Set<Locale> all = new HashSet<Locale>(allLocales.length);
            for (Locale locale : allLocales) {
                all.add(getLookupLocale(locale));
            }
            for (Class providerClass : providerClasses) {
                LocaleServiceProviderPool pool =
                    LocaleServiceProviderPool.getPool(providerClass);
                all.addAll(pool.getProviderLocales());
            }
            allAvailableLocales = all.toArray(new Locale[0]);
        }
    }
    public static Locale[] getAllAvailableLocales() {
        return AllAvailableLocales.allAvailableLocales.clone();
    }
    public synchronized Locale[] getAvailableLocales() {
        if (availableLocales == null) {
            availableLocales = new HashSet<Locale>(getJRELocales());
            if (hasProviders()) {
                availableLocales.addAll(getProviderLocales());
            }
        }
        Locale[] tmp = new Locale[availableLocales.size()];
        availableLocales.toArray(tmp);
        return tmp;
    }
    private synchronized Set<Locale> getProviderLocales() {
        if (providerLocales == null) {
            providerLocales = new HashSet<Locale>();
            if (hasProviders()) {
                for (LocaleServiceProvider lsp : providers) {
                    Locale[] locales = lsp.getAvailableLocales();
                    for (Locale locale: locales) {
                        providerLocales.add(getLookupLocale(locale));
                    }
                }
            }
        }
        return providerLocales;
    }
    public boolean hasProviders() {
        return !providers.isEmpty();
    }
    private List<Locale> getJRELocales() {
        if (availableJRELocales == null) {
            synchronized (LocaleServiceProviderPool.class) {
                if (availableJRELocales == null) {
                    Locale[] allLocales = LocaleData.getAvailableLocales();
                    availableJRELocales = new ArrayList<Locale>(allLocales.length);
                    for (Locale locale : allLocales) {
                        availableJRELocales.add(getLookupLocale(locale));
                    }
                }
            }
        }
        return availableJRELocales;
    }
    private boolean isJRESupported(Locale locale) {
        List<Locale> locales = getJRELocales();
        return locales.contains(getLookupLocale(locale));
    }
    public <P, S> S getLocalizedObject(LocalizedObjectGetter<P, S> getter,
                                     Locale locale,
                                     Object... params) {
        return getLocalizedObjectImpl(getter, locale, true, null, null, null, params);
    }
    public <P, S> S getLocalizedObject(LocalizedObjectGetter<P, S> getter,
                                     Locale locale,
                                     OpenListResourceBundle bundle,
                                     String key,
                                     Object... params) {
        return getLocalizedObjectImpl(getter, locale, false, null, bundle, key, params);
    }
    public <P, S> S getLocalizedObject(LocalizedObjectGetter<P, S> getter,
                                     Locale locale,
                                     String bundleKey,
                                     OpenListResourceBundle bundle,
                                     String key,
                                     Object... params) {
        return getLocalizedObjectImpl(getter, locale, false, bundleKey, bundle, key, params);
    }
    private <P, S> S getLocalizedObjectImpl(LocalizedObjectGetter<P, S> getter,
                                     Locale locale,
                                     boolean isObjectProvider,
                                     String bundleKey,
                                     OpenListResourceBundle bundle,
                                     String key,
                                     Object... params) {
        if (hasProviders()) {
            if (bundleKey == null) {
                bundleKey = key;
            }
            Locale bundleLocale = (bundle != null ? bundle.getLocale() : null);
            List<Locale> lookupLocales = getLookupLocales(locale);
            P lsp;
            S providersObj = null;
            Set<Locale> provLoc = getProviderLocales();
            for (int i = 0; i < lookupLocales.size(); i++) {
                Locale current = lookupLocales.get(i);
                if (bundleLocale != null) {
                    if (current.equals(bundleLocale)) {
                        break;
                    }
                } else {
                    if (isJRESupported(current)) {
                        break;
                    }
                }
                if (provLoc.contains(current)) {
                    lsp = (P)findProvider(current);
                    if (lsp != null) {
                        providersObj = getter.getObject(lsp, locale, key, params);
                        if (providersObj != null) {
                            return providersObj;
                        } else if (isObjectProvider) {
                            config(
                                "A locale sensitive service provider returned null for a localized objects,  which should not happen.  provider: " + lsp + " locale: " + locale);
                        }
                    }
                }
            }
            while (bundle != null) {
                bundleLocale = bundle.getLocale();
                if (bundle.handleGetKeys().contains(bundleKey)) {
                    return null;
                } else {
                    lsp = (P)findProvider(bundleLocale);
                    if (lsp != null) {
                        providersObj = getter.getObject(lsp, locale, key, params);
                        if (providersObj != null) {
                            return providersObj;
                        }
                    }
                }
                bundle = bundle.getParent();
            }
        }
        return null;
    }
    private LocaleServiceProvider findProvider(Locale locale) {
        if (!hasProviders()) {
            return null;
        }
        if (providersCache.containsKey(locale)) {
            LocaleServiceProvider provider = providersCache.get(locale);
            if (provider != NullProvider.INSTANCE) {
                return provider;
            }
        } else {
            for (LocaleServiceProvider lsp : providers) {
                Locale[] locales = lsp.getAvailableLocales();
                for (Locale available: locales) {
                    available = getLookupLocale(available);
                    if (locale.equals(available)) {
                        LocaleServiceProvider providerInCache =
                            providersCache.put(locale, lsp);
                        return (providerInCache != null ?
                                providerInCache :
                                lsp);
                    }
                }
            }
            providersCache.put(locale, NullProvider.INSTANCE);
        }
        return null;
    }
    private static List<Locale> getLookupLocales(Locale locale) {
        List<Locale> lookupLocales = new Control(){}.getCandidateLocales("", locale);
        return lookupLocales;
    }
    private static Locale getLookupLocale(Locale locale) {
        Locale lookupLocale = locale;
        Set<Character> extensions = locale.getExtensionKeys();
        if (!extensions.isEmpty()
                && !locale.equals(locale_ja_JP_JP)
                && !locale.equals(locale_th_TH_TH)) {
            Builder locbld = new Builder();
            try {
                locbld.setLocale(locale);
                locbld.clearExtensions();
                lookupLocale = locbld.build();
            } catch (IllformedLocaleException e) {
                config("A locale(" + locale + ") has non-empty extensions, but has illformed fields.");
                lookupLocale = new Locale(locale.getLanguage(), locale.getCountry(), locale.getVariant());
            }
        }
        return lookupLocale;
    }
    private static class NullProvider extends LocaleServiceProvider {
        private static final NullProvider INSTANCE = new NullProvider();
        public Locale[] getAvailableLocales() {
            throw new RuntimeException("Should not get called.");
        }
    }
    public interface LocalizedObjectGetter<P, S> {
        public S getObject(P lsp,
                                Locale locale,
                                String key,
                                Object... params);
    }
}
