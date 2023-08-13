public class LeakTest {
    private static Class<?>[] otherTests = {RandomMXBeanTest.class};
    private static class ShadowClassLoader extends URLClassLoader {
        ShadowClassLoader(URL[] urls, ClassLoader parent) {
            super(urls, parent);
        }
    }
    public static void main(String[] args) throws Exception {
        System.out.println("Testing that no references are held to ClassLoaders " +
                "by caches in the MXBean infrastructure");
        for (Class<?> testClass : otherTests)
            test(testClass);
        if (failure != null)
            throw new Exception("CLASSLOADER LEAK TEST FAILED: " + failure);
        System.out.println("CLASSLOADER LEAK TEST PASSED");
        if (args.length > 0) {
            System.out.println("Waiting for input");
            System.in.read();
        }
    }
    private static void test(Class<?> originalTestClass) throws Exception {
        System.out.println();
        System.out.println("TESTING " + originalTestClass.getName());
        WeakReference<ClassLoader> wr = testShadow(originalTestClass);
        System.out.println("Test passed, waiting for ClassLoader to disappear");
        long deadline = System.currentTimeMillis() + 20*1000;
        Reference<? extends ClassLoader> ref;
        while (wr.get() != null && System.currentTimeMillis() < deadline) {
            System.gc();
            Thread.sleep(100);
        }
        if (wr.get() != null)
            fail(originalTestClass.getName() + " kept ClassLoader reference");
    }
    private static WeakReference<ClassLoader>
            testShadow(Class<?> originalTestClass) throws Exception {
        URLClassLoader originalLoader =
                (URLClassLoader) originalTestClass.getClassLoader();
        URL[] urls = originalLoader.getURLs();
        URLClassLoader shadowLoader =
                new ShadowClassLoader(urls, originalLoader.getParent());
        System.out.println("Shadow loader is " + shadowLoader);
        String className = originalTestClass.getName();
        Class<?> testClass = Class.forName(className, false, shadowLoader);
        if (testClass.getClassLoader() != shadowLoader) {
            throw new IllegalArgumentException("Loader didn't work: " +
                    testClass.getClassLoader() + " != " + shadowLoader);
        }
        Method main = testClass.getMethod("main", String[].class);
        main.invoke(null, (Object) new String[0]);
        return new WeakReference<ClassLoader>(shadowLoader);
    }
    private static void fail(String why) {
        System.out.println("FAILED: " + why);
        failure = why;
    }
    private static String failure;
}
