public class Basic1 {
    public static void main(String[] args) {
        System.err.println(
            "\nBasic functional test of dynamic proxy API, part 1\n");
        try {
            Class[] interfaces =
                new Class[] { Runnable.class, Observer.class };
            ClassLoader loader = ClassLoader.getSystemClassLoader();
            Class proxyClass = Proxy.getProxyClass(loader, interfaces);
            System.err.println("+ generated proxy class: " + proxyClass);
            int flags = proxyClass.getModifiers();
            System.err.println(
                "+ proxy class's modifiers: " + Modifier.toString(flags));
            if (!Modifier.isPublic(flags)) {
                throw new RuntimeException("proxy class in not public");
            }
            if (!Modifier.isFinal(flags)) {
                throw new RuntimeException("proxy class in not final");
            }
            if (Modifier.isAbstract(flags)) {
                throw new RuntimeException("proxy class in abstract");
            }
            for (int i = 0; i < interfaces.length; i++) {
                if (!interfaces[i].isAssignableFrom(proxyClass)) {
                    throw new RuntimeException(
                        "proxy class not assignable to proxy interface " +
                        interfaces[i].getName());
                }
            }
            List l1 = Arrays.asList(interfaces);
            List l2 = Arrays.asList(proxyClass.getInterfaces());
            System.err.println("+ proxy class's interfaces: " + l2);
            if (!l1.equals(l2)) {
                throw new RuntimeException(
                    "proxy class interfaces are " + l2 +
                    " (expected " + l1 + ")");
            }
            if (Proxy.isProxyClass(Object.class)) {
                throw new RuntimeException(
                    "Proxy.isProxyClass returned true for java.lang.Object");
            }
            if (!Proxy.isProxyClass(proxyClass)) {
                throw new RuntimeException(
                    "Proxy.isProxyClass returned false for proxy class");
            }
            ProtectionDomain pd = proxyClass.getProtectionDomain();
            System.err.println("+ proxy class's protextion domain: " + pd);
            if (!pd.implies(new AllPermission())) {
                throw new RuntimeException(
                    "proxy class does not have AllPermission");
            }
            Constructor cons = proxyClass.getConstructor(
                new Class[] { InvocationHandler.class });
            Handler handler = new Handler();
            Object proxy = cons.newInstance(new Object[] { handler });
            handler.currentProxy = proxy;
            Method m = Runnable.class.getMethod("run", null);
            ((Runnable) proxy).run();
            if (!handler.lastMethod.equals(m)) {
                throw new RuntimeException(
                    "proxy method invocation failure (lastMethod = " +
                        handler.lastMethod + ")");
            }
            System.err.println("\nTEST PASSED");
        } catch (Exception e) {
            System.err.println("\nTEST FAILED:");
            e.printStackTrace();
            throw new RuntimeException("TEST FAILED: " + e.toString());
        }
    }
    public static class Handler implements InvocationHandler {
        Object currentProxy;
        Method lastMethod;
        public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable
        {
            if (proxy != currentProxy) {
                throw new RuntimeException(
                    "wrong proxy instance passed to invoke method");
            }
            lastMethod = method;
            return null;
        }
    }
}
