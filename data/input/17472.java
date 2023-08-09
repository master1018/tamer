public class CloseableURLClassLoader
        extends URLClassLoader implements Closeable {
    public CloseableURLClassLoader(URL[] urls, ClassLoader parent) throws Error {
        super(urls, parent);
        try {
            getLoaders(); 
        } catch (Throwable t) {
            throw new Error("cannot create CloseableURLClassLoader", t);
        }
    }
    @Override
    public void close() throws IOException {
        try {
            for (Object l: getLoaders()) {
                if (l.getClass().getName().equals("sun.misc.URLClassPath$JarLoader")) {
                    Field jarField = l.getClass().getDeclaredField("jar");
                    JarFile jar = (JarFile) getField(l, jarField);
                    if (jar != null) {
                        jar.close();
                    }
                }
            }
        } catch (Throwable t) {
            IOException e = new IOException("cannot close class loader");
            e.initCause(t);
            throw e;
        }
    }
    private ArrayList<?> getLoaders()
            throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException
    {
        Field ucpField = URLClassLoader.class.getDeclaredField("ucp");
        Object urlClassPath = getField(this, ucpField);
        if (urlClassPath == null)
            throw new AssertionError("urlClassPath not set in URLClassLoader");
        Field loadersField = urlClassPath.getClass().getDeclaredField("loaders");
        return (ArrayList<?>) getField(urlClassPath, loadersField);
    }
    private Object getField(Object o, Field f)
            throws IllegalArgumentException, IllegalAccessException {
        boolean prev = f.isAccessible();
        try {
            f.setAccessible(true);
            return f.get(o);
        } finally {
            f.setAccessible(prev);
        }
    }
}
