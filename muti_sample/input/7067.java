public class JDK13Services {
    private static final long DEFAULT_CACHING_PERIOD = 60000;
    private static final String PROPERTIES_FILENAME = "sound.properties";
    private static Map providersCacheMap = new HashMap();
    private static long cachingPeriod = DEFAULT_CACHING_PERIOD;
    private static Properties properties;
    private JDK13Services() {
    }
    public static void setCachingPeriod(int seconds) {
        cachingPeriod = seconds * 1000L;
    }
    public static synchronized List getProviders(Class serviceClass) {
        ProviderCache cache = (ProviderCache) providersCacheMap.get(serviceClass);
        if (cache == null) {
            cache = new ProviderCache();
            providersCacheMap.put(serviceClass, cache);
        }
        if (cache.providers == null ||
            System.currentTimeMillis() > cache.lastUpdate + cachingPeriod) {
            cache.providers = Collections.unmodifiableList(JSSecurityManager.getProviders(serviceClass));
            cache.lastUpdate = System.currentTimeMillis();
        }
        return cache.providers;
    }
    public static synchronized String getDefaultProviderClassName(Class typeClass) {
        String value = null;
        String defaultProviderSpec = getDefaultProvider(typeClass);
        if (defaultProviderSpec != null) {
            int hashpos = defaultProviderSpec.indexOf('#');
            if (hashpos == 0) {
            } else if (hashpos > 0) {
                value = defaultProviderSpec.substring(0, hashpos);
            } else {
                value = defaultProviderSpec;
            }
        }
        return value;
    }
    public static synchronized String getDefaultInstanceName(Class typeClass) {
        String value = null;
        String defaultProviderSpec = getDefaultProvider(typeClass);
        if (defaultProviderSpec != null) {
            int hashpos = defaultProviderSpec.indexOf('#');
            if (hashpos >= 0 && hashpos < defaultProviderSpec.length() - 1) {
                value = defaultProviderSpec.substring(hashpos + 1);
            }
        }
        return value;
    }
    private static synchronized String getDefaultProvider(Class typeClass) {
        if (!SourceDataLine.class.equals(typeClass)
                && !TargetDataLine.class.equals(typeClass)
                && !Clip.class.equals(typeClass)
                && !Port.class.equals(typeClass)
                && !Receiver.class.equals(typeClass)
                && !Transmitter.class.equals(typeClass)
                && !Synthesizer.class.equals(typeClass)
                && !Sequencer.class.equals(typeClass)) {
            return null;
        }
        String value;
        String propertyName = typeClass.getName();
        value = JSSecurityManager.getProperty(propertyName);
        if (value == null) {
            value = getProperties().getProperty(propertyName);
        }
        if ("".equals(value)) {
            value = null;
        }
        return value;
    }
    private static synchronized Properties getProperties() {
        if (properties == null) {
            properties = new Properties();
            JSSecurityManager.loadProperties(properties, PROPERTIES_FILENAME);
        }
        return properties;
    }
    private static class ProviderCache {
        public long lastUpdate;
        public List providers;
    }
}
