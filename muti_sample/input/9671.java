class ClassDefiner {
    static final Unsafe unsafe = Unsafe.getUnsafe();
    static Class defineClass(String name, byte[] bytes, int off, int len,
                             final ClassLoader parentClassLoader)
    {
        ClassLoader newLoader = AccessController.doPrivileged(
            new PrivilegedAction<ClassLoader>() {
                public ClassLoader run() {
                        return new DelegatingClassLoader(parentClassLoader);
                    }
                });
        return unsafe.defineClass(name, bytes, off, len, newLoader, null);
    }
}
class DelegatingClassLoader extends ClassLoader {
    DelegatingClassLoader(ClassLoader parent) {
        super(parent);
    }
}
