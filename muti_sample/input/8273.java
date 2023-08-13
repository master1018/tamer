public class DaemonRMIExporterTest {
    public static void main(String[] args) throws Exception {
        Set<Thread> initialNonDaemonThreads = getNonDaemonThreads();
        JMXServiceURL addr = new JMXServiceURL("rmi", null, 0);
        System.out.println("DaemonRMIExporterTest: Creating a RMIConnectorServer on " + addr);
        Map<String, ?> env =
            Collections.singletonMap("jmx.remote.x.daemon", "true");
        JMXConnectorServer server =
                JMXConnectorServerFactory.newJMXConnectorServer(addr,
                env,
                MBeanServerFactory.createMBeanServer());
        server.start();
        System.out.println("DaemonRMIExporterTest: Started the server on " + server.getAddress());
        System.out.println("DaemonRMIExporterTest: Connecting a client to the server ...");
        final JMXConnector conn = JMXConnectorFactory.connect(server.getAddress());
        conn.getMBeanServerConnection().getDefaultDomain();
        System.out.println("DaemonRMIExporterTest: Closing the client ...");
        conn.close();
        System.out.println("DaemonRMIExporterTest No more user code to execute, the VM should " +
                "exit normally, otherwise will be blocked forever if the bug is not fixed.");
        long deadline = System.currentTimeMillis() + 10000;
        ok: {
            while (System.currentTimeMillis() < deadline) {
                Set<Thread> nonDaemonThreads = getNonDaemonThreads();
                nonDaemonThreads.removeAll(initialNonDaemonThreads);
                if (nonDaemonThreads.isEmpty())
                    break ok;
                System.out.println("Non-daemon threads: " + nonDaemonThreads);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw new AssertionError(e);
                }
            }
            throw new Exception("TEST FAILED: non-daemon threads remain");
        }
        System.out.println("TEST PASSED");
    }
    private static Set<Thread> getNonDaemonThreads() {
        ThreadGroup tg = Thread.currentThread().getThreadGroup();
        while (tg.getParent() != null)
            tg = tg.getParent();
        Thread[] threads = null;
        for (int size = 10; size < 10000; size *= 2) {
            threads = new Thread[size];
            int n = tg.enumerate(threads, true);
            if (n < size) {
                threads = Arrays.copyOf(threads, n);
                break;
            }
        }
        Set<Thread> ndThreads = new HashSet<Thread>();
        for (Thread t : threads) {
            if (!t.isDaemon())
                ndThreads.add(t);
        }
        return ndThreads;
    }
}
