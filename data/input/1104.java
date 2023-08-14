public class ResolveProxyClass {
    private static class TestObjectInputStream extends ObjectInputStream {
        TestObjectInputStream() throws IOException {
            super();
        }
        protected Class resolveProxyClass(String[] interfaces)
            throws IOException, ClassNotFoundException
        {
            return super.resolveProxyClass(interfaces);
        }
    }
    public static void main(String[] args) {
        System.err.println("\nRegression test for bug 4258644\n");
        try {
            Thread.currentThread().setContextClassLoader(null);
            ClassLoader expectedLoader = ResolveProxyClass.class.getClassLoader();
            TestObjectInputStream in = new TestObjectInputStream();
            Class proxyClass = in.resolveProxyClass(
                new String[] { Runnable.class.getName() });
            ClassLoader proxyLoader = proxyClass.getClassLoader();
            System.err.println("proxy class \"" + proxyClass +
                "\" defined in loader: " + proxyLoader);
            if (proxyLoader != expectedLoader) {
                throw new RuntimeException(
                    "proxy class defined in loader: " + proxyLoader);
            }
            System.err.println("\nTEST PASSED");
        } catch (Throwable e) {
            System.err.println("\nTEST FAILED:");
            e.printStackTrace();
            throw new RuntimeException("TEST FAILED: " + e.toString());
        }
    }
}
