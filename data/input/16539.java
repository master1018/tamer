public class ProxyObjectMethodsTest {
    public static void main(String[] args) throws Exception {
        System.out.println("<<< Test how to execute the 3 Object methods by a Proxy.");
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        final ObjectName name = new ObjectName(":class=Simple");
        JMXServiceURL url = new JMXServiceURL("rmi", null, 0);
        final JMXConnectorServer server =
            JMXConnectorServerFactory.newJMXConnectorServer(url, null, mbs);
        server.start();
        url = server.getAddress();
        final JMXConnector client = JMXConnectorFactory.connect(url);
        System.out.println("<<< Test the methods at local side.");
        final Simple simple = new Simple();
        mbs.registerMBean(simple, name);
        SimpleMBean simple0 =
            MBeanServerInvocationHandler.newProxyInstance(client.getMBeanServerConnection(),
                                                          name,
                                                          SimpleMBean.class,
                                                          false);
        SimpleMBean simple1 =
            MBeanServerInvocationHandler.newProxyInstance(client.getMBeanServerConnection(),
                                                          name,
                                                          SimpleMBean.class,
                                                          false);
        Simplest simple3 =
            MBeanServerInvocationHandler.newProxyInstance(client.getMBeanServerConnection(),
                                                          name,
                                                          Simplest.class,
                                                          false);
        if (!simple0.equals(simple1) ||
            simple0.equals(simple) ||
            simple0.equals(simple3)) {
            throw new RuntimeException("The method equals does not work correctly.");
        }
        if (simple0.hashCode() != simple1.hashCode() ||
            simple.hashCode() == simple0.hashCode()) {
            throw new RuntimeException("The method hashCode does not work correctly.");
        }
        if (!simple0.toString().equals(simple1.toString()) ||
            simple.toString().equals(simple0.toString())) {
            throw new RuntimeException("The method toString does not work correctly.");
        }
        if (!simple0.equals("foo").equals("foo"))
            throw new RuntimeException("The method equals(String) was not forwarded.");
        ArrayList al = new ArrayList();
        al.add(simple0);
        if (!al.contains(simple0) || !al.contains(simple1)) {
            throw new RuntimeException("Cannot find correctly a proxy in an ArrayList.");
        }
        System.out.println("<<< Test whether the methods are done at server side.");
        final ObjectName name1 = new ObjectName(":class=Test");
        mbs.registerMBean(new Test(), name1);
        TestMBean test0 = MBeanServerInvocationHandler.newProxyInstance(mbs,
                                                                name1,
                                                                TestMBean.class,
                                                                false);
        if(test0.equals(test0)) {
            throw new RuntimeException("The method equals is not done remotely as expected.");
        }
        if (!test0.toString().equals("Test-toString")) {
            throw new RuntimeException("The method toString is not done remotely as expected.");
        }
        if (test0.hashCode() != 123) {
            throw new RuntimeException("The method hashCode is not done remotely as expected.");
        }
        System.out.println("<<< Test on using a null connection or a null name.");
        SimpleMBean simple2;
        try {
            simple2 = MBeanServerInvocationHandler.newProxyInstance(null,
                                                                name,
                                                                SimpleMBean.class,
                                                                false);
            throw new RuntimeException(
                  "Null connection does not cause an IllegalArgumentException.");
        } catch (IllegalArgumentException ie) {
        }
        try {
            simple2 = MBeanServerInvocationHandler.newProxyInstance(mbs,
                                                                null,
                                                                SimpleMBean.class,
                                                                false);
            throw new RuntimeException(
                  "Null object name does not cause an IllegalArgumentException.");
        } catch (IllegalArgumentException ie) {
        }
    }
    public static interface Simplest {
    }
    public static interface SimpleMBean extends Simplest {
        public String equals(String x);
    }
    private static class Simple implements SimpleMBean {
        public String equals(String x) {
            return x;
        }
    }
    public static interface TestMBean {
        public boolean equals(Object o);
        public String toString();
        public int hashCode();
    }
    private static class Test implements TestMBean {
        public boolean equals(Object o) {
            return false;
        }
        public String toString() {
            return "Test-toString";
        }
        public int hashCode() {
            return 123;
        }
    }
}
