public class ThreadPoolTest {
    static int maxPoolSize;
    static int nTasks;
    private static Waiter waiter;
    static final long MAX_WAITING_TIME = 10000;
    public class ObservedObject implements ObservedObjectMBean {
        private boolean called = false;
        public Integer getInteger() {
            inform("getInteger()");
            return 0;
        }
        public Double getDouble() {
            inform("getDouble()");
            return 0.0;
        }
        public String getString() {
            inform("getString()");
            return "";
        }
        private void inform(String prop) {
            synchronized(waiter) {
                if (!called) {
                    called = true;
                    waiter.count();
                }
            }
            echo(">>> TASK "+prop+" is called.");
        }
    }
    public interface ObservedObjectMBean {
        public Integer getInteger();
        public Double getDouble();
        public String getString();
    }
    public int runTest(int monitorType) throws Exception {
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
                monitor[i].start();
            }
            if (!waiter.waiting(MAX_WAITING_TIME)) {
                echo("Error, not all "+nTasks+" monitor tasks are called after "
                     +MAX_WAITING_TIME);
                return 1;
            }
        } finally {
            for (int i = 0; i < nTasks; i++)
                if (monitor[i] != null)
                    monitor[i].stop();
        }
        echo("All "+nTasks+" monitors are called.");
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
        nTasks = maxPoolSize + 2;
        waiter = new Waiter(nTasks);
        ThreadPoolTest test = new ThreadPoolTest();
        int error = test.runTest(Integer.parseInt(args[0]));
        if (error > 0) {
            echo(">>> Unhappy Bye, Bye!");
            throw new IllegalStateException(
                "Test FAILED: Unexpected Maximum Pool Size Overflow!");
        } else {
            echo(">>> Happy Bye, Bye!");
        }
    }
    private static class Waiter {
        public Waiter(int waitedNB) {
            this.waitedNB = waitedNB;
        }
        public void count() {
            synchronized(this) {
                counted++;
                if (counted == waitedNB) {
                    this.notifyAll();
                }
            }
        }
        public boolean waiting(long timeout) {
            final long startTime = System.currentTimeMillis();
            long toWait = timeout;
            synchronized(this) {
                while(counted < waitedNB && toWait > 0) {
                    try {
                        this.wait(toWait);
                    } catch (InterruptedException ire) {
                        break;
                    }
                    toWait = timeout -
                        (System.currentTimeMillis() - startTime);
                }
            }
            return counted == waitedNB;
        }
        private int waitedNB;
        private int counted = 0;
    }
}
