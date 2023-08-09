public class SecureClassLoader extends ClassLoader {
    private final boolean initialized;
    private final HashMap<CodeSource, ProtectionDomain> pdcache =
                        new HashMap<>(11);
    private static final Debug debug = Debug.getInstance("scl");
    static {
        ClassLoader.registerAsParallelCapable();
    }
    protected SecureClassLoader(ClassLoader parent) {
        super(parent);
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkCreateClassLoader();
        }
        initialized = true;
    }
    protected SecureClassLoader() {
        super();
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkCreateClassLoader();
        }
        initialized = true;
    }
    protected final Class<?> defineClass(String name,
                                         byte[] b, int off, int len,
                                         CodeSource cs)
    {
        return defineClass(name, b, off, len, getProtectionDomain(cs));
    }
    protected final Class<?> defineClass(String name, java.nio.ByteBuffer b,
                                         CodeSource cs)
    {
        return defineClass(name, b, getProtectionDomain(cs));
    }
    protected PermissionCollection getPermissions(CodeSource codesource)
    {
        check();
        return new Permissions(); 
    }
    private ProtectionDomain getProtectionDomain(CodeSource cs) {
        if (cs == null)
            return null;
        ProtectionDomain pd = null;
        synchronized (pdcache) {
            pd = pdcache.get(cs);
            if (pd == null) {
                PermissionCollection perms = getPermissions(cs);
                pd = new ProtectionDomain(cs, perms, this, null);
                pdcache.put(cs, pd);
                if (debug != null) {
                    debug.println(" getPermissions "+ pd);
                    debug.println("");
                }
            }
        }
        return pd;
    }
    private void check() {
        if (!initialized) {
            throw new SecurityException("ClassLoader object not initialized");
        }
    }
}
