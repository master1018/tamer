public class ClassLoaderUtil {
    public static void releaseLoader(URLClassLoader classLoader) {
        releaseLoader(classLoader, null);
    }
    public static List<IOException> releaseLoader(URLClassLoader classLoader, List<String> jarsClosed) {
        List<IOException> ioExceptions = new LinkedList<IOException>();
        try {
            if (jarsClosed != null) {
                jarsClosed.clear();
            }
            URLClassPath ucp = SharedSecrets.getJavaNetAccess()
                                                .getURLClassPath(classLoader);
            ArrayList loaders = ucp.loaders;
            Stack urls = ucp.urls;
            HashMap lmap = ucp.lmap;
            synchronized(urls) {
                urls.clear();
            }
            synchronized(lmap) {
                lmap.clear();
            }
            synchronized (ucp) {
                for (Object o : loaders) {
                    if (o != null) {
                        if (o instanceof URLClassPath.JarLoader) {
                                URLClassPath.JarLoader jl = (URLClassPath.JarLoader)o;
                                JarFile jarFile = jl.getJarFile();
                                try {
                                    if (jarFile != null) {
                                        jarFile.close();
                                        if (jarsClosed != null) {
                                            jarsClosed.add(jarFile.getName());
                                        }
                                    }
                                } catch (IOException ioe) {
                                    String jarFileName = (jarFile == null) ? "filename not available":jarFile.getName();
                                    String msg = "Error closing JAR file: " + jarFileName;
                                    IOException newIOE = new IOException(msg);
                                    newIOE.initCause(ioe);
                                    ioExceptions.add(newIOE);
                                }
                        }
                    }
                }
                loaders.clear();
            }
        } catch (Throwable t) {
            throw new RuntimeException (t);
        }
        return ioExceptions;
    }
}
