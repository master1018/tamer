public class SecureClassLoader extends ClassLoader {
    private HashMap<CodeSource, ProtectionDomain> pds = new HashMap<CodeSource, ProtectionDomain>();
    protected SecureClassLoader() {
        super();
    }
    protected SecureClassLoader(ClassLoader parent) {
        super(parent);
    }
    protected PermissionCollection getPermissions(CodeSource codesource) {
        return new Permissions();
    }
    protected final Class<?> defineClass(String name, byte[] b, int off, int len,
            CodeSource cs) {
        return cs == null ? defineClass(name, b, off, len) : defineClass(name,
                b, off, len, getPD(cs));
    }
    protected final Class<?> defineClass(String name, ByteBuffer b, CodeSource cs) {
        byte[] data = b.array();
        return cs == null ? defineClass(name, data, 0, data.length)
                : defineClass(name, data, 0, data.length, getPD(cs));
    }
    private ProtectionDomain getPD(CodeSource cs) {
        if (cs == null) {
            return null;
        }
        ProtectionDomain pd;
        synchronized (pds) {
            if ((pd = pds.get(cs)) != null) {
                return pd;
            }
            PermissionCollection perms = getPermissions(cs);
            pd = new ProtectionDomain(cs, perms, this, null);
            pds.put(cs, pd);
        }
        return pd;
    }
}
