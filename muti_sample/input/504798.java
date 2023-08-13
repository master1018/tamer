public abstract class Support_ClassLoader {
    public abstract ClassLoader getClassLoader(URL url, ClassLoader parent);
    public static ClassLoader getInstance(URL url, ClassLoader parent) {
        try {
            Support_ClassLoader factory; 
            if ("Dalvik".equals(System.getProperty("java.vm.name"))) {
                factory = (Support_ClassLoader)Class.forName(
                    "tests.support.Support_ClassLoader$Dalvik").newInstance();
            } else {
                factory = (Support_ClassLoader)Class.forName(
                    "tests.support.Support_ClassLoader$RefImpl").newInstance();
            }
            return factory.getClassLoader(url, parent);
        } catch (Exception ex) {
            throw new RuntimeException("Unable to create ClassLoader", ex);
        }
    }
    static class Dalvik extends Support_ClassLoader {
        private static File tmp;
        static {
            tmp = new File(System.getProperty("java.io.tmpdir"), "dex-cache");
            tmp.mkdirs();
        }
        @Override
        public ClassLoader getClassLoader(URL url, ClassLoader parent) {
            return new DexClassLoader(url.getPath(), tmp.getAbsolutePath(),
                    null, parent);
        }
    }
    static class RefImpl extends Support_ClassLoader {
        @Override
        public ClassLoader getClassLoader(URL url, ClassLoader parent) {
            return new URLClassLoader(new URL[] { url }, parent);
        }
    }
}
