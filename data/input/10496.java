class JDKClassLoader {
    private static final JDKClassLoaderCache classCache
        = new JDKClassLoaderCache();
    private static final Bridge bridge =
        (Bridge)AccessController.doPrivileged(
            new PrivilegedAction() {
                public Object run() {
                    return Bridge.get() ;
                }
            }
        ) ;
    static Class loadClass(Class aClass, String className)
        throws ClassNotFoundException {
        if (className == null) {
            throw new NullPointerException();
        }
        if (className.length() == 0) {
            throw new ClassNotFoundException();
        }
        ClassLoader loader;
        if (aClass != null) {
            loader = aClass.getClassLoader();
        } else {
            loader = bridge.getLatestUserDefinedLoader();
        }
        Object key = classCache.createKey(className, loader);
        if (classCache.knownToFail(key)) {
            throw new ClassNotFoundException(className);
        } else {
            try {
                return Class.forName(className, false, loader);
            } catch(ClassNotFoundException cnfe) {
                classCache.recordFailure(key);
                throw cnfe;
            }
        }
    }
    private static class JDKClassLoaderCache
    {
        public final void recordFailure(Object key) {
            cache.put(key, JDKClassLoaderCache.KNOWN_TO_FAIL);
        }
        public final Object createKey(String className, ClassLoader latestLoader) {
            return new CacheKey(className, latestLoader);
        }
        public final boolean knownToFail(Object key) {
            return cache.get(key) == JDKClassLoaderCache.KNOWN_TO_FAIL;
        }
        private final Map cache
            = Collections.synchronizedMap(new WeakHashMap());
        private static final Object KNOWN_TO_FAIL = new Object();
        private static class CacheKey
        {
            String className;
            ClassLoader loader;
            public CacheKey(String className, ClassLoader loader) {
                this.className = className;
                this.loader = loader;
            }
            public int hashCode() {
                if (loader == null)
                    return className.hashCode();
                else
                    return className.hashCode() ^ loader.hashCode();
            }
            public boolean equals(Object obj) {
                try {
                    if (obj == null)
                        return false;
                    CacheKey other = (CacheKey)obj;
                    return (className.equals(other.className) &&
                            loader == other.loader);
                } catch (ClassCastException cce) {
                    return false;
                }
            }
        }
    }
}
