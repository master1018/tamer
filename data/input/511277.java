final class InstrClassLoader extends URLClassLoader
{
    public static final String PROPERTY_FORCED_DELEGATION_FILTER  = "clsload.forced_delegation_filter";
    public static final String PROPERTY_THROUGH_DELEGATION_FILTER  = "clsload.through_delegation_filter";
    public InstrClassLoader (final ClassLoader parent, final File [] classpath,
                             final IInclExclFilter forcedDelegationFilter,
                             final IInclExclFilter throughDelegationFilter,
                             final IClassLoadHook hook, final Map cache)
        throws MalformedURLException
    {
        super (filesToURLs (classpath), null);
        m_hook = hook;
        m_cache = cache; 
        m_forcedDelegationFilter = forcedDelegationFilter;
        m_throughDelegationFilter = throughDelegationFilter;
        m_parent = parent;        
        m_bufPool = new PoolEntry [BAOS_POOL_SIZE];
        m_log = Logger.getLogger ();
    }
    public synchronized final Class loadClass (final String name, final boolean resolve)
        throws ClassNotFoundException
    {
        final boolean trace1 = m_log.atTRACE1 ();
        if (trace1) m_log.trace1 ("loadClass",  "(" + name + ", " + resolve + "): nest level " + m_nestLevel);
        Class c = null;
        c = findLoadedClass (name);
        if (c == null)
        {
            Class parentsVersion = null;
            if (m_parent != null)
            {
                try
                {
                    parentsVersion = m_parent.loadClass (name); 
                    if ((parentsVersion.getClassLoader () != m_parent) ||
                        ((m_forcedDelegationFilter == null) || m_forcedDelegationFilter.included (name)))
                    {
                        c = parentsVersion;
                        if (trace1) m_log.trace1 ("loadClass", "using parent's version for [" + name + "]");
                    }
                }
                catch (ClassNotFoundException cnfe)
                {
                    if ((m_forcedDelegationFilter == null) || m_forcedDelegationFilter.included (name))
                        throw cnfe;
                }
            }
            if (c == null)
            {
                try
                {
                    c = findClass (name);
                }
                catch (ClassNotFoundException cnfe)
                {
                    if (parentsVersion != null)
                    {
                        final boolean delegate = (m_throughDelegationFilter == null) || m_throughDelegationFilter.included (name); 
                        if (delegate)
                        {
                            c = parentsVersion;
                            if (trace1) m_log.trace1 ("loadClass", "[delegation filter] using parent's version for [" + name + "]");
                        }
                        else
                            throw cnfe;
                    }
                    else
                      throw cnfe;
                }
            }
        }
        if (c == null) throw new ClassNotFoundException (name);
        if (resolve) resolveClass (c); 
        return c;
    }
    public final URL getResource (final String name)
    {
        final boolean trace1 = m_log.atTRACE1 ();
        if (trace1) m_log.trace1 ("getResource",  "(" + name + "): nest level " + m_nestLevel);
        final URL result = super.getResource (name);
        if (trace1 && (result != null)) m_log.trace1 ("loadClass",  "[" + name + "] found in " + result);
        return result;
    }
    protected final Class findClass (final String name)
        throws ClassNotFoundException
    {
        final boolean trace1 = m_log.atTRACE1 ();
        if (trace1) m_log.trace1 ("findClass",  "(" + name + "): nest level " + m_nestLevel);
        final boolean useClassCache = (m_cache != null);
        final ClassPathCacheEntry entry = useClassCache ? (ClassPathCacheEntry) m_cache.remove (name) : null;
        byte [] bytes;
        int length;
        URL classURL = null;
        if (entry != null) 
        {
            ++ m_cacheHits;
            try
            {
                classURL = new URL (entry.m_srcURL);
            }
            catch (MalformedURLException murle) 
            {
                if ($assert.ENABLED)
                {
                    murle.printStackTrace (System.out);
                }
            }
            PoolEntry buf = null;
            try
            {
                buf = acquirePoolEntry ();
                final ByteArrayOStream baos = buf.m_baos; 
                bytes = entry.m_bytes;
                length = bytes.length;
                if ((m_hook != null) && m_hook.processClassDef (name, bytes, length, baos)) 
                {
                    bytes = baos.getByteArray ();
                    length = baos.size ();
                    if (trace1) m_log.trace1 ("findClass",  "defining [cached] instrumented [" + name + "] {" + length + " bytes }");
                }
                else
                {
                    if (trace1) m_log.trace1 ("findClass",  "defining [cached] [" + name + "] {" + length + " bytes }");
                }
                return defineClass (name, bytes, length, classURL);
            }
            catch (IOException ioe)
            {
                throw new ClassNotFoundException (name);
            }
            finally
            {
                if (buf != null) releasePoolEntry (buf);
            }
        }
        else 
        {
            if (useClassCache) ++ m_cacheMisses;
            final String classResource = name.replace ('.', '/') + ".class";
            classURL = getResource (classResource); 
            if (trace1 && (classURL != null)) m_log.trace1 ("findClass",  "[" + name + "] found in " + classURL);
            if (classURL == null)
                throw new ClassNotFoundException (name);
            else
            {
                InputStream in = null;
                PoolEntry buf = null;
                try
                {
                    in = classURL.openStream ();
                    buf = acquirePoolEntry ();
                    final ByteArrayOStream baos = buf.m_baos; 
                    readFully (in, baos, buf.m_buf);
                    in.close (); 
                    in = null;
                    bytes = baos.getByteArray ();
                    length = baos.size ();
                    baos.reset (); 
                    if ((m_hook != null) && m_hook.processClassDef (name, bytes, length, baos)) 
                    {
                        bytes = baos.getByteArray ();
                        length = baos.size ();
                        if (trace1) m_log.trace1 ("findClass",  "defining instrumented [" + name + "] {" + length + " bytes }");
                    }
                    else
                    {
                        if (trace1) m_log.trace1 ("findClass",  "defining [" + name + "] {" + length + " bytes }");
                    }
                    return defineClass (name, bytes, length, classURL);
                }
                catch (IOException ioe)
                {
                    throw new ClassNotFoundException (name);
                }
                finally
                {
                    if (buf != null) releasePoolEntry (buf);
                    if (in != null) try { in.close (); } catch (Exception ignore) {}
                }
            }
        }
    }
    public void debugDump (final PrintWriter out)
    {
        if (out != null)
        {
            out.println (this + ": " + m_cacheHits + " class cache hits, " + m_cacheMisses + " misses");
        }
    }
    private static final class PoolEntry
    {
        PoolEntry (final int baosCapacity, final int bufSize)
        {
            m_baos = new ByteArrayOStream (baosCapacity);
            m_buf = new byte [bufSize];
        }
        void trim (final int baosCapacity, final int baosMaxCapacity)
        {
            if (m_baos.capacity () > baosMaxCapacity)
            {
                m_baos = new ByteArrayOStream (baosCapacity);
            }
        }
        ByteArrayOStream m_baos;
        final byte [] m_buf;
    } 
    private Class defineClass (final String className, final byte [] bytes, final int length, final URL srcURL)
    {
        final CodeSource csrc = new CodeSource (srcURL, (Certificate[])null);
        final int lastDot = className.lastIndexOf ('.');
        if (lastDot >= 0)
        {
            final String packageName = className.substring (0, lastDot);
            final Package pkg = getPackage (packageName);
            if (pkg == null)
            {
                definePackage (packageName,
                               IAppConstants.APP_NAME, IAppConstants.APP_VERSION, IAppConstants.APP_COPYRIGHT,
                               IAppConstants.APP_NAME, IAppConstants.APP_VERSION, IAppConstants.APP_COPYRIGHT,
                               srcURL);
            }
        }
        return defineClass (className, bytes, 0, length, csrc);
    }
    private static URL [] filesToURLs (final File [] classpath)
        throws MalformedURLException
    {
        if ((classpath == null) || (classpath.length == 0))
            return EMPTY_URL_ARRAY;
        final URL [] result = new URL [classpath.length];
        for (int f = 0; f < result.length ; ++ f)
        {
            result [f] = classpath [f].toURL (); 
        }
        return result;
    }
    private static void readFully (final InputStream in, final ByteArrayOStream out, final byte [] buf)
        throws IOException
    {
        for (int read; (read = in.read (buf)) >= 0; )
        {
            out.write (buf, 0, read);
        }
    }
    private PoolEntry acquirePoolEntry ()
    {
        PoolEntry result;
        if (m_nestLevel >= BAOS_POOL_SIZE)
        {
            result = new PoolEntry (BAOS_INIT_SIZE, BAOS_INIT_SIZE);
        }
        else
        {
            result = m_bufPool [m_nestLevel];
            if (result == null)
            {
                result = new PoolEntry (BAOS_INIT_SIZE, BAOS_INIT_SIZE);
                m_bufPool [m_nestLevel] = result;
            }
            else
            {
                result.m_baos.reset ();
            }
        }
        ++ m_nestLevel;
        return result;
    }
    private void releasePoolEntry (final PoolEntry buf)
    {
        if (-- m_nestLevel < BAOS_POOL_SIZE)
        {
            buf.trim (BAOS_INIT_SIZE, BAOS_MAX_SIZE);
        }
    }
    private final ClassLoader m_parent;    
    private final IInclExclFilter m_forcedDelegationFilter;
    private final IInclExclFilter m_throughDelegationFilter;
    private final Map  m_cache; 
    private final IClassLoadHook m_hook;
    private final PoolEntry [] m_bufPool;
    private final Logger m_log; 
    private int m_nestLevel;
    private int m_cacheHits, m_cacheMisses;
    private static final int BAOS_INIT_SIZE = 32 * 1024;
    private static final int BAOS_MAX_SIZE = 1024 * 1024;
    private static final int BAOS_POOL_SIZE = 8;
    private static final URL [] EMPTY_URL_ARRAY = new URL [0];
} 
