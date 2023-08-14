class VMClassLoader {
    static URL getResource(String name) {
        int numEntries = getBootClassPathSize();
        int i;
        for (i = 0; i < numEntries; i++) {
            String urlStr = getBootClassPathResource(name, i);
            if (urlStr != null) {
                try {
                    return new URL(urlStr);
                }
                catch (MalformedURLException mue) {
                    mue.printStackTrace();
                }
            }
        }
        return null;
    }
    static Enumeration<URL> getResources(String name) {
        ArrayList<URL> list = null;
        int numEntries = getBootClassPathSize();
        int i;
        for (i = 0; i < numEntries; i++) {
            String urlStr = getBootClassPathResource(name, i);
            if (urlStr != null) {
                if (list == null)
                    list = new ArrayList<URL>();
                try {
                    list.add(new URL(urlStr));
                }
                catch (MalformedURLException mue) {
                    mue.printStackTrace();
                }
            }
        }
        if (list == null)
            return null;
        else
            return new EnumerateListArray<URL>(list);
    }
    native static Class loadClass(String name, boolean resolve)
        throws ClassNotFoundException;
    native static Class getPrimitiveClass(char type);
    native static Class defineClass(ClassLoader cl, String name,
        byte[] data, int offset, int len, ProtectionDomain pd)
        throws ClassFormatError;
    native static Class defineClass(ClassLoader cl,
            byte[] data, int offset, int len, ProtectionDomain pd)
            throws ClassFormatError;
    native static Class findLoadedClass(ClassLoader cl, String name);
    native private static int getBootClassPathSize();
    native private static String getBootClassPathResource(String name,
            int index);
    private static class EnumerateListArray<T> implements Enumeration<T> {
        private final ArrayList mList;
        private int i = 0;
        EnumerateListArray(ArrayList list) {
            mList = list;
        }
        public boolean hasMoreElements() {
            return i < mList.size();
        }
        public T nextElement() {
            if (i >= mList.size())
                throw new NoSuchElementException();
            return (T) mList.get(i++);
        }
    };
}
