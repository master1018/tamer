public class RetryDirtyCalls implements Self, Unreferenced {
    private final static long TIMEOUT = 20000;
    private boolean unreferenced = false;
    public Self getSelf() {
        return this;
    }
    public void unreferenced() {
        synchronized (this) {
            unreferenced = true;
            notifyAll();
        }
    }
    public static void main(String[] args) {
        System.err.println("\nRegression test for bug 4268258\n");
        System.setProperty("java.rmi.dgc.leaseValue", "10000");
        System.setProperty("sun.rmi.dgc.checkInterval", "3000");
        System.setProperty("sun.rmi.transport.connectionTimeout", "100");
        RetryDirtyCalls impl = new RetryDirtyCalls();
        try {
            TestSF sf = new TestSF();
            RMISocketFactory.setSocketFactory(sf);
            Self stub = (Self) UnicastRemoteObject.exportObject(impl);
            Self dgcStub = stub.getSelf();
            stub = null;                
            final int FLAKE_FACTOR = 3;
            sf.setFlakeFactor(FLAKE_FACTOR);
            long deadline = System.currentTimeMillis() + TIMEOUT;
            boolean unreferenced;
            synchronized (impl) {
                while (!(unreferenced = impl.unreferenced)) {
                    long timeToWait = deadline - System.currentTimeMillis();
                    if (timeToWait > 0) {
                        impl.wait(timeToWait);
                    } else {
                        break;
                    }
                }
            }
            if (unreferenced) {
                throw new RuntimeException("remote object unreferenced");
            }
            int createCount = sf.getCreateCount();
            if (createCount == 0) {
                throw new RuntimeException("test socket factory never used");
            } else if (createCount < (FLAKE_FACTOR + 3)) {
                throw new RuntimeException(
                    "test failed because dirty calls not retried enough, " +
                    "but remote object not unreferenced");
            }
            System.err.println(
                "TEST PASSED: remote object not unreferenced");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("TEST FAILED: " + e.toString());
        } finally {
            try {
                UnicastRemoteObject.unexportObject(impl, true);
            } catch (Exception e) {
            }
        }
    }
}
class TestSF extends RMISocketFactory {
    private int flakeFactor = 0;
    private int flakeState = 0;
    private int createCount = 0;
    public synchronized void setFlakeFactor(int newFlakeFactor) {
        flakeFactor = newFlakeFactor;
    }
    public synchronized int getCreateCount() {
        return createCount;
    }
    public synchronized Socket createSocket(String host, int port)
        throws IOException
    {
        createCount++;
        if (++flakeState > flakeFactor) {
            flakeState = 0;
        }
        if (flakeState == 0) {
            return new Socket(host, port);
        } else {
            throw new IOException("random network failure");
        }
    }
    public ServerSocket createServerSocket(int port) throws IOException {
        return new ServerSocket(port);
    }
}
