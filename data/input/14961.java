public class LoadProxyClasses {
    private static URL publicUrl = null;
    public static boolean boomerangSemantics = false;
    public static void main(String[] args) {
        try {
            System.err.println("\nFunctional test to verify that RMI " +
                               "loads proxy classes correctly\n");
            publicUrl =
                TestLibrary.installClassInCodebase("PublicInterface",
                                                   "public");
            URL publicUrl1 =
                TestLibrary.installClassInCodebase("PublicInterface1",
                                                   "public1");
            URL nonpublicUrl =
                TestLibrary.installClassInCodebase("NonpublicInterface",
                                                   "nonpublic", false);
            URL nonpublicUrl1 =
                TestLibrary.installClassInCodebase("NonpublicInterface1",
                                                   "nonpublic1", false);
            URL bothNonpublicUrl =
                TestLibrary.installClassInCodebase("NonpublicInterface",
                                                   "bothNonpublic");
            TestLibrary.installClassInCodebase("NonpublicInterface1",
                                               "bothNonpublic");
            URL fnnUrl =
                TestLibrary.installClassInCodebase("FnnClass", "fnn");
            TestLibrary.suggestSecurityManager(null);
            ClassLoader grandParentPublic =
                new URLClassLoader(new URL[] {publicUrl});
            ClassLoader parentNonpublic =
                new URLClassLoader(new URL[] {nonpublicUrl},
                                   grandParentPublic);
            URLClassLoader fnnLoader1 =
                new URLClassLoader(new URL[] {fnnUrl}, parentNonpublic);
            Class nonpublicInterface =
                fnnLoader1.loadClass("NonpublicInterface");
            Class publicInterface =
                fnnLoader1.loadClass("PublicInterface");
            Proxy proxy1 = (Proxy) Proxy.newProxyInstance(parentNonpublic,
                new Class[] {nonpublicInterface, publicInterface},
                new TestInvocationHandler());
            unmarshalProxyClass(proxy1, fnnLoader1, parentNonpublic, 1, null);
            Class zipConstantsClass =
                Class.forName("java.util.zip.ZipConstants");
            URLClassLoader fnnLoader2 =
                new URLClassLoader(new URL[] {fnnUrl});
            Proxy proxy2 = (Proxy) Proxy.newProxyInstance(null,
                new Class[] {zipConstantsClass, Checksum.class},
                new TestInvocationHandler());
            unmarshalProxyClass(proxy2, fnnLoader2,
                                (ClassLoader) null, 2, null);
            Thread currentThread = Thread.currentThread();
            ClassLoader fnnLoader3 = new URLClassLoader(
                new URL[] {publicUrl, fnnUrl});
            ClassLoader newCtxLoader =
                new URLClassLoader(new URL[] {publicUrl}, fnnLoader3);
            Class publicInterface3 =
                fnnLoader3.loadClass("PublicInterface");
            ClassLoader currentCtxLoader =
                currentThread.getContextClassLoader();
            currentThread.setContextClassLoader(newCtxLoader);
            Proxy proxy3 = (Proxy) Proxy.newProxyInstance(newCtxLoader,
                new Class[] {publicInterface3},
                new TestInvocationHandler());
            unmarshalProxyClass(proxy3, fnnLoader3, fnnLoader3,
                3, new Case3Checker());
            currentThread.setContextClassLoader(currentCtxLoader);
            ClassLoader bothNonpublicLoader =
                new URLClassLoader(new URL[] {bothNonpublicUrl});
            Class nonpublicInterface4a =
                bothNonpublicLoader.loadClass("NonpublicInterface");
            Class nonpublicInterface4b =
                bothNonpublicLoader.loadClass("NonpublicInterface1");
            Proxy proxy4 = (Proxy) Proxy.newProxyInstance(bothNonpublicLoader,
                new Class[] {nonpublicInterface4a, nonpublicInterface4b},
                new TestInvocationHandler());
            ClassLoader nonpublicLoaderA =
                new URLClassLoader(new URL[] {nonpublicUrl});
            ClassLoader nonpublicLoaderB =
                new URLClassLoader(new URL[] {nonpublicUrl1}, nonpublicLoaderA);
            currentCtxLoader =
                currentThread.getContextClassLoader();
            currentThread.setContextClassLoader(nonpublicLoaderB);
            IllegalAccessError illegal = null;
            try {
                unmarshalProxyClass(proxy4, fnnLoader2, nonpublicLoaderB,
                                    4, null);
            } catch (IllegalAccessError e) {
                illegal = e;
            }
            if (illegal == null) {
                TestLibrary.bomb("case4: IllegalAccessError not thrown " +
                                 "when multiple nonpublic interfaces have \n" +
                                 "different class loaders");
            } else {
                System.err.println("\ncase4: IllegalAccessError correctly " +
                                   "thrown \n when trying to load proxy " +
                                   "with multiple nonpublic interfaces in \n" +
                                   "  different class loaders");
            }
            currentThread.setContextClassLoader(currentCtxLoader);
            ClassLoader publicLoader =
                new URLClassLoader(new URL[] {publicUrl});
            Class publicInterface5 =
                publicLoader.loadClass("PublicInterface");
            Proxy proxy5 = (Proxy) Proxy.newProxyInstance(publicLoader,
                new Class[] {publicInterface5},
                new TestInvocationHandler());
            currentCtxLoader =
                currentThread.getContextClassLoader();
            currentThread.setContextClassLoader(publicLoader);
            unmarshalProxyClass(proxy5, fnnLoader2, publicLoader, 5,
                                new Case5Checker());
            currentThread.setContextClassLoader(currentCtxLoader);
            ClassLoader fnnLoader6 =
                new URLClassLoader(new URL[] {fnnUrl, publicUrl});
            ClassLoader publicLoader6 =
                new URLClassLoader(new URL[] {publicUrl1}, fnnLoader6);
            Class publicInterface6a =
                publicLoader6.loadClass("PublicInterface1");
            Class publicInterface6b =
                fnnLoader6.loadClass("PublicInterface");
            Proxy proxy6 = (Proxy) Proxy.newProxyInstance(publicLoader6,
                new Class[] {publicInterface6a, publicInterface6b},
                new TestInvocationHandler());
            ClassNotFoundException cnfe = null;
            try {
                unmarshalProxyClass(proxy6, fnnLoader6, publicLoader6, 6,
                                    null);
            } catch (ClassNotFoundException e) {
                cnfe = e;
            }
            if (cnfe == null) {
                TestLibrary.bomb("ClassNotFoundException not thrown " +
                                 "when not all proxy interfaces could " +
                                 " be found in a single class loader ");
            } else {
                System.err.println("Case6: ClassNotFoundException " +
                                   "correctly thrown when not all proxy" +
                                   " interfaces could be found in a " +
                                   "single class loader");
                cnfe.printStackTrace();
            }
            System.err.println("TEST PASSED");
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            TestLibrary.bomb(e);
        }
    }
    private interface LoadChecker {
        void checkLoad(Proxy proxy, ClassLoader expectedLoader);
    }
    private static Proxy unmarshalProxyClass(Proxy proxy,
                                             ClassLoader fnnLoader,
                                             ClassLoader expectedLoader,
                                             int n,
                                             LoadChecker checker)
        throws ClassNotFoundException, IOException,
               InstantiationException, IllegalAccessException
    {
        FnnUnmarshal fnnUnmarshal = (FnnUnmarshal)
                fnnLoader.loadClass("FnnClass").newInstance();
        Proxy unmarshalled = (Proxy)
            fnnUnmarshal.unmarshal(new MarshalledObject(proxy));
        ClassLoader unmarshalledLoader =
            unmarshalled.getClass().getClassLoader();
        if (checker != null) {
            checker.checkLoad(unmarshalled, expectedLoader);
        } else {
            if (unmarshalledLoader != expectedLoader) {
                TestLibrary.bomb("case" + n + ": proxy class not " +
                                 "placed into incorrect loader: " +
                                 unmarshalledLoader);
            } else {
                System.err.println("\ncase" + n + ": proxy class correctly" +
                                   " placed into expected loader: " +
                                   expectedLoader);
            }
        }
        return proxy;
    }
    private static class Case3Checker implements LoadChecker {
        public void checkLoad(Proxy proxy, ClassLoader expectedLoader) {
            ClassLoader ifaceLoader =
                proxy.getClass().getInterfaces()[0].getClassLoader();
            ClassLoader proxyLoader = proxy.getClass().getClassLoader();
            boolean proxyOk = false;
            if (boomerangSemantics) {
                ClassLoader ctxLoader =
                    Thread.currentThread().getContextClassLoader();
                if (proxyLoader == ctxLoader) {
                    proxyOk = true;
                }
            } else if (proxyLoader.getClass().
                       getName().indexOf("sun.rmi") >= 0)
            {
                proxyOk = true;
            }
            if (proxyOk) {
                System.err.println("\ncase3: proxy loaded in" +
                                   " correct loader: " + proxyLoader +
                                   Arrays.asList(((URLClassLoader)
                                                 proxyLoader).getURLs()));
            } else {
                TestLibrary.bomb("case3: proxy class loaded in " +
                                 "incorrect loader: " + proxyLoader +
                                   Arrays.asList(((URLClassLoader)
                                                  proxyLoader).getURLs()));
            }
            if (ifaceLoader == expectedLoader) {
                System.err.println("case3: proxy interface loaded in" +
                                   " correct loader: " + ifaceLoader);
            } else {
                TestLibrary.bomb("public proxy interface loaded in " +
                                 "incorrect loader: " + ifaceLoader);
            }
        }
    }
    private static class Case5Checker implements LoadChecker {
        public void checkLoad(Proxy proxy, ClassLoader expectedLoader) {
            ClassLoader proxyLoader = proxy.getClass().getClassLoader();
            String proxyAnnotation =
                RMIClassLoader.getClassAnnotation(proxy.getClass());
            if ((proxyAnnotation == null) ||
                !proxyAnnotation.equals(publicUrl.toString()))
            {
                TestLibrary.bomb("proxy class had incorrect annotation: " +
                                 proxyAnnotation);
            } else {
                System.err.println("proxy class had correct annotation: " +
                                   proxyAnnotation);
            }
            boolean proxyOk = false;
            if (boomerangSemantics) {
                ClassLoader ctxLoader =
                    Thread.currentThread().getContextClassLoader();
                if (proxyLoader == ctxLoader) {
                    proxyOk = true;
                }
            } else if (proxyLoader.getClass().
                       getName().indexOf("sun.rmi") >= 0)
            {
                proxyOk = true;
            }
            if (proxyOk) {
                System.err.println("\ncase5: proxy loaded from" +
                                   " correct loader: " + proxyLoader);
            } else {
                TestLibrary.bomb("case5: proxy interface loaded from " +
                                 "incorrect loader: " + proxyLoader);
            }
        }
    }
    private static class TestInvocationHandler
        implements InvocationHandler, Serializable
    {
        public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {return null;}
    }
}
