public class ThreadPoolAccTest {
    public class ObservedObject implements ObservedObjectMBean {
        public String principal;
        public Integer getInteger() {
            setPrincipal();
            return 0;
        }
        public Double getDouble() {
            setPrincipal();
            return 0.0;
        }
        public String getString() {
            setPrincipal();
            return "";
        }
        private void setPrincipal() {
            Subject subject = Subject.getSubject(AccessController.getContext());
            Set principals = subject.getPrincipals(JMXPrincipal.class);
            principal = ((Principal) principals.iterator().next()).getName();
        }
    }
    public interface ObservedObjectMBean {
        public Integer getInteger();
        public Double getDouble();
        public String getString();
    }
    public int runTest() throws Exception {
        ObjectName[] mbeanNames = new ObjectName[6];
        ObservedObject[] monitored = new ObservedObject[6];
        ObjectName[] monitorNames = new ObjectName[6];
        Monitor[] monitor = new Monitor[6];
        String[] principals = { "role1", "role2" };
        String[] attributes = { "Integer", "Double", "String" };
        try {
            echo(">>> CREATE MBeanServer");
            MBeanServer server = MBeanServerFactory.newMBeanServer();
            String domain = server.getDefaultDomain();
            for (int i = 0; i < 6; i++) {
                mbeanNames[i] =
                    new ObjectName(":type=ObservedObject,instance=" + i);
                monitored[i] = new ObservedObject();
                echo(">>> CREATE ObservedObject = " + mbeanNames[i].toString());
                server.registerMBean(monitored[i], mbeanNames[i]);
                switch (i) {
                    case 0:
                    case 3:
                        monitorNames[i] =
                            new ObjectName(":type=CounterMonitor,instance=" + i);
                        monitor[i] = new CounterMonitor();
                        break;
                    case 1:
                    case 4:
                        monitorNames[i] =
                            new ObjectName(":type=GaugeMonitor,instance=" + i);
                        monitor[i] = new GaugeMonitor();
                        break;
                    case 2:
                    case 5:
                        monitorNames[i] =
                            new ObjectName(":type=StringMonitor,instance=" + i);
                        monitor[i] = new StringMonitor();
                        break;
                }
                echo(">>> CREATE Monitor = " + monitorNames[i].toString());
                server.registerMBean(monitor[i], monitorNames[i]);
                monitor[i].addObservedObject(mbeanNames[i]);
                monitor[i].setObservedAttribute(attributes[i % 3]);
                monitor[i].setGranularityPeriod(500);
                final Monitor m = monitor[i];
                Subject subject = new Subject();
                echo(">>> RUN Principal = " + principals[i / 3]);
                subject.getPrincipals().add(new JMXPrincipal(principals[i / 3]));
                PrivilegedAction action = new PrivilegedAction() {
                    public Object run() {
                        m.start();
                        return null;
                    }
                };
                Subject.doAs(subject, action);
            }
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                echo("I fell asleep but someone woke me up");
                return 1;
            }
            for (int i = 0; i < 6; i++) {
                echo(">>> Monitor = " + monitorNames[i]);
                echo(">>> ObservedObject = " +
                     monitor[i].getObservedObject());
                echo(">>> ObservedAttribute = " +
                     monitor[i].getObservedAttribute());
                echo(">>> Principal = " + monitored[i].principal);
                if (monitored[i].principal.equals(principals[i / 3])) {
                    echo("\tOK: Got Expected Principal");
                } else {
                    echo("\tKO: Got Unexpected Principal");
                    return 1;
                }
            }
        } finally {
            for (int i = 0; i < 6; i++)
                if (monitor[i] != null)
                    monitor[i].stop();
        }
        return 0;
    }
    private static void echo(String message) {
        System.out.println(message);
    }
    public static void main (String args[]) throws Exception {
        ThreadPoolAccTest test = new ThreadPoolAccTest();
        int error = test.runTest();
        if (error > 0) {
            echo(">>> Unhappy Bye, Bye!");
            throw new IllegalStateException(
                "Test FAILED: Monitor task ran on wrong security context!");
        } else {
            echo(">>> Happy Bye, Bye!");
        }
    }
}
