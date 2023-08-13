public class CounterMonitorInitThresholdTest {
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
    public static void runTest() throws Exception {
        System.out.println("\nRetrieve the platform MBean server");
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        String domain = mbs.getDefaultDomain();
        ObjectName name1 =
            new ObjectName(domain +
                           ":type=" + Test.class.getName() +
                           ",name=1");
        mbs.createMBean(Test.class.getName(), name1);
        TestMBean mbean1 = (TestMBean)
            MBeanServerInvocationHandler.newProxyInstance(
                mbs, name1, TestMBean.class, false);
        ObjectName name2 =
            new ObjectName(domain +
                           ":type=" + Test.class.getName() +
                           ",name=2");
        mbs.createMBean(Test.class.getName(), name2);
        TestMBean mbean2 = (TestMBean)
            MBeanServerInvocationHandler.newProxyInstance(
                mbs, name2, TestMBean.class, false);
        ObjectName cmn =
            new ObjectName(domain +
                           ":type=" + CounterMonitor.class.getName());
        CounterMonitor m = new CounterMonitor();
        mbs.registerMBean(m, cmn);
        CounterMonitorMBean cm = (CounterMonitorMBean)
            MBeanServerInvocationHandler.newProxyInstance(
                mbs, cmn, CounterMonitorMBean.class, true);
        ((NotificationEmitter) cm).addNotificationListener(
            new Listener(), null, null);
        cm.setObservedAttribute("Counter");
        cm.setGranularityPeriod(100);
        cm.setInitThreshold(3);
        cm.setNotify(true);
        System.out.println("\nObservedObject \"" + name1 +
            "\" registered before starting the monitor");
        cm.addObservedObject(name1);
        System.out.println("\nStart monitoring...");
        cm.start();
        System.out.println("\nTest ObservedObject \"" + name1 + "\"");
        for (int i = 0; i < 4; i++) {
            mbean1.setCounter(i);
            System.out.println("\nCounter = " + mbean1.getCounter());
            Thread.sleep(300);
            Number thresholdValue = cm.getThreshold(name1);
            System.out.println("Threshold = " + thresholdValue);
            if (thresholdValue.intValue() != 3) {
                System.out.println("Wrong threshold! Current value = " +
                    thresholdValue + " Expected value = 3");
                System.out.println("\nStop monitoring...");
                cm.stop();
                throw new IllegalArgumentException("wrong threshold");
            }
            Thread.sleep(300);
        }
        System.out.println("\nObservedObject \"" + name2 +
            "\" registered after starting the monitor");
        cm.addObservedObject(name2);
        System.out.println("\nTest ObservedObject \"" + name2 + "\"");
        for (int i = 0; i < 4; i++) {
            mbean2.setCounter(i);
            System.out.println("\nCounter = " + mbean2.getCounter());
            Thread.sleep(300);
            Number thresholdValue = cm.getThreshold(name2);
            System.out.println("Threshold = " + thresholdValue);
            if (thresholdValue.intValue() != 3) {
                System.out.println("Wrong threshold! Current value = " +
                    thresholdValue + " Expected value = 3");
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
        runTest();
    }
}
