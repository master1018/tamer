final class Logger implements ILogLevels
{
    public static Logger create (final int level, final PrintWriter out, final String prefix, final Set classMask)
    {
        if ((level < NONE) || (level > ALL))
            throw new IllegalArgumentException ("invalid log level: " + level);
        if ((out == null) || out.checkError ())
            throw new IllegalArgumentException ("null or corrupt input: out");
        return new Logger (level, out, prefix, classMask);
    }
    public static Logger create (final int level, final PrintWriter out, final String prefix, final Set classMask,
                                 final Logger base)
    {
        if (base == null)
        {
            return create (level, out, prefix, classMask);
        }
        else
        {
            final int _level = level >= NONE
                ? level
                : base.m_level;
            final PrintWriter _out = (out != null) && ! out.checkError ()
                ? out
                : base.m_out;
            final String _prefix = prefix;
            final Set _classMask = classMask != null
                ? classMask
                : base.m_classMask;
            return new Logger (_level, _out, _prefix, _classMask);
        }
    }
    public final boolean isLoggable (final int level)
    {
        return (level <= m_level);
    }
    public final boolean atINFO ()
    {
        return (INFO <= m_level);
    }
    public final boolean atVERBOSE ()
    {
        return (VERBOSE <= m_level);
    }
    public final boolean atTRACE1 ()
    {
        return (TRACE1 <= m_level);
    }
    public final boolean atTRACE2 ()
    {
        return (TRACE2 <= m_level);
    }
    public final boolean atTRACE3 ()
    {
        return (TRACE3 <= m_level);
    }
    public final void warning (final String msg)
    {
        _log (WARNING, null, msg, false);
    }
    public final void info (final String msg)
    {
        _log (INFO, null, msg, false);
    }
    public final void verbose (final String msg)
    {
        _log (VERBOSE, null, msg, false);
    }
    public final void trace1 (final String method, final String msg)
    {
        _log (TRACE1, method, msg, true);
    }
    public final void trace2 (final String method, final String msg)
    {
        _log (TRACE2, method, msg, true);
    }
    public final void trace3 (final String method, final String msg)
    {
        _log (TRACE3, method, msg, true);
    }
    public final void log (final int level, final String msg, final boolean logCaller)
    {
        _log (level, null, msg, logCaller);
    }
    public final void log (final int level, final String method, final String msg, final boolean logCaller)
    {
        _log (level, method, msg, logCaller);
    }
    public final void log (final int level, final String msg, final Throwable throwable)
    {
        _log (level, null, msg, throwable);
    }
    public final void log (final int level, final String method, final String msg, final Throwable throwable)
    {
        _log (level, method, msg, throwable);
    }
    public PrintWriter getWriter ()
    {
        return m_out;
    }
    public static Logger getLogger ()
    {
        final LinkedList stack = (LinkedList) THREAD_LOCAL_STACK.get ();
        if (stack.isEmpty ())
        {
            return STATIC_LOGGER;
        }
        else
        {
            return (Logger) stack.getLast ();
        }
    }
    public static void push (final Logger ctx)
    {
        if (ctx == null)
            throw new IllegalArgumentException ("null input: ctx");
        final LinkedList stack = (LinkedList) THREAD_LOCAL_STACK.get ();
        stack.addLast (ctx);
    }
    public static void pop (final Logger ctx)
    {
        final LinkedList stack = (LinkedList) THREAD_LOCAL_STACK.get ();
        try
        {
            final Logger current = (Logger) stack.getLast ();
            if (current != ctx)
                throw new IllegalStateException ("invalid context being popped: " + ctx);
            stack.removeLast ();
            current.cleanup ();
        }
        catch (NoSuchElementException nsee)
        {
            throw new IllegalStateException ("empty logger context stack on thread [" + Thread.currentThread () + "]: " + nsee);
        }
    }
    public static int stringToLevel (final String level)
    {
        if (ILogLevels.SEVERE_STRING.equalsIgnoreCase (level) || ILogLevels.SILENT_STRING.equalsIgnoreCase (level))
            return ILogLevels.SEVERE;
        else if (ILogLevels.WARNING_STRING.equalsIgnoreCase (level) || ILogLevels.QUIET_STRING.equalsIgnoreCase (level))
            return ILogLevels.WARNING;
        else if (ILogLevels.INFO_STRING.equalsIgnoreCase (level))
            return ILogLevels.INFO;
        else if (ILogLevels.VERBOSE_STRING.equalsIgnoreCase (level))
            return ILogLevels.VERBOSE;
        else if (ILogLevels.TRACE1_STRING.equalsIgnoreCase (level))
            return ILogLevels.TRACE1;
        else if (ILogLevels.TRACE2_STRING.equalsIgnoreCase (level))
            return ILogLevels.TRACE2;
        else if (ILogLevels.TRACE3_STRING.equalsIgnoreCase (level))
            return ILogLevels.TRACE3;
        else if (ILogLevels.NONE_STRING.equalsIgnoreCase (level))
            return ILogLevels.NONE;
        else if (ILogLevels.ALL_STRING.equalsIgnoreCase (level))
            return ILogLevels.ALL;
        else
        {
            int _level = Integer.MIN_VALUE;
            try
            {
                _level = Integer.parseInt (level);
            }
            catch (Exception ignore) {}
            if ((_level >= ILogLevels.NONE) && (_level <= ILogLevels.ALL))
                return _level;
            else
                return ILogLevels.INFO; 
        }
    }
    private static final class ThreadLocalStack extends InheritableThreadLocal
    {
        protected Object initialValue ()
        {
            return new LinkedList ();
        }
    } 
    private Logger (final int level, final PrintWriter out, final String prefix, final Set classMask)
    {
        m_level = level;
        m_out = out;
        m_prefix = prefix;
        m_classMask = classMask; 
    }
    private void cleanup ()
    {
        m_out.flush ();
    }
    private void _log (final int level, final String method,
                       final String msg, final boolean logCaller)
    {
        if ((level <= m_level) && (level >= SEVERE))
        {
            final Class caller = logCaller ? ClassLoaderResolver.getCallerClass (2) : null;
            final StringBuffer buf = new StringBuffer (m_prefix != null ? m_prefix + ": " : "");
            if ((caller != null) || (method != null))
            {
                buf.append ("[");
                if (caller != null) 
                {
                    String callerName = caller.getName ();
                    if (callerName.startsWith (PREFIX_TO_STRIP))
                        callerName = callerName.substring (PREFIX_TO_STRIP_LENGTH);
                    String parentName = callerName;
                    final int firstDollar = callerName.indexOf ('$');
                    if (firstDollar > 0) parentName = callerName.substring (0, firstDollar);
                    if ((m_classMask == null) || m_classMask.contains (parentName))
                        buf.append (callerName);
                    else
                        return;
                }
                if (method != null)
                {
                    buf.append ("::");
                    buf.append (method);
                }
                buf.append ("] ");
            }
            final PrintWriter out = m_out;
            if (msg != null) buf.append (msg);
            out.println (buf);
            if (FLUSH_LOG) out.flush ();
        }
    }
    private void _log (final int level, final String method,
                       final String msg, final Throwable throwable)
    {        
        if ((level <= m_level) && (level >= SEVERE))
        {
            final Class caller = ClassLoaderResolver.getCallerClass (2);
            final StringBuffer buf = new StringBuffer (m_prefix != null ? m_prefix + ": " : "");
            if ((caller != null) || (method != null))
            {
                buf.append ("[");
                if (caller != null) 
                {
                    String callerName = caller.getName ();
                    if (callerName.startsWith (PREFIX_TO_STRIP))
                        callerName = callerName.substring (PREFIX_TO_STRIP_LENGTH);
                    String parentName = callerName;
                    final int firstDollar = callerName.indexOf ('$');
                    if (firstDollar > 0) parentName = callerName.substring (0, firstDollar);
                    if ((m_classMask == null) || m_classMask.contains (parentName))
                        buf.append (callerName);
                    else
                        return;
                }
                if (method != null)
                {
                    buf.append ("::");
                    buf.append (method);
                }
                buf.append ("] ");
            }
            final PrintWriter out = m_out;
            if (msg != null) buf.append (msg);
            if (throwable != null)
            {
                final StringWriter sw = new StringWriter ();
                final PrintWriter pw = new PrintWriter (sw);
                throwable.printStackTrace (pw);
                pw.flush ();
                buf.append (sw.toString ());
            }
            out.println (buf);
            if (FLUSH_LOG) out.flush ();
        }
    }
    private final int m_level; 
    private final PrintWriter m_out; 
    private final String m_prefix; 
    private final Set  m_classMask; 
    private static final String PREFIX_TO_STRIP = "com.vladium."; 
    private static final int PREFIX_TO_STRIP_LENGTH = PREFIX_TO_STRIP.length ();
    private static final boolean FLUSH_LOG = true;
    private static final String COMMA_DELIMITERS    = "," + Strings.WHITE_SPACE;
    private static final Logger STATIC_LOGGER; 
    private static final ThreadLocalStack THREAD_LOCAL_STACK; 
    static
    {
        THREAD_LOCAL_STACK = new ThreadLocalStack ();
        final Properties properties = Property.getAppProperties (IAppConstants.APP_NAME_LC, Logger.class.getClassLoader ());
        final int level;
        {
            final String _level = properties.getProperty (AppLoggers.PROPERTY_VERBOSITY_LEVEL,
                                                          AppLoggers.DEFAULT_VERBOSITY_LEVEL);
            level = stringToLevel (_level);
        }
        final Set filter;
        {
            final String _filter = properties.getProperty (AppLoggers.PROPERTY_VERBOSITY_FILTER);
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
        STATIC_LOGGER = create (level,
                                new PrintWriter (System.out, false),
                                IAppConstants.APP_NAME,
                                filter);
    }
} 
