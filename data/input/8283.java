public class ToStub implements RemoteInterface {
    public Object passObject(Object obj) {
        return obj;
    }
    public static void main(String[] args) throws Exception {
        RemoteInterface server1 = null;
        RemoteInterface server2 = null;
        RemoteInterface stub = null;
        RemoteInterface proxy = null;
        try {
            System.setProperty("java.rmi.server.ignoreStubClasses", "true");
            if (System.getSecurityManager() == null) {
                System.setSecurityManager(new SecurityManager());
            }
            System.err.println("export objects");
            server1 = new ToStub();
            server2 = new ToStub();
            stub = (RemoteInterface) UnicastRemoteObject.exportObject(server1);
            proxy = (RemoteInterface)
                UnicastRemoteObject.exportObject(server2, 0);
            System.err.println("test toStub");
            if (stub != RemoteObject.toStub(server1)) {
                throw new RuntimeException(
                    "toStub returned incorrect value for server1");
            }
            if (!Proxy.isProxyClass(proxy.getClass())) {
                throw new RuntimeException("proxy is not a dynamic proxy");
            }
            if (proxy != RemoteObject.toStub(server2)) {
                throw new RuntimeException(
                    "toStub returned incorrect value for server2");
            }
            try {
                RemoteObject.toStub(new ToStub());
                throw new RuntimeException(
                    "stub returned for exported object!");
            } catch (NoSuchObjectException nsoe) {
            }
            System.err.println("invoke methods");
            Object obj = stub.passObject(stub);
            if (!stub.equals(obj)) {
                throw new RuntimeException("returned stub not equal");
            }
            obj = proxy.passObject(proxy);
            if (!proxy.equals(obj)) {
                throw new RuntimeException("returned proxy not equal");
            }
            System.err.println("TEST PASSED");
        } finally {
            if (stub != null) {
                UnicastRemoteObject.unexportObject(server1, true);
            }
            if (proxy != null) {
                UnicastRemoteObject.unexportObject(server2, true);
            }
        }
    }
}
interface RemoteInterface extends Remote {
    Object passObject(Object obj) throws IOException;
}
