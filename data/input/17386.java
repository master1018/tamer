public class JavacMessages implements Messages {
    public static final Context.Key<JavacMessages> messagesKey =
        new Context.Key<JavacMessages>();
    public static JavacMessages instance(Context context) {
        JavacMessages instance = context.get(messagesKey);
        if (instance == null)
            instance = new JavacMessages(context);
        return instance;
    }
    private Map<Locale, SoftReference<List<ResourceBundle>>> bundleCache;
    private List<String> bundleNames;
    private Locale currentLocale;
    private List<ResourceBundle> currentBundles;
    public Locale getCurrentLocale() {
        return currentLocale;
    }
    public void setCurrentLocale(Locale locale) {
        if (locale == null) {
            locale = Locale.getDefault();
        }
        this.currentBundles = getBundles(locale);
        this.currentLocale = locale;
    }
    public JavacMessages(Context context) {
        this(defaultBundleName, context.get(Locale.class));
        context.put(messagesKey, this);
    }
    public JavacMessages(String bundleName) throws MissingResourceException {
        this(bundleName, null);
    }
    public JavacMessages(String bundleName, Locale locale) throws MissingResourceException {
        bundleNames = List.nil();
        bundleCache = new HashMap<Locale, SoftReference<List<ResourceBundle>>>();
        add(bundleName);
        setCurrentLocale(locale);
    }
    public JavacMessages() throws MissingResourceException {
        this(defaultBundleName);
    }
    public void add(String bundleName) throws MissingResourceException {
        bundleNames = bundleNames.prepend(bundleName);
        if (!bundleCache.isEmpty())
            bundleCache.clear();
        currentBundles = null;
    }
    public List<ResourceBundle> getBundles(Locale locale) {
        if (locale == currentLocale && currentBundles != null)
            return currentBundles;
        SoftReference<List<ResourceBundle>> bundles = bundleCache.get(locale);
        List<ResourceBundle> bundleList = bundles == null ? null : bundles.get();
        if (bundleList == null) {
            bundleList = List.nil();
            for (String bundleName : bundleNames) {
                try {
                    ResourceBundle rb = ResourceBundle.getBundle(bundleName, locale);
                    bundleList = bundleList.prepend(rb);
                } catch (MissingResourceException e) {
                    throw new InternalError("Cannot find javac resource bundle for locale " + locale);
                }
            }
            bundleCache.put(locale, new SoftReference<List<ResourceBundle>>(bundleList));
        }
        return bundleList;
    }
    public String getLocalizedString(String key, Object... args) {
        return getLocalizedString(currentLocale, key, args);
    }
    public String getLocalizedString(Locale l, String key, Object... args) {
        if (l == null)
            l = getCurrentLocale();
        return getLocalizedString(getBundles(l), key, args);
    }
    private static final String defaultBundleName =
        "com.sun.tools.javac.resources.compiler";
    private static ResourceBundle defaultBundle;
    private static JavacMessages defaultMessages;
    static String getDefaultLocalizedString(String key, Object... args) {
        return getLocalizedString(List.of(getDefaultBundle()), key, args);
    }
    @Deprecated
    static JavacMessages getDefaultMessages() {
        if (defaultMessages == null)
            defaultMessages = new JavacMessages(defaultBundleName);
        return defaultMessages;
    }
    public static ResourceBundle getDefaultBundle() {
        try {
            if (defaultBundle == null)
                defaultBundle = ResourceBundle.getBundle(defaultBundleName);
            return defaultBundle;
        }
        catch (MissingResourceException e) {
            throw new Error("Fatal: Resource for compiler is missing", e);
        }
    }
    private static String getLocalizedString(List<ResourceBundle> bundles,
                                             String key,
                                             Object... args) {
       String msg = null;
        for (List<ResourceBundle> l = bundles; l.nonEmpty() && msg == null; l = l.tail) {
            ResourceBundle rb = l.head;
            try {
                msg = rb.getString(key);
            }
            catch (MissingResourceException e) {
            }
        }
        if (msg == null) {
            msg = "compiler message file broken: key=" + key +
                " arguments={0}, {1}, {2}, {3}, {4}, {5}, {6}, {7}";
        }
        return MessageFormat.format(msg, args);
    }
}
