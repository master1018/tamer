public abstract class ResourceBundle {
    protected ResourceBundle parent;
    private Locale locale;
    static class MissingBundle extends ResourceBundle {
        @Override
        public Enumeration<String> getKeys() {
            return null;
        }
        @Override
        public Object handleGetObject(String name) {
            return null;
        }
    }
    private static final ResourceBundle MISSING = new MissingBundle();
    private static final ResourceBundle MISSINGBASE = new MissingBundle();
    private static final WeakHashMap<Object, Hashtable<String, ResourceBundle>> cache = new WeakHashMap<Object, Hashtable<String, ResourceBundle>>();
    private static Locale defaultLocale = Locale.getDefault();
    public ResourceBundle() {
    }
    public static final ResourceBundle getBundle(String bundleName)
            throws MissingResourceException {
        return getBundleImpl(bundleName, Locale.getDefault(), VMStack
                .getCallingClassLoader());
    }
    public static final ResourceBundle getBundle(String bundleName,
            Locale locale) {
        return getBundleImpl(bundleName, locale,
                VMStack.getCallingClassLoader());
    }
    public static ResourceBundle getBundle(String bundleName, Locale locale,
            ClassLoader loader) throws MissingResourceException {
        if (loader == null) {
            throw new NullPointerException();
        }
        return getBundleImpl(bundleName, locale, loader);
    }
    private static ResourceBundle getBundleImpl(String bundleName,
            Locale locale, ClassLoader loader) throws MissingResourceException {
        if (bundleName != null) {
            ResourceBundle bundle;
            if (!defaultLocale.equals(Locale.getDefault())) {
                cache.clear();
                defaultLocale = Locale.getDefault();
            }
            if (!locale.equals(Locale.getDefault())) {
                String localeName = locale.toString();
                if (localeName.length() > 0) {
                    localeName = "_" + localeName; 
                }
                if ((bundle = handleGetBundle(bundleName, localeName, false,
                        loader)) != null) {
                    return bundle;
                }
            }
            String localeName = Locale.getDefault().toString();
            if (localeName.length() > 0) {
                localeName = "_" + localeName; 
            }
            if ((bundle = handleGetBundle(bundleName, localeName, true, loader)) != null) {
                return bundle;
            }
            throw new MissingResourceException(Msg.getString("KA029", bundleName, locale), bundleName + '_' + locale, 
                    ""); 
        }
        throw new NullPointerException();
    }
    public abstract Enumeration<String> getKeys();
    public Locale getLocale() {
        return locale;
    }
    public final Object getObject(String key) {
        ResourceBundle last, theParent = this;
        do {
            Object result = theParent.handleGetObject(key);
            if (result != null) {
                return result;
            }
            last = theParent;
            theParent = theParent.parent;
        } while (theParent != null);
        throw new MissingResourceException(Msg.getString("KA029", last.getClass().getName(), key), last.getClass().getName(), key); 
    }
    public final String getString(String key) {
        return (String) getObject(key);
    }
    public final String[] getStringArray(String key) {
        return (String[]) getObject(key);
    }
    private static ResourceBundle handleGetBundle(String base, String locale,
            boolean loadBase, final ClassLoader loader) {
        ResourceBundle bundle = null;
        String bundleName = base + locale;
        Object cacheKey = loader != null ? (Object) loader : (Object) "null"; 
        Hashtable<String, ResourceBundle> loaderCache;
        synchronized (cache) {
            loaderCache = cache.get(cacheKey);
            if (loaderCache == null) {
                loaderCache = new Hashtable<String, ResourceBundle>();
                cache.put(cacheKey, loaderCache);
            }
        }
        ResourceBundle result = loaderCache.get(bundleName);
        if (result != null) {
            if (result == MISSINGBASE) {
                return null;
            }
            if (result == MISSING) {
                if (!loadBase) {
                    return null;
                }
                String extension = strip(locale);
                if (extension == null) {
                    return null;
                }
                return handleGetBundle(base, extension, loadBase, loader);
            }
            return result;
        }
        try {
            Class<?> bundleClass = Class.forName(bundleName, true, loader);
            if (ResourceBundle.class.isAssignableFrom(bundleClass)) {
                bundle = (ResourceBundle) bundleClass.newInstance();
            }
        } catch (LinkageError e) {
        } catch (Exception e) {
        }
        if (bundle != null) {
            bundle.setLocale(locale);
        } else {
            final String fileName = bundleName.replace('.', '/');
            InputStream stream = AccessController
                    .doPrivileged(new PrivilegedAction<InputStream>() {
                        public InputStream run() {
                            return loader == null ? ClassLoader
                                    .getSystemResourceAsStream(fileName
                                            + ".properties") : loader 
                                    .getResourceAsStream(fileName
                                            + ".properties"); 
                        }
                    });
            if (stream != null) {
                try {
                    try {
                        bundle = new PropertyResourceBundle(stream);
                    } finally {
                        stream.close();
                    }
                    bundle.setLocale(locale);
                } catch (IOException e) {
                }
            }
        }
        String extension = strip(locale);
        if (bundle != null) {
            if (extension != null) {
                ResourceBundle parent = handleGetBundle(base, extension, true,
                        loader);
                if (parent != null) {
                    bundle.setParent(parent);
                }
            }
            loaderCache.put(bundleName, bundle);
            return bundle;
        }
        if (extension != null && (loadBase || extension.length() > 0)) {
            bundle = handleGetBundle(base, extension, loadBase, loader);
            if (bundle != null) {
                loaderCache.put(bundleName, bundle);
                return bundle;
            }
        }
        loaderCache.put(bundleName, loadBase ? MISSINGBASE : MISSING);
        return null;
    }
    protected abstract Object handleGetObject(String key);
    protected void setParent(ResourceBundle bundle) {
        parent = bundle;
    }
    private static String strip(String name) {
        int index = name.lastIndexOf('_');
        if (index != -1) {
            return name.substring(0, index);
        }
        return null;
    }
    private void setLocale(String name) {
        String language = "", country = "", variant = ""; 
        if (name.length() > 1) {
            int nextIndex = name.indexOf('_', 1);
            if (nextIndex == -1) {
                nextIndex = name.length();
            }
            language = name.substring(1, nextIndex);
            if (nextIndex + 1 < name.length()) {
                int index = nextIndex;
                nextIndex = name.indexOf('_', nextIndex + 1);
                if (nextIndex == -1) {
                    nextIndex = name.length();
                }
                country = name.substring(index + 1, nextIndex);
                if (nextIndex + 1 < name.length()) {
                    variant = name.substring(nextIndex + 1, name.length());
                }
            }
        }
        locale = new Locale(language, country, variant);
    }
}
