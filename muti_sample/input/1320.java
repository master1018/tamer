public final class Bridge
{
    private static final Class[] NO_ARGS = new Class[] {};
    private static final Permission getBridgePermission =
        new BridgePermission( "getBridge" ) ;
    private static Bridge bridge = null ;
    private final Method latestUserDefinedLoaderMethod ;
    private final Unsafe unsafe ;
    private final ReflectionFactory reflectionFactory ;
    private Method getLatestUserDefinedLoaderMethod()
    {
        return (Method) AccessController.doPrivileged(
            new PrivilegedAction()
            {
                public Object run()
                {
                    Method result = null;
                    try {
                        Class io = ObjectInputStream.class;
                        result = io.getDeclaredMethod(
                            "latestUserDefinedLoader", NO_ARGS);
                        result.setAccessible(true);
                    } catch (NoSuchMethodException nsme) {
                        Error err = new Error( "java.io.ObjectInputStream" +
                            " latestUserDefinedLoader " + nsme );
                        err.initCause(nsme) ;
                        throw err ;
                    }
                    return result;
                }
            }
        );
    }
    private Unsafe getUnsafe() {
        Field fld = (Field)AccessController.doPrivileged(
            new PrivilegedAction()
            {
                public Object run()
                {
                    Field fld = null ;
                    try {
                        Class unsafeClass = sun.misc.Unsafe.class ;
                        fld = unsafeClass.getDeclaredField( "theUnsafe" ) ;
                        fld.setAccessible( true ) ;
                        return fld ;
                    } catch (NoSuchFieldException exc) {
                        Error err = new Error( "Could not access Unsafe" ) ;
                        err.initCause( exc ) ;
                        throw err ;
                    }
                }
            }
        ) ;
        Unsafe unsafe = null;
        try {
            unsafe = (Unsafe)(fld.get( null )) ;
        } catch (Throwable t) {
            Error err = new Error( "Could not access Unsafe" ) ;
            err.initCause( t ) ;
            throw err ;
        }
        return unsafe ;
    }
    private Bridge()
    {
        latestUserDefinedLoaderMethod = getLatestUserDefinedLoaderMethod();
        unsafe = getUnsafe() ;
        reflectionFactory = (ReflectionFactory)AccessController.doPrivileged(
            new ReflectionFactory.GetReflectionFactoryAction());
    }
    public static final synchronized Bridge get()
    {
        SecurityManager sman = System.getSecurityManager() ;
        if (sman != null)
            sman.checkPermission( getBridgePermission ) ;
        if (bridge == null) {
            bridge = new Bridge() ;
        }
        return bridge ;
    }
    public final ClassLoader getLatestUserDefinedLoader()
    {
        try {
            return (ClassLoader)latestUserDefinedLoaderMethod.invoke(null,
                                                                     (Object[])NO_ARGS);
        } catch (InvocationTargetException ite) {
            Error err = new Error(
                "sun.corba.Bridge.latestUserDefinedLoader: " + ite ) ;
            err.initCause( ite ) ;
            throw err ;
        } catch (IllegalAccessException iae) {
            Error err = new Error(
                "sun.corba.Bridge.latestUserDefinedLoader: " + iae ) ;
            err.initCause( iae ) ;
            throw err ;
        }
    }
    public final int getInt(Object o, long offset)
    {
        return unsafe.getInt( o, offset ) ;
    }
    public final void putInt(Object o, long offset, int x)
    {
        unsafe.putInt( o, offset, x ) ;
    }
    public final Object getObject(Object o, long offset)
    {
        return unsafe.getObject( o, offset ) ;
    }
    public final void putObject(Object o, long offset, Object x)
    {
        unsafe.putObject( o, offset, x ) ;
    }
    public final boolean getBoolean(Object o, long offset)
    {
        return unsafe.getBoolean( o, offset ) ;
    }
    public final void    putBoolean(Object o, long offset, boolean x)
    {
        unsafe.putBoolean( o, offset, x ) ;
    }
    public final byte    getByte(Object o, long offset)
    {
        return unsafe.getByte( o, offset ) ;
    }
    public final void    putByte(Object o, long offset, byte x)
    {
        unsafe.putByte( o, offset, x ) ;
    }
    public final short   getShort(Object o, long offset)
    {
        return unsafe.getShort( o, offset ) ;
    }
    public final void    putShort(Object o, long offset, short x)
    {
        unsafe.putShort( o, offset, x ) ;
    }
    public final char    getChar(Object o, long offset)
    {
        return unsafe.getChar( o, offset ) ;
    }
    public final void    putChar(Object o, long offset, char x)
    {
        unsafe.putChar( o, offset, x ) ;
    }
    public final long    getLong(Object o, long offset)
    {
        return unsafe.getLong( o, offset ) ;
    }
    public final void    putLong(Object o, long offset, long x)
    {
        unsafe.putLong( o, offset, x ) ;
    }
    public final float   getFloat(Object o, long offset)
    {
        return unsafe.getFloat( o, offset ) ;
    }
    public final void    putFloat(Object o, long offset, float x)
    {
        unsafe.putFloat( o, offset, x ) ;
    }
    public final double  getDouble(Object o, long offset)
    {
        return unsafe.getDouble( o, offset ) ;
    }
    public final void    putDouble(Object o, long offset, double x)
    {
        unsafe.putDouble( o, offset, x ) ;
    }
    public static final long INVALID_FIELD_OFFSET   = -1;
    public final long objectFieldOffset(Field f)
    {
        return unsafe.objectFieldOffset( f ) ;
    }
    public final void throwException(Throwable ee)
    {
        unsafe.throwException( ee ) ;
    }
    public final Constructor newConstructorForSerialization( Class cl,
        Constructor cons )
    {
        return reflectionFactory.newConstructorForSerialization( cl, cons ) ;
    }
}
