public class DGCAckFailure implements ReturnRemote {
    private static final long TIMEOUT = 20000;
    public Object returnRemote() {
        return new Wrapper(this);
    }
    public static void main(String[] args) throws Exception {
        System.setProperty("sun.rmi.dgc.ackTimeout", "10000");
        RMISocketFactory.setSocketFactory(new TestSF());
        System.err.println("test socket factory set");
        Remote impl = new DGCAckFailure();
        ReferenceQueue refQueue = new ReferenceQueue();
        Reference weakRef = new WeakReference(impl, refQueue);
        ReturnRemote stub =
            (ReturnRemote) UnicastRemoteObject.exportObject(impl);
        System.err.println("remote object exported; stub = " + stub);
        try {
            Object wrappedStub = stub.returnRemote();
            System.err.println("invocation returned: " + wrappedStub);
            impl = null;
            stub = null;        
            System.err.println("strong references to impl cleared");
            System.err.println("waiting for weak reference notification:");
            Reference ref = null;
            for (int i = 0; i < 6; i++) {
                System.gc();
                ref = refQueue.remove(TIMEOUT / 5);
                if (ref != null) {
                    break;
                }
            }
            if (ref == weakRef) {
                System.err.println("TEST PASSED");
            } else {
                throw new RuntimeException("TEST FAILED: " +
                    "timed out, remote object not garbage collected");
            }
        } finally {
            try {
                UnicastRemoteObject.unexportObject((Remote) weakRef.get(),
                                                   true);
            } catch (Exception e) {
            }
        }
    }
    private static class Wrapper implements Serializable {
        private final Remote obj;
        Wrapper(Remote obj) { this.obj = obj; }
        private void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException
        {
            TestSF.shutdownClientOutput();
            System.err.println(
                "Wrapper.readObject: SHUTTING DOWN CLIENT OUTPUT");
            in.defaultReadObject();
        }
        public String toString() { return "Wrapper[" + obj + "]"; }
    }
    private static class TestSF extends RMISocketFactory {
        private static volatile boolean shutdown = false;
        static void shutdownClientOutput() { shutdown = true; }
        public Socket createSocket(String host, int port) throws IOException {
            if (shutdown) {
                IOException e = new java.net.ConnectException(
                    "test socket factory rejecting client connection");
                System.err.println(e);
                throw e;
            } else {
                return new TestSocket(host, port);
            }
        }
        public ServerSocket createServerSocket(int port) throws IOException {
            return new ServerSocket(port);
        }
        private static class TestSocket extends Socket {
            TestSocket(String host, int port) throws IOException {
                super(host, port);
            }
            public OutputStream getOutputStream() throws IOException {
                return new TestOutputStream(super.getOutputStream());
            }
        }
        private static class TestOutputStream extends FilterOutputStream {
            TestOutputStream(OutputStream out) { super(out); }
            public void write(int b) throws IOException {
                if (shutdown) {
                    IOException e = new IOException(
                        "connection broken by test socket factory");
                    System.err.println(e);
                    throw e;
                } else {
                    super.write(b);
                }
            }
        }
    }
}
