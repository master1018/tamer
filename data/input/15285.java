public class UseDynamicProxies implements RemoteInterface {
    public Object passObject(Object obj) {
        return obj;
    }
    public int passInt(int x) {
        return x;
    }
    public String passString(String string) {
        return string;
    }
    public static void main(String[] args) throws Exception {
        RemoteInterface server = null;
        RemoteInterface proxy = null;
        try {
            System.setProperty("java.rmi.server.ignoreStubClasses", args[0]);
            boolean ignoreStubClasses = Boolean.parseBoolean(args[0]);
            if (System.getSecurityManager() == null) {
                System.setSecurityManager(new SecurityManager());
            }
            System.err.println("export object");
            server = new UseDynamicProxies();
            proxy =
                (RemoteInterface) UnicastRemoteObject.exportObject(server, 0);
            System.err.println("proxy = " + proxy);
            if (ignoreStubClasses) {
                if (!Proxy.isProxyClass(proxy.getClass())) {
                    throw new RuntimeException(
                        "server proxy is not a dynamic proxy");
                }
                if (!(Proxy.getInvocationHandler(proxy) instanceof
                      RemoteObjectInvocationHandler))
                {
                    throw new RuntimeException("invalid invocation handler");
                }
            } else if (!(proxy instanceof RemoteStub)) {
                throw new RuntimeException(
                    "server proxy is not a RemoteStub");
            }
            System.err.println("invoke methods");
            Object obj = proxy.passObject(proxy);
            if (!proxy.equals(obj)) {
                throw new RuntimeException("returned proxy not equal");
            }
            int x = proxy.passInt(53);
            if (x != 53) {
                throw new RuntimeException("returned int not equal");
            }
            String string = proxy.passString("test");
            if (!string.equals("test")) {
                throw new RuntimeException("returned string not equal");
            }
            System.err.println("TEST PASSED");
        } finally {
            if (proxy != null) {
                UnicastRemoteObject.unexportObject(server, true);
            }
        }
    }
}
interface RemoteInterface extends Remote {
    Object passObject(Object obj) throws IOException;
    int passInt(int x) throws IOException;
    String passString(String string) throws IOException;
}
