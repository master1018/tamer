public class ArrayClassTest {
    public static void main(String[] args) throws Exception {
        MBeanServer mbs = MBeanServerFactory.createMBeanServer();
        URLClassLoader testLoader =
            (URLClassLoader) ArrayClassTest.class.getClassLoader();
        ClassLoader loader = new SpyLoader(testLoader.getURLs());
        ObjectName loaderName = new ObjectName("test:type=SpyLoader");
        mbs.registerMBean(loader, loaderName);
        ObjectName testName = new ObjectName("test:type=Test");
        mbs.createMBean(Test.class.getName(), testName, loaderName,
                        new Object[1], new String[] {X[].class.getName()});
        ClassLoader checkLoader = mbs.getClassLoaderFor(testName);
        if (checkLoader != loader)
            throw new AssertionError("Wrong loader: " + checkLoader);
        mbs.invoke(testName, "ignore", new Object[1],
                   new String[] {Y[].class.getName()});
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream oout = new ObjectOutputStream(bout);
        oout.writeObject(new Z[0]);
        oout.close();
        byte[] bytes = bout.toByteArray();
        ObjectInputStream oin = mbs.deserialize(testName, bytes);
        Object zarray = oin.readObject();
        String failed = null;
        if (zarray instanceof Z[])
            failed = "read back a real Z[]";
        else if (!zarray.getClass().getName().equals(Z[].class.getName())) {
            failed = "returned object of wrong type: " +
                zarray.getClass().getName();
        } else if (Array.getLength(zarray) != 0)
            failed = "returned array of wrong size: " + Array.getLength(zarray);
        if (failed != null) {
            System.out.println("TEST FAILED: " + failed);
            System.exit(1);
        }
        System.out.println("Test passed");
    }
    public static interface TestMBean {
        public void ignore(Y[] ignored);
    }
    public static class Test implements TestMBean {
        public Test(X[] ignored) {}
        public void ignore(Y[] ignored) {}
    }
    public static class X {}
    public static class Y {}
    public static class Z implements Serializable {}
    public static interface SpyLoaderMBean {}
    public static class SpyLoader extends URLClassLoader
            implements SpyLoaderMBean, PrivateClassLoader {
        public SpyLoader(URL[] urls) {
            super(urls, null);
        }
        public Class findClass(String name) throws ClassNotFoundException {
            System.out.println("findClass: " + name);
            if (false)
                new Throwable().printStackTrace(System.out);
            Class c = super.findClass(name);
            System.out.println(" -> " + name + " (" + c.getClassLoader() + ")");
            return c;
        }
    }
}
