public class TestBug4179766 extends RBTestFmwk {
    private static final int SAME_HASH_CODE = 0;
    private static int nextHashCode = SAME_HASH_CODE + 1;
    private static final String CLASS_SUFFIX = ".class";
    private static synchronized int getNextHashCode() {
        return nextHashCode++;
    }
    public static void main(String[] args) throws Exception {
        Object o1 = new Bug4179766Class();
        Object o2 = new Bug4179766Resource();
        new TestBug4179766().run(args);
    }
    public void testCache() throws Exception {
        Loader loader = new Loader(false);
        ResourceBundle b1 = getResourceBundle(loader, "Bug4179766Resource");
        if (b1 == null) {
            errln("Resource not found: Bug4179766Resource");
        }
        ResourceBundle b2 = getResourceBundle(loader, "Bug4179766Resource");
        if (b2 == null) {
            errln("Resource not found: Bug4179766Resource");
        }
        printIDInfo("[bundle1]",b1);
        printIDInfo("[bundle2]",b2);
        if (b1 != b2) {
            errln("Different objects returned by same ClassLoader");
        }
    }
    public void testSameHash() throws Exception {
        doTest(true);
    }
    public void testDifferentHash() throws Exception {
        doTest(false);
    }
    private void doTest(boolean sameHash) throws Exception {
        ResourceBundle b1 = getResourceBundle(new Loader(sameHash), "Bug4179766Resource");
        if (b1 == null) {
           errln("Resource not found: Bug4179766Resource");
        }
        ResourceBundle b2 = getResourceBundle(new Loader(sameHash), "Bug4179766Resource");
        if (b2 == null) {
           errln("Resource not found: Bug4179766Resource");
        }
        printIDInfo("[bundle1]",b1);
        printIDInfo("[bundle2]",b2);
        if (b1 == b2) {
           errln("Same object returned by different ClassLoaders");
        }
    }
    private ResourceBundle getResourceBundle(Loader loader, String name) throws Exception {
        try {
            Class c = loader.loadClass("Bug4179766Class");
            Bug4179766Getter test = (Bug4179766Getter)c.newInstance();
            return test.getResourceBundle(name);
        } catch (ClassNotFoundException e) {
            errln("Class not found by custom class loader: "+name);
            throw e;
        } catch (InstantiationException e) {
            errln("Error instantiating: "+name);
            throw e;
        } catch (IllegalAccessException e) {
            errln("IllegalAccessException instantiating: "+name);
            throw e;
        }
    }
    private void printIDInfo(String message, Object o) {
        if (o == null) {
            return;
        }
        Class c = o.getClass();
        ClassLoader l = c.getClassLoader();
        int hash = -1;
        if (l != null) {
            hash = l.hashCode();
        }
        logln(message + System.identityHashCode(o) + "  Class: " + c
                + "  ClassLoader: " + l + "  loaderHash: " + hash
                + "  loaderPrimHash: " + System.identityHashCode(l));
    }
    public class Loader extends ClassLoader {
        private int thisHashCode;
        public Loader(boolean sameHash) {
            super(Loader.class.getClassLoader());
            if (sameHash) {
                thisHashCode = SAME_HASH_CODE;
            } else {
                thisHashCode = getNextHashCode();
            }
        }
        public int hashCode() {
            return thisHashCode;
        }
        private byte[] getClassData(String className) {
            boolean shouldLoad = className.equals("Bug4179766Class");
            shouldLoad = shouldLoad || className.equals("Bug4179766Resource");
            if (shouldLoad) {
                try {
                    File file = new File(System.getProperty("test.classes", "."), className+CLASS_SUFFIX);
                    FileInputStream fi = new FileInputStream(file);
                    byte[] result = new byte[fi.available()];
                    fi.read(result);
                    return result;
                } catch (Exception e) {
                    return null;
                }
            } else {
                return null;
            }
        }
        public synchronized Class loadClass(String className, boolean resolveIt)
                throws ClassNotFoundException {
            Class result = findLoadedClass(className);
            if (result != null) {
                printInfo("        ***Returning cached class: "+className, result);
                return result;
            }
            byte[] classData = getClassData(className);
            if (classData == null) {
                return loadFromSystem(className);
            }
            result = defineClass(classData, 0, classData.length);
            if (result == null) {
                return loadFromSystem(className);
            }
            if (resolveIt) {
                resolveClass(result);
            }
            printInfo("        ***Loaded local class: "+className, result);
            return result;
        }
        private Class loadFromSystem(String className) throws ClassNotFoundException {
            try {
                Class result = getParent().loadClass(className);
                printInfo("        ***Returning system class: "+className, result);
                return result;
            } catch (ClassNotFoundException e) {
                printInfo("        ***Class not found: "+className, null);
                throw e;
            }
        }
        private void printInfo(String message, Class c) {
            if (c != null) {
                logln(""+System.identityHashCode(this)+"  "+message+"  "+System.identityHashCode(c));
            } else {
                logln(""+System.identityHashCode(this)+"  "+message);
            }
        }
    }
}
