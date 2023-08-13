abstract class ClassLoaderResolver
{
    public static synchronized ClassLoader getClassLoader (final Class caller)
    {
        final ClassLoadContext ctx = new ClassLoadContext (caller);
        return s_strategy.getClassLoader (ctx); 
    }
    public static synchronized ClassLoader getClassLoader ()
    {
        final Class caller = getCallerClass (1); 
        final ClassLoadContext ctx = new ClassLoadContext (caller);
        return s_strategy.getClassLoader (ctx); 
    }
    public static Class getCallerClass (final int callerOffset)
    {
        if (CALLER_RESOLVER == null) return null; 
        return CALLER_RESOLVER.getClassContext () [CALL_CONTEXT_OFFSET + callerOffset];
    }
    public static boolean isChild (final ClassLoader loader1, ClassLoader loader2)
    {
        if (loader1 == loader2) return true; 
        if (loader2 == null) return false; 
        if (loader1 == null) return true;
        for ( ; loader2 != null; loader2 = loader2.getParent ())
        {
            if (loader2 == loader1) return true;
        }   
        return false;
    }
    public static synchronized IClassLoadStrategy getStrategy ()
    {
        return s_strategy;
    }
    public static synchronized IClassLoadStrategy setStrategy (final IClassLoadStrategy strategy)
    {
        if (strategy == null) throw new IllegalArgumentException ("null input: strategy");
        final IClassLoadStrategy old = s_strategy;
        s_strategy = strategy;
        return old;
    }
    private static final class DefaultClassLoadStrategy implements IClassLoadStrategy
    {
        public ClassLoader getClassLoader (final ClassLoadContext ctx)
        {
            if (ctx == null) throw new IllegalArgumentException ("null input: ctx");
            final Class caller = ctx.getCallerClass ();
            final ClassLoader contextLoader = Thread.currentThread ().getContextClassLoader ();
            ClassLoader result;
            if (caller == null)
                result = contextLoader;
            else
            {
                final ClassLoader callerLoader = caller.getClassLoader ();
                if (isChild (callerLoader, contextLoader))
                    result = contextLoader;
                else
                    result = callerLoader;
            }
            final ClassLoader systemLoader = ClassLoader.getSystemClassLoader ();
            if (isChild (result, systemLoader))
                result = systemLoader;
            return result;
        }
    } 
    private static final class CallerResolver extends SecurityManager
    {
        protected Class [] getClassContext ()
        {
            return super.getClassContext ();
        }
    } 
    private ClassLoaderResolver () {} 
    private static IClassLoadStrategy s_strategy; 
    private static final int CALL_CONTEXT_OFFSET = 2; 
    private static final CallerResolver CALLER_RESOLVER; 
    static
    {
        CallerResolver temp = null;
        try
        {
            temp = new CallerResolver ();
        }
        catch (Throwable t)
        {
        }
        CALLER_RESOLVER = temp;
        s_strategy = new DefaultClassLoadStrategy ();
    }
} 
