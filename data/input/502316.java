abstract class AppLoggers
{
    public static final String PREFIX_VERBOSITY                 = "verbosity.";
    public static final String PROPERTY_VERBOSITY_LEVEL         = PREFIX_VERBOSITY + "level";
    public static final String DEFAULT_VERBOSITY_LEVEL          = ILogLevels.INFO_STRING;
    public static final String PROPERTY_VERBOSITY_FILTER        = PREFIX_VERBOSITY + "filter";
    public static Logger create (final String appName, final IProperties properties, final Logger base)
    {
        if (properties == null)
            throw new IllegalArgumentException ("null input: properties");
        final int level;
        {
            final String _level = properties.getProperty (PROPERTY_VERBOSITY_LEVEL,
                                                          DEFAULT_VERBOSITY_LEVEL);
            level = Logger.stringToLevel (_level);
        }
        final Set filter;
        {
            final String _filter = properties.getProperty (PROPERTY_VERBOSITY_FILTER);
            Set temp = null;
            if (_filter != null)
            {
                final StringTokenizer tokenizer = new StringTokenizer (_filter, COMMA_DELIMITERS);
                if (tokenizer.countTokens () > 0)
                {
                    temp = new HashSet (tokenizer.countTokens ());
                    while (tokenizer.hasMoreTokens ())
                    {
                        temp.add (tokenizer.nextToken ());
                    }
                }
            }
            filter = temp;
        }
        return Logger.create (level, null, appName, filter, base);
    }
    private AppLoggers () {} 
    private static final String COMMA_DELIMITERS    = "," + Strings.WHITE_SPACE;
} 
