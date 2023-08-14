abstract class ResourceLoader
{
    public static Class loadClass (final String name)
        throws ClassNotFoundException
    {
        final Class caller = ClassLoaderResolver.getCallerClass (1);
        final ClassLoader loader = ClassLoaderResolver.getClassLoader (caller);
        return Class.forName (name, false, loader);
    }
    public static URL getResource (final String name)
    {
        final Class caller = ClassLoaderResolver.getCallerClass (1);
        final ClassLoader loader = ClassLoaderResolver.getClassLoader (caller);
        if (loader != null)
            return loader.getResource (name);
        else
            return ClassLoader.getSystemResource (name);
    }
    public static InputStream getResourceAsStream (final String name)
    {
        final Class caller = ClassLoaderResolver.getCallerClass (1);
        final ClassLoader loader = ClassLoaderResolver.getClassLoader (caller);
        if (loader != null)
            return loader.getResourceAsStream (name);
        else
            return ClassLoader.getSystemResourceAsStream (name);
    }
    public static Enumeration getResources (final String name)
        throws IOException
    {
        final Class caller = ClassLoaderResolver.getCallerClass (1);
        final ClassLoader loader = ClassLoaderResolver.getClassLoader (caller);
        if (loader != null)
            return loader.getResources (name);
        else
            return ClassLoader.getSystemResources (name);
    }
    public static Class loadClass (final String name, final ClassLoader loader)
        throws ClassNotFoundException
    {
        return Class.forName (name, false, loader != null ? loader : ClassLoader.getSystemClassLoader ());
    }
    public static URL getResource (final String name, final ClassLoader loader)
    {
        if (loader != null)
            return loader.getResource (name);
        else
            return ClassLoader.getSystemResource (name);
    }
    public static InputStream getResourceAsStream (final String name, final ClassLoader loader)
    {
        if (loader != null)
            return loader.getResourceAsStream (name);
        else
            return ClassLoader.getSystemResourceAsStream (name);
    }
    public static Enumeration getResources (final String name, final ClassLoader loader)
        throws IOException
    {
        if (loader != null)
            return loader.getResources (name);
        else
            return ClassLoader.getSystemResources (name);
    }
    private ResourceLoader () {} 
} 
