abstract class RT implements IAppConstants
{
    public static synchronized ICoverageData reset (final boolean createCoverageData, final boolean createExitHook)
    {
        ClassLoader loader = RT.class.getClassLoader ();
        if (loader == null) loader = ClassLoader.getSystemClassLoader (); 
        IProperties appProperties = null;
        try
        {
            appProperties = EMMAProperties.getAppProperties (loader);
        }
        catch (Throwable t)
        {
            t.printStackTrace (System.out);
        }
        s_appProperties = appProperties;
        if (EXIT_HOOK_MANAGER != null)
        {
            if (s_exitHook != null)
            {
                EXIT_HOOK_MANAGER.removeExitHook (s_exitHook);
                s_exitHook = null;
            }
        }
        ICoverageData cdata = s_cdata; 
        if (createCoverageData)
        {
            cdata = DataFactory.newCoverageData ();
            s_cdata = cdata;
        }
        else
        {
            s_cdata = null;
        }
        if (EXIT_HOOK_MANAGER != null)
        {
            if (createExitHook && (cdata != null))
            {
                final Runnable exitHook = new RTExitHook (RT.class, cdata, getCoverageOutFile (), getCoverageOutMerge ());
                RTExitHook.createClassLoaderClosure ();
                if (EXIT_HOOK_MANAGER.addExitHook (exitHook))
                {
                    s_exitHook = exitHook;
                }
            }
        }
        return cdata;
    }
    public static void r (final boolean [][] coverage, final String classVMName, final long stamp)
    {
        final ICoverageData cdata = getCoverageData (); 
        if (cdata != null)
        {
            synchronized (cdata.lock ())
            {
                cdata.addClass (coverage, classVMName, stamp);
            }
        }
    }
    public static synchronized ICoverageData getCoverageData ()
    {
        return s_cdata;
    }
    public static synchronized IProperties getAppProperties ()
    {
        return s_appProperties;
    }
    public static synchronized void dumpCoverageData (File outFile, final boolean merge, final boolean stopDataCollection)
    {
        if (DEBUG) System.out.println ("RT::dumpCoverageData() DUMPING " + RT.class.getClassLoader ());
        outFile = outFile != null ? outFile : getCoverageOutFile ();
        ICoverageData cdata = s_cdata; 
        if (stopDataCollection) s_cdata = null; 
        RTCoverageDataPersister.dumpCoverageData (cdata, ! stopDataCollection, outFile, merge);
    }
    public static synchronized void dumpCoverageData (File outFile, final boolean stopDataCollection)
    {
        outFile = outFile != null ? outFile : getCoverageOutFile ();
        ICoverageData cdata = s_cdata; 
        if (stopDataCollection) s_cdata = null; 
        RTCoverageDataPersister.dumpCoverageData (cdata, ! stopDataCollection, outFile, getCoverageOutMerge ());
    }
    private RT () {} 
    private static File getCoverageOutFile ()
    {
        final IProperties appProperties = getAppProperties (); 
        if (appProperties != null)
        {
            final String property = appProperties.getProperty (EMMAProperties.PROPERTY_COVERAGE_DATA_OUT_FILE,
                                                               EMMAProperties.DEFAULT_COVERAGE_DATA_OUT_FILE);
            return new File (property);
        }
        return new File (EMMAProperties.DEFAULT_COVERAGE_DATA_OUT_FILE); 
    }
    private static boolean getCoverageOutMerge ()
    {
        final IProperties appProperties = getAppProperties (); 
        if (appProperties != null)
        {
            final String property = appProperties.getProperty (EMMAProperties.PROPERTY_COVERAGE_DATA_OUT_MERGE,
                                                               EMMAProperties.DEFAULT_COVERAGE_DATA_OUT_MERGE.toString ());
            return Property.toBoolean (property);
        }
        return EMMAProperties.DEFAULT_COVERAGE_DATA_OUT_MERGE.booleanValue ();
    }
    private static ICoverageData s_cdata;
    private static Runnable s_exitHook;
    private static IProperties s_appProperties; 
    private static final ExitHookManager EXIT_HOOK_MANAGER; 
    private static final boolean DEBUG = false;
    static
    {
        if (DEBUG) System.out.println ("RT[" + System.identityHashCode (RT.class) + "]::<clinit>: loaded by " + RT.class.getClassLoader ());
        ExitHookManager temp = null;
        try
        {
            temp = ExitHookManager.getSingleton ();
        }
        catch (Throwable t)
        {
            t.printStackTrace (System.out);
        }
        EXIT_HOOK_MANAGER = temp;
        if (RTSettings.isStandaloneMode ())
        {
            if (DEBUG) System.out.println ("RT::<clinit>: STANDALONE MODE");
            reset (true, true);
            final Logger log = Logger.getLogger ();
            if (log.atINFO ())
            {
                log.info ("collecting runtime coverage data ...");
            }
        }
        else
        {
            reset (false, false);
        }
    }
} 
