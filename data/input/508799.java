public final class EasyMockProperties {
    private static final String PREFIX = "easymock.";
    private static volatile EasyMockProperties instance;
    private final Properties properties = new Properties();
    public static EasyMockProperties getInstance() {
        if (instance == null) {
            synchronized (EasyMockProperties.class) {
                if (instance == null) {
                    instance = new EasyMockProperties();
                }
            }
        }
        return instance;
    }
    private EasyMockProperties() {
        InputStream in = getClassLoader().getResourceAsStream(
                "easymock.properties");
        if (in != null) {
            in = new BufferedInputStream(in);
            try {
                properties.load(in);
            } catch (IOException e) {
                throw new RuntimeException(
                        "Failed to read easymock.properties file");
            } finally {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }
        for (Map.Entry<Object, Object> entry : System.getProperties()
                .entrySet()) {
            if (entry.getKey() instanceof String
                    && entry.getKey().toString().startsWith(PREFIX)) {
                properties.put(entry.getKey(), entry.getValue());
            }
        }
    }
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
    public String getProperty(String key) {
        return properties.getProperty(key);
    }
    public String setProperty(String key, String value) {
        if (!key.startsWith(PREFIX)) {
            throw new IllegalArgumentException("Invalid key (" + key
                    + "), an easymock property starts with \"" + PREFIX + "\"");
        }
        if (value == null) {
            return (String) properties.remove(key);
        }
        return (String) properties.setProperty(key, value);
    }
    private ClassLoader getClassLoader() {
        ClassLoader cl = null;
        try {
            cl = Thread.currentThread().getContextClassLoader();
        } catch (Throwable ex) {
        }
        if (cl == null) {
            cl = getClass().getClassLoader();
        }
        return cl;
    }
}
