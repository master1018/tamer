final class RTExitHook implements Runnable
{
    public synchronized void run ()
    {
        if (m_cdata != null)
        {
            RTCoverageDataPersister.dumpCoverageData (m_cdata, true, m_outFile, m_merge);
            m_RT = null;
            m_cdata = null;
        }
    }
    public static void createClassLoaderClosure ()
    {
        Properties closureMap = null;
        InputStream in = null;
        try
        {
            in = RTExitHook.class.getResourceAsStream (CLOSURE_RESOURCE);
            if (in != null)
            {
                closureMap = new Properties ();
                closureMap.load (in);
            }
            else
            {
                throw new Error ("packaging failure: closure resource not found");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace (System.out);
            throw new Error ("packaging failure: " + e.toString ());
        }
        finally
        {
            if (in != null) try { in.close (); } catch (IOException ignore) { ignore.printStackTrace (); }
        }
        in = null;
        final String closureList = closureMap.getProperty ("closure");
        if (closureList == null)
        {
            throw new Error ("packaging failure: no closure mapping");
        }
        final ClassLoader loader = RTExitHook.class.getClassLoader ();
        final StringTokenizer tokenizer = new StringTokenizer (closureList, ",");
        while (tokenizer.hasMoreTokens ())
        {
            final String className = tokenizer.nextToken ();
            try
            {
                Class.forName (className, true, loader);
            }
            catch (Exception e)
            {
                throw new Error ("packaging failure: class [" + className + "] not found {" + e.toString () + "}");
            }
        }
    }
    RTExitHook (final Class RT, final ICoverageData cdata, final File outFile, final boolean merge)
    {
        m_RT = RT;
        m_cdata = cdata;
        m_outFile = outFile;
        m_merge = merge;
    }
    private final File m_outFile;
    private final boolean m_merge;
    private Class m_RT; 
    private ICoverageData m_cdata;
    private static final String CLOSURE_RESOURCE = "RTExitHook.closure"; 
} 
