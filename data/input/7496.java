public class CounterMonitorThresholdTest {
    private static int counter1[]      = { 0, 1, 2, 3, 4, 4, 5, 5, 0, 1, 2, 3, 4, 5, 0, 1 };
    private static int derivedGauge1[] = { 0, 1, 2, 3, 4, 4, 5, 5, 0, 1, 2, 3, 4, 5, 0, 1 };
    private static int threshold1[]    = { 1, 2, 3, 4, 5, 5, 1, 1, 1, 2, 3, 4, 5, 1, 1, 2 };
    private static int counter2[]      = { 0, 1, 2, 3, 3, 4, 4, 5, 0, 1, 2, 3, 4, 5, 0, 1 };
    private static int derivedGauge2[] = { 0, 1, 2, 3, 3, 4, 4, 5, 0, 1, 2, 3, 4, 5, 0, 1 };
    private static int threshold2[]    = { 1, 4, 4, 4, 4, 1, 1, 1, 1, 4, 4, 4, 1, 1, 1, 4 };
    public interface TestMBean {
        public int getCounter();
        public void setCounter(int count);
    }
    public static class Test implements TestMBean {
        public int getCounter() {
            return count;
        }
        public void setCounter(int count) {
            this.count = count;
        }
        private int count = 0;
    }
    public static class Listener implements NotificationListener {
        public void handleNotification(Notification n, Object hb) {
            System.out.println("\tReceived notification: " + n.getType());
            if (n instanceof MonitorNotification) {
                MonitorNotification mn = (MonitorNotification) n;
                System.out.println("\tSource: " +
                    mn.getSource());
                System.out.println("\tType: " +
                    mn.getType());
                System.out.println("\tTimeStamp: " +
                    mn.getTimeStamp());
                System.out.println("\tObservedObject: " +
                    mn.getObservedObject());
                System.out.println("\tObservedAttribute: " +
                    mn.getObservedAttribute());
                System.out.println("\tDerivedGauge: " +
                    mn.getDerivedGauge());
                System.out.println("\tTrigger: " +
                    mn.getTrigger());
            }
        }
    }
    public static void runTest(int offset,
                               int counter[],
                               int derivedGauge[],
                               int threshold[]) throws Exception {
        System.out.println("\nRetrieve the platform MBean server");
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        String domain = mbs.getDefaultDomain();
        ObjectName name =
            new ObjectName(domain +
                           ":type=" + Test.class.getName() +
                           ",offset=" + offset);
        mbs.createMBean(Test.class.getName(), name);
        TestMBean mbean = (TestMBean)
            MBeanServerInvocationHandler.newProxyInstance(
                mbs, name, TestMBean.class, false);
        ObjectName cmn =
            new ObjectName(domain +
                           ":type=" + CounterMonitor.class.getName() +
                           ",offset=" + offset);
        CounterMonitor m = new CounterMonitor();
        mbs.registerMBean(m, cmn);
        CounterMonitorMBean cm = (CounterMonitorMBean)
            MBeanServerInvocationHandler.newProxyInstance(
                mbs, cmn, CounterMonitorMBean.class, true);
        ((NotificationEmitter) cm).addNotificationListener(
            new Listener(), null, null);
        cm.addObservedObject(name);
        cm.setObservedAttribute("Counter");
        cm.setGranularityPeriod(100);
        cm.setInitThreshold(1);
        cm.setOffset(offset);
        cm.setModulus(5);
        cm.setNotify(true);
        System.out.println("\nStart monitoring...");
        cm.start();
        for (int i = 0; i < counter.length; i++) {
            mbean.setCounter(counter[i]);
            System.out.println("\nCounter = " + mbean.getCounter());
            Thread.sleep(300);
            Integer derivedGaugeValue = (Integer) cm.getDerivedGauge(name);
            System.out.println("Derived Gauge = " + derivedGaugeValue);
            if (derivedGaugeValue.intValue() != derivedGauge[i]) {
                System.out.println("Wrong derived gauge! Current value = " +
                    derivedGaugeValue + " Expected value = " + derivedGauge[i]);
                System.out.println("\nStop monitoring...");
                cm.stop();
                throw new IllegalArgumentException("wrong derived gauge");
            }
            Number thresholdValue = cm.getThreshold(name);
            System.out.println("Threshold = " + thresholdValue);
            if (thresholdValue.intValue() != threshold[i]) {
                System.out.println("Wrong threshold! Current value = " +
                    thresholdValue + " Expected value = " + threshold[i]);
                System.out.println("\nStop monitoring...");
                cm.stop();
                throw new IllegalArgumentException("wrong threshold");
            }
            Thread.sleep(300);
        }
        System.out.println("\nStop monitoring...");
        cm.stop();
    }
    public static void main(String[] args) throws Exception {
        runTest(1, counter1, derivedGauge1, threshold1);
        runTest(3, counter2, derivedGauge2, threshold2);
    }
}
