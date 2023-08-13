public class StartStopTest {
    static int maxPoolSize;
    static final AtomicInteger counter = new AtomicInteger();
    public class ObservedObject implements ObservedObjectMBean {
        public boolean called = false;
        public Integer getInteger() {
            task("Integer");
            return 0;
        }
        public Double getDouble() {
            task("Double");
            return 0.0;
        }
        public String getString() {
            task("String");
            return "";
        }
        private void task(String prop) {
            called = true;
            final int c = counter.incrementAndGet();
            echo("\tTASK [" + c + "] in get" + prop);
        }
    }
    public interface ObservedObjectMBean {
        public Integer getInteger();
        public Double getDouble();
        public String getString();
    }
    public int runTest(int monitorType) throws Exception {
        int nTasks = maxPoolSize + 2;
        ObjectName[] mbeanNames = new ObjectName[nTasks];
        ObservedObject[] monitored = new ObservedObject[nTasks];
        ObjectName[] monitorNames = new ObjectName[nTasks];
        Monitor[] monitor = new Monitor[nTasks];
        String[] attributes = { "Integer", "Double", "String" };
        try {
            echo(">>> CREATE MBeanServer");
            MBeanServer server = MBeanServerFactory.newMBeanServer();
            String domain = server.getDefaultDomain();
            for (int i = 0; i < nTasks; i++) {
                mbeanNames[i] =
                    new ObjectName(":type=ObservedObject,instance=" + (i + 1));
                monitored[i] = new ObservedObject();
                echo(">>> CREATE ObservedObject = " + mbeanNames[i].toString());
                server.registerMBean(monitored[i], mbeanNames[i]);
                switch (monitorType) {
                case 1:
                    monitorNames[i] = new ObjectName(":type=CounterMonitor," +
                                                     "instance=" + (i + 1));
                    monitor[i] = new CounterMonitor();
                    break;
                case 2:
                    monitorNames[i] = new ObjectName(":type=GaugeMonitor," +
                                                     "instance=" + (i + 1));
                    monitor[i] = new GaugeMonitor();
                    break;
                case 3:
                    monitorNames[i] = new ObjectName(":type=StringMonitor," +
                                                     "instance=" + (i + 1));
                    monitor[i] = new StringMonitor();
                    break;
                default:
                    echo("Unsupported monitor type");
                    return 1;
                }
                echo(">>> CREATE Monitor = " + monitorNames[i].toString());
                server.registerMBean(monitor[i], monitorNames[i]);
                monitor[i].addObservedObject(mbeanNames[i]);
                monitor[i].setObservedAttribute(attributes[monitorType-1]);
                monitor[i].setGranularityPeriod(50);
            }
            for (int j = 0; j < 2; j++) {
                echo(">>> Start MONITORS");
                for (int i = 0; i < nTasks; i++)
                    monitor[i].start();
                echo(">>> MONITORS started");
                Thread.sleep(500);
                echo(">>> Check FLAGS true");
                for (int i = 0; i < nTasks; i++)
                    if (!monitored[i].called) {
                        echo("KO: At least one attribute was not called");
                        return 1;
                    }
                echo(">>> FLAGS checked true");
                echo(">>> Stop MONITORS");
                for (int i = 0; i < nTasks; i++)
                    monitor[i].stop();
                echo(">>> MONITORS stopped");
                Thread.sleep(500);
                echo(">>> Set FLAGS to false");
                for (int i = 0; i < nTasks; i++)
                    monitored[i].called = false;
                echo(">>> FLAGS set to false");
                echo(">>> Check FLAGS remain false");
                for (int i = 0; i < nTasks; i++)
                    if (monitored[i].called) {
                        echo("KO: At least one attribute " +
                             "continued to get called");
                        return 1;
                    }
                echo(">>> FLAGS checked false");
            }
        } finally {
            for (int i = 0; i < nTasks; i++)
                if (monitor[i] != null)
                    monitor[i].stop();
        }
        return 0;
    }
    private static void echo(String message) {
        System.out.println(message);
    }
    public static void main (String args[]) throws Exception {
        Integer size = Integer.getInteger("jmx.x.monitor.maximum.pool.size");
        if (size == null) {
            maxPoolSize = 10;
            echo(">>> MAXIMUM POOL SIZE = 10 [default value]");
        } else {
            maxPoolSize = size.intValue() < 1 ? 1 : size.intValue();
            echo(">>> MAXIMUM POOL SIZE = " + maxPoolSize);
        }
        StartStopTest test = new StartStopTest();
        int error = test.runTest(Integer.parseInt(args[0]));
        if (error > 0) {
            echo(">>> Unhappy Bye, Bye!");
            throw new IllegalStateException(
                "Test FAILED: Unexpected Maximum Pool Size Overflow!");
        } else {
            echo(">>> Happy Bye, Bye!");
        }
    }
}
