public class DGCImplInsulation implements java.rmi.Remote {
    private static final long TIMEOUT = 5000;
    public static void main(String[] args) throws Exception {
        TestLibrary.suggestSecurityManager(null);
        Permissions perms = new Permissions();
        perms.add(new SocketPermission("*:1024-", "listen"));
        AccessControlContext acc =
            new AccessControlContext(new ProtectionDomain[] {
                new ProtectionDomain(
                    new CodeSource(null, (Certificate[]) null), perms) });
        Remote impl = new DGCImplInsulation();;
        try {
            Remote stub = (Remote) java.security.AccessController.doPrivileged(
                new ExportAction(impl));
            System.err.println("exported remote object; local stub: " + stub);
            MarshalledObject mobj = new MarshalledObject(stub);
            stub = (Remote) mobj.get();
            System.err.println("marshalled/unmarshalled stub: " + stub);
            ReferenceQueue refQueue = new ReferenceQueue();
            Reference weakRef = new WeakReference(impl, refQueue);
            impl = null;
            System.gc();
            if (refQueue.remove(TIMEOUT) == weakRef) {
                throw new RuntimeException(
                    "TEST FAILED: remote object garbage collected");
            } else {
                System.err.println("TEST PASSED");
                stub = null;
                System.gc();
                Thread.sleep(2000);
                System.gc();
            }
        } finally {
            try {
                UnicastRemoteObject.unexportObject(impl, true);
            } catch (Exception e) {
            }
        }
    }
    private static class ExportAction implements PrivilegedExceptionAction {
        private final Remote impl;
        ExportAction(Remote impl) {
            this.impl = impl;
        }
        public Object run() throws Exception {
            return UnicastRemoteObject.exportObject(impl);
        }
    }
}
