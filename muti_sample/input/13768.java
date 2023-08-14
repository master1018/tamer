public class ConnectorStopDeadlockTest {
    private static String failure;
    private static RMIConnectorServer connectorServer;
    public static void main(String[] args) throws Exception {
        JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        RMIJRMPServerImplSub impl = new RMIJRMPServerImplSub();
        System.out.println("Creating connectorServer");
        connectorServer = new RMIConnectorServer(url, null, impl, mbs);
        System.out.println("Starting connectorServer");
        connectorServer.start();
        System.out.println("Making client");
        RMIConnection cc = impl.newClient(null);
        System.out.println("Closing client");
        cc.close();
        if (connectorServer.isActive()) {
            System.out.println("Stopping connectorServer");
            connectorServer.stop();
        }
        if (failure == null)
            System.out.println("TEST PASSED, no deadlock");
        else
            System.out.println("TEST FAILED");
    }
    static void fail(Throwable e) {
        System.out.println("FAILED WITH EXCEPTION: " + e);
        e.printStackTrace(System.out);
        failure = e.toString();
    }
    static void fail(String s) {
        System.out.println("FAILED: " + s);
        failure = s;
    }
    static void waitForBlock(Thread t) {
        Thread currentThread = Thread.currentThread();
        System.out.println("waiting for thread " + t.getName() + " to block " +
                "on a lock held by thread " + currentThread.getName());
        ThreadMXBean tm = ManagementFactory.getThreadMXBean();
        while (true) {
            ThreadInfo ti = tm.getThreadInfo(t.getId());
            if (ti == null) {
                System.out.println("  thread has exited");
                return;
            }
            if (ti.getLockOwnerId() == currentThread.getId()) {
                System.out.println("  thread now blocked");
                return;
            }
            Thread.yield();
        }
    }
    public static class RMIJRMPServerImplSub extends RMIJRMPServerImpl {
        RMIJRMPServerImplSub() throws IOException {
            super(0, null, null, null);
        }
        public RMIConnection makeClient() throws IOException {
            return super.makeClient("connection id", null);
        }
        @Override
        protected void clientClosed(RMIConnection conn) throws IOException {
            System.out.println("clientClosed, will call connectorServer.stop");
            final Exchanger<Void> x = new Exchanger<Void>();
            Thread t = new Thread() {
                public void run() {
                    try {
                        connectorServer.stop();
                    } catch (Exception e) {
                        fail(e);
                    }
                }
            };
            t.setName("connectorServer.stop");
            t.start();
            waitForBlock(t);
            System.out.println("calling super.clientClosed");
            System.out.flush();
            super.clientClosed(conn);
        }
    }
}
