public class MBSFPreStartPostStartTest {
    public static class MBSFInvocationHandler implements InvocationHandler {
        public static MBeanServerForwarder newProxyInstance() {
            final InvocationHandler handler = new MBSFInvocationHandler();
            final Class[] interfaces =
                new Class[] {MBeanServerForwarder.class};
            Object proxy = Proxy.newProxyInstance(
                                 MBeanServerForwarder.class.getClassLoader(),
                                 interfaces,
                                 handler);
            return MBeanServerForwarder.class.cast(proxy);
        }
        public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
            final String methodName = method.getName();
            if (methodName.equals("getMBeanServer")) {
                return mbs;
            }
            if (methodName.equals("setMBeanServer")) {
                if (args[0] == null)
                    throw new IllegalArgumentException("Null MBeanServer");
                if (mbs != null)
                    throw new IllegalArgumentException("MBeanServer object " +
                                                       "already initialized");
                mbs = (MBeanServer) args[0];
                return null;
            }
            flag = true;
            return method.invoke(mbs, args);
        }
        public boolean getFlag() {
            return flag;
        }
        public void setFlag(boolean flag) {
            this.flag = flag;
        }
        private boolean flag;
        private MBeanServer mbs;
    }
    public int runTest(boolean setBeforeStart) throws Exception {
        echo("=-=-= MBSFPreStartPostStartTest: Set MBSF " +
             (setBeforeStart ? "before" : "after") +
             " starting the connector server =-=-=");
        JMXConnectorServer server = null;
        JMXConnector client = null;
        final MBeanServer mbs = MBeanServerFactory.createMBeanServer();
        try {
            final JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:
            server = JMXConnectorServerFactory.newJMXConnectorServer(url,
                                                                     null,
                                                                     mbs);
            MBeanServerForwarder mbsf =
                MBSFInvocationHandler.newProxyInstance();
            if (setBeforeStart)
                server.setMBeanServerForwarder(mbsf);
            server.start();
            if (!setBeforeStart)
                server.setMBeanServerForwarder(mbsf);
            client = server.toJMXConnector(null);
            client.connect(null);
            final MBeanServerConnection mbsc =
                client.getMBeanServerConnection();
            mbsc.getDefaultDomain();
            MBSFInvocationHandler mbsfih =
                (MBSFInvocationHandler) Proxy.getInvocationHandler(mbsf);
            if (mbsfih.getFlag() == true) {
                echo("OK: Did go into MBeanServerForwarder!");
            } else {
                echo("KO: Didn't go into MBeanServerForwarder!");
                return 1;
            }
        } catch (Exception e) {
            echo("Failed to perform operation: " + e);
            return 1;
        } finally {
            if (client != null)
                client.close();
            if (server != null)
                server.stop();
            if (mbs != null)
                MBeanServerFactory.releaseMBeanServer(mbs);
        }
        return 0;
    }
    private static void echo(String message) {
        System.out.println(message);
    }
    public static void main (String args[]) throws Exception {
        int error = 0;
        MBSFPreStartPostStartTest test = new MBSFPreStartPostStartTest();
        error += test.runTest(true);
        error += test.runTest(false);
        if (error > 0) {
            echo(">>> Unhappy Bye, Bye!");
            throw new IllegalStateException(
                "Test FAILED: Unexpected error!");
        } else {
            echo(">>> Happy Bye, Bye!");
        }
    }
}
