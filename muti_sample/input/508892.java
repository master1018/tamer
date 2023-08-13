public abstract class ClassLoader {
    static private class SystemClassLoader {
        public static ClassLoader loader = ClassLoader.createSystemClassLoader();
    }
    private ClassLoader parent;
    private Map<String, Package> packages = new HashMap<String, Package>();
    private static ClassLoader createSystemClassLoader() {
        String classPath = System.getProperty("java.class.path", ".");
        return new PathClassLoader(classPath, BootClassLoader.getInstance());
    }
    public static ClassLoader getSystemClassLoader() {
        SecurityManager smgr = System.getSecurityManager();
        if (smgr != null) {
            ClassLoader caller = VMStack.getCallingClassLoader();
            if (caller != null && !caller.isAncestorOf(SystemClassLoader.loader)) {
                smgr.checkPermission(new RuntimePermission("getClassLoader"));
            }
        }
        return SystemClassLoader.loader;
    }
    public static URL getSystemResource(String resName) {
        return SystemClassLoader.loader.getResource(resName);
    }
    public static Enumeration<URL> getSystemResources(String resName) throws IOException {
        return SystemClassLoader.loader.getResources(resName);
    }
    public static InputStream getSystemResourceAsStream(String resName) {
        return SystemClassLoader.loader.getResourceAsStream(resName);
    }
    protected ClassLoader() {
        this(getSystemClassLoader(), false);
    }
    protected ClassLoader(ClassLoader parentLoader) {
        this(parentLoader, false);
    }
    ClassLoader(ClassLoader parentLoader, boolean nullAllowed) {
        SecurityManager smgr = System.getSecurityManager();
        if (smgr != null) {
            smgr.checkCreateClassLoader();
        }
        if (parentLoader == null && !nullAllowed) {
            throw new NullPointerException(
                    "Parent ClassLoader may not be null");
        }
        parent = parentLoader;
    }
    @Deprecated
    protected final Class<?> defineClass(byte[] classRep, int offset, int length)
            throws ClassFormatError {
        return VMClassLoader.defineClass(this, classRep, offset, length, null);
    }
    protected final Class<?> defineClass(String className, byte[] classRep, int offset, int length)
            throws ClassFormatError {
        return defineClass(className, classRep, offset, length, null);
    }
    protected final Class<?> defineClass(String className, byte[] classRep, int offset, int length,
            ProtectionDomain protectionDomain) throws java.lang.ClassFormatError {
        return VMClassLoader.defineClass(this, className, classRep, offset, length,
                protectionDomain);
    }
    protected final Class<?> defineClass(String name, ByteBuffer b,
            ProtectionDomain protectionDomain) throws ClassFormatError {
        byte[] temp = new byte[b.remaining()];
        b.get(temp);
        return defineClass(name, temp, 0, temp.length, protectionDomain);
    }
    protected Class<?> findClass(String className) throws ClassNotFoundException {
        throw new ClassNotFoundException(className);
    }
    protected final Class<?> findLoadedClass(String className) {
        ClassLoader loader;
        if (this == BootClassLoader.getInstance())
            loader = null;
        else
            loader = this;
        return VMClassLoader.findLoadedClass(loader, className);
    }
    protected final Class<?> findSystemClass(String className) throws ClassNotFoundException {
        return Class.forName(className, false, getSystemClassLoader());
    }
    public final ClassLoader getParent() {
        SecurityManager smgr = System.getSecurityManager();
        if (smgr != null) {
            smgr.checkPermission(new RuntimePermission("getClassLoader"));
        }
        return parent;
    }
    public URL getResource(String resName) {
        URL resource = null;
        resource = parent.getResource(resName);
        if (resource == null) {
            resource = findResource(resName);
        }
        return resource;
    }
    @SuppressWarnings("unchecked")
    public Enumeration<URL> getResources(String resName) throws IOException {
        Enumeration first = parent.getResources(resName);
        Enumeration second = findResources(resName);
        return new TwoEnumerationsInOne(first, second);
    }
    public InputStream getResourceAsStream(String resName) {
        try {
            URL url = getResource(resName);
            if (url != null) {
                return url.openStream();
            }
        } catch (IOException ex) {
        }
        return null;
    }
    public Class<?> loadClass(String className) throws ClassNotFoundException {
        return loadClass(className, false);
    }
    protected Class<?> loadClass(String className, boolean resolve) throws ClassNotFoundException {
        Class<?> clazz = findLoadedClass(className);
        if (clazz == null) {
            try {
                clazz = parent.loadClass(className, false);
            } catch (ClassNotFoundException e) {
            }
            if (clazz == null) {
                clazz = findClass(className);
            }
        }
        return clazz;
    }
    protected final void resolveClass(Class<?> clazz) {
    }
    final boolean isSystemClassLoader() {
        return false;
    }
    final boolean isAncestorOf(ClassLoader child) {
        for (ClassLoader current = child; current != null;
                current = current.parent) {
            if (current == this) {
                return true;
            }
        }
        return false;
    }
    protected URL findResource(String resName) {
        return null;
    }
    @SuppressWarnings( {
            "unchecked", "unused"
    })
    protected Enumeration<URL> findResources(String resName) throws IOException {
        return EmptyEnumeration.getInstance();
    }
    protected String findLibrary(String libName) {
        return null;
    }
    protected Package getPackage(String name) {
        synchronized (packages) {
            Package p = packages.get(name);
            return p;
        }
    }
    static Package getPackage(ClassLoader loader, String name) {
        return loader.getPackage(name);
    }
    protected Package[] getPackages() {
        synchronized (packages) {
            Collection<Package> col = packages.values();
            Package[] result = new Package[col.size()];
            col.toArray(result);
            return result;
        }
    }
    protected Package definePackage(String name, String specTitle, String specVersion,
            String specVendor, String implTitle, String implVersion, String implVendor, URL sealBase)
            throws IllegalArgumentException {
        synchronized (packages) {
            if (packages.containsKey(name)) {
                throw new IllegalArgumentException("Package " + name + " already defined");
            }
            Package newPackage = new Package(name, specTitle, specVersion, specVendor, implTitle,
                    implVersion, implVendor, sealBase);
            packages.put(name, newPackage);
            return newPackage;
        }
    }
    final Object[] getSigners(Class<?> c) {
        return null;
    }
    protected final void setSigners(Class<?> c, Object[] signers) {
        return;
    }
    static final ClassLoader getStackClassLoader(int depth) {
        Class<?>[] stack = VMStack.getClasses(depth + 1, false);
        if(stack.length < depth + 1) {
            return null;
        }
        return stack[depth].getClassLoader(); 
    }
    static void loadLibraryWithClassLoader(String libName, ClassLoader loader) {
        return;
    }
    static void loadLibraryWithPath(String libName, ClassLoader loader, String libraryPath) {
        return;
    }
    public void setClassAssertionStatus(String cname, boolean enable) {
        return;
    }
    public void setPackageAssertionStatus(String pname, boolean enable) {
        return;
    }
    public void setDefaultAssertionStatus(boolean enable) {
        return;
    }
    public void clearAssertionStatus() {
        return;
    }
    boolean getClassAssertionStatus(String cname) {
        return false;
    }
    boolean getPackageAssertionStatus(String pname) {
        return false;
    }
    boolean getDefaultAssertionStatus() {
        return false;
    }
}
class TwoEnumerationsInOne implements Enumeration<URL> {
    private Enumeration<URL> first;
    private Enumeration<URL> second;
    public TwoEnumerationsInOne(Enumeration<URL> first, Enumeration<URL> second) {
        this.first = first;
        this.second = second;
    }
    public boolean hasMoreElements() {
        return first.hasMoreElements() || second.hasMoreElements();
    }
    public URL nextElement() {
        if (first.hasMoreElements()) {
            return first.nextElement();
        } else {
            return second.nextElement();
        }
    }
}
class BootClassLoader extends ClassLoader {
    static BootClassLoader instance;
    public static BootClassLoader getInstance() {
        if (instance == null) {
            instance = new BootClassLoader();
        }
        return instance;
    }
    public BootClassLoader() {
        super(null, true);
    }
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        return VMClassLoader.loadClass(name, false);
    }
    @Override
    protected URL findResource(String name) {
        return VMClassLoader.getResource(name);
    }
    @SuppressWarnings("unused")
    @Override
    protected Enumeration<URL> findResources(String resName) throws IOException {
        Enumeration<URL> result = VMClassLoader.getResources(resName);
        if (result == null) {
            result = EmptyEnumeration.getInstance();
        }
        return result;
    }
    @Override
    protected Package getPackage(String name) {
        if (name != null && !"".equals(name)) {
            synchronized (this) {
                Package pack = super.getPackage(name);
                if (pack == null) {
                    pack = definePackage(name, "Unknown", "0.0", "Unknown", "Unknown", "0.0",
                            "Unknown", null);
                }
                return pack;
            }
        }
        return null;
    }
    @Override
    public URL getResource(String resName) {
        return findResource(resName);
    }
    @Override
    protected Class<?> loadClass(String className, boolean resolve)
           throws ClassNotFoundException {
        Class<?> clazz = findLoadedClass(className);
        if (clazz == null) {
            clazz = findClass(className);
        }
        return clazz;
    }
    @Override
    public Enumeration<URL> getResources(String resName) throws IOException {
        return findResources(resName);
    }
}
