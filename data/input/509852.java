abstract class EMMAProperties
{
    public static final String GENERIC_PROPERTY_OVERRIDE_PREFIX = "D";
    public static final String DEFAULT_META_DATA_OUT_FILE       = "coverage.em";
    public static final Boolean DEFAULT_META_DATA_OUT_MERGE     = Boolean.TRUE;
    public static final String PREFIX_META_DATA                 = "metadata.";
    public static final String PROPERTY_META_DATA_OUT_FILE      = PREFIX_META_DATA + "out.file";
    public static final String PROPERTY_META_DATA_OUT_MERGE     = PREFIX_META_DATA + "out.merge";
    public static final String DEFAULT_COVERAGE_DATA_OUT_FILE   = "coverage.ec";
    public static final Boolean DEFAULT_COVERAGE_DATA_OUT_MERGE = Boolean.TRUE;
    public static final String PREFIX_COVERAGE_DATA             = "coverage.";
    public static final String PROPERTY_COVERAGE_DATA_OUT_FILE  = PREFIX_COVERAGE_DATA + "out.file";
    public static final String PROPERTY_COVERAGE_DATA_OUT_MERGE = PREFIX_COVERAGE_DATA + "out.merge";
    public static final String DEFAULT_SESSION_DATA_OUT_FILE    = "coverage.es";
    public static final Boolean DEFAULT_SESSION_DATA_OUT_MERGE  = Boolean.TRUE;
    public static final String PREFIX_SESSION_DATA              = "session.";
    public static final String PROPERTY_SESSION_DATA_OUT_FILE   = PREFIX_SESSION_DATA + "out.file";
    public static final String PROPERTY_SESSION_DATA_OUT_MERGE  = PREFIX_SESSION_DATA + "out.merge";
    public static final String PROPERTY_TEMP_FILE_EXT           = ".et";
    public static final Map SYSTEM_PROPERTY_REDIRECTS; 
    public static synchronized long getTimeStamp ()
    {
        long result = s_timestamp;
        if (result == 0)
        {
            s_timestamp = result = System.currentTimeMillis ();
        }
        return result; 
    }
    public static String makeAppVersion (final int major, final int minor, final int build)
    {
        final StringBuffer buf = new StringBuffer ();
        buf.append (major);
        buf.append ('.');
        buf.append (minor);
        buf.append ('.');
        buf.append (build);
        return buf.toString ();
    }
    public static IProperties wrap (final Properties properties)
    {
        if (properties == null) return null;
        return IProperties.Factory.wrap (properties, ReportProperties.REPORT_PROPERTY_MAPPER);
    }
    public static synchronized IProperties getAppProperties ()
    {
        final ClassLoader loader = ClassLoaderResolver.getClassLoader ();
        return getAppProperties (loader);
    }
    public static synchronized IProperties getAppProperties (final ClassLoader loader)
    {
        IProperties properties = (IProperties) s_properties.get (loader);
        if (properties != null)
            return properties;
        else
        {
            final String appName = IAppConstants.APP_NAME_LC;
            final IProperties systemRedirects = wrap (Property.getSystemPropertyRedirects (EMMAProperties.SYSTEM_PROPERTY_REDIRECTS));
            final IProperties appDefaults = wrap (Property.getProperties (appName + "_default.properties", loader));
            final IProperties systemFile;
            {
                final String fileName = Property.getSystemProperty (appName + ".properties");
                final File file = fileName != null
                    ? new File (fileName)
                    : null;
                systemFile = wrap (Property.getLazyPropertiesFromFile (file));
            }
            final IProperties system = wrap (Property.getSystemProperties (appName));
            final IProperties userOverrides = wrap (Property.getProperties (appName + ".properties", loader));
            properties = IProperties.Factory.combine (userOverrides,
                         IProperties.Factory.combine (system,
                         IProperties.Factory.combine (systemFile,
                         IProperties.Factory.combine (appDefaults,
                                                      systemRedirects))));
            s_properties.put (loader, properties);
            return properties;
        }
    }
    private EMMAProperties () {} 
    private static long s_timestamp;
    private static final Map  s_properties; 
    static
    {
        s_properties = new WeakHashMap ();
        final Map redirects = new HashMap ();
        redirects.put (IReportProperties.PREFIX.concat (IReportProperties.OUT_ENCODING),
                       "file.encoding");
        redirects.put (IReportProperties.PREFIX.concat (IReportProperties.OUT_DIR),
                       "user.dir");
        SYSTEM_PROPERTY_REDIRECTS = Collections.unmodifiableMap (redirects);
    }
} 
