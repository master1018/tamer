abstract class ExitHookManager implements IJREVersion
{
    public abstract boolean addExitHook (Runnable runnable);
    public abstract boolean removeExitHook (Runnable runnable);
    public static synchronized ExitHookManager getSingleton ()
    {
        if (s_singleton == null)
        {
            if (JRE_1_3_PLUS)
            {
                s_singleton = new JRE13ExitHookManager ();
            }
            else
            {
                throw new UnsupportedOperationException ("no shutdown hook manager available [JVM: " + Property.getSystemFingerprint () + "]");
            }
        }
        return s_singleton;
    }
    protected ExitHookManager () {}
    private static final class JRE13ExitHookManager extends ExitHookManager
    {
        public synchronized boolean addExitHook (final Runnable runnable)
        {
            if ((runnable != null) && ! m_exitThreadMap.containsKey (runnable))
            {
                final Thread exitThread = new Thread (runnable, IAppConstants.APP_NAME + " shutdown handler thread");
                try
                {
                    Runtime.getRuntime ().addShutdownHook (exitThread);
                    m_exitThreadMap.put (runnable, exitThread); 
                    return true;
                }
                catch (Exception e)
                {
                    System.out.println ("exception caught while adding a shutdown hook:");
                    e.printStackTrace (System.out);
                }
            }
            return false;
        }
        public synchronized boolean removeExitHook (final Runnable runnable)
        {
            if (runnable != null)
            {
                final Thread exitThread = (Thread) m_exitThreadMap.get (runnable);  
                if (exitThread != null)
                {
                    try
                    {
                        Runtime.getRuntime ().removeShutdownHook (exitThread);
                        m_exitThreadMap.remove (runnable);
                        return true;
                    }
                    catch (Exception e)
                    {
                        System.out.println ("exception caught while removing a shutdown hook:");
                        e.printStackTrace (System.out);
                    }
                }
            }
            return false;
        }
        JRE13ExitHookManager ()
        {
            m_exitThreadMap = new HashMap ();
        }
        private final Map  m_exitThreadMap;
    } 
    private static ExitHookManager s_singleton;
} 
