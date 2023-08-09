public class NonComparableAttributeValueTest implements NotificationListener {
    private boolean messageReceived = false;
    public class ObservedObject implements ObservedObjectMBean {
        public Object getIntegerAttribute() {
            return new Object();
        }
        public Object getStringAttribute() {
            return new Object();
        }
    }
    public interface ObservedObjectMBean {
        public Object getIntegerAttribute();
        public Object getStringAttribute();
    }
    public void handleNotification(Notification notification,
                                   Object handback) {
        MonitorNotification n = (MonitorNotification) notification;
        echo("\tInside handleNotification...");
        String type = n.getType();
        try {
            if (type.equals(
                    MonitorNotification.OBSERVED_ATTRIBUTE_TYPE_ERROR)) {
                echo("\t\t" + n.getObservedAttribute() + " is null");
                echo("\t\tDerived Gauge = " + n.getDerivedGauge());
                echo("\t\tTrigger = " + n.getTrigger());
                messageReceived = true;
            } else {
                echo("\t\tSkipping notification of type: " + type);
            }
        } catch (Exception e) {
            echo("\tError in handleNotification!");
            e.printStackTrace(System.out);
        }
    }
    public int counterMonitorNotification() throws Exception {
        CounterMonitor counterMonitor = null;
        try {
            MBeanServer server = MBeanServerFactory.newMBeanServer();
            String domain = server.getDefaultDomain();
            echo(">>> CREATE a new CounterMonitor MBean");
            ObjectName counterMonitorName = new ObjectName(
                            domain + ":type=" + CounterMonitor.class.getName());
            counterMonitor = new CounterMonitor();
            server.registerMBean(counterMonitor, counterMonitorName);
            echo(">>> ADD a listener to the CounterMonitor");
            counterMonitor.addNotificationListener(this, null, null);
            echo(">>> CREATE a new ObservedObject MBean");
            ObjectName obsObjName =
                ObjectName.getInstance(domain + ":type=ObservedObject");
            ObservedObject obsObj = new ObservedObject();
            server.registerMBean(obsObj, obsObjName);
            echo(">>> SET the attributes of the CounterMonitor:");
            counterMonitor.addObservedObject(obsObjName);
            echo("\tATTRIBUTE \"ObservedObject\"    = " + obsObjName);
            counterMonitor.setObservedAttribute("IntegerAttribute");
            echo("\tATTRIBUTE \"ObservedAttribute\" = IntegerAttribute");
            counterMonitor.setNotify(true);
            echo("\tATTRIBUTE \"NotifyFlag\"        = true");
            Integer threshold = 2;
            counterMonitor.setInitThreshold(threshold);
            echo("\tATTRIBUTE \"Threshold\"         = " + threshold);
            int granularityperiod = 500;
            counterMonitor.setGranularityPeriod(granularityperiod);
            echo("\tATTRIBUTE \"GranularityPeriod\" = " + granularityperiod);
            echo(">>> START the CounterMonitor");
            counterMonitor.start();
            Thread.sleep(granularityperiod * 2);
            if (messageReceived) {
                echo("\tOK: CounterMonitor notification received");
            } else {
                echo("\tKO: CounterMonitor notification missed or not emitted");
                return 1;
            }
        } finally {
            if (counterMonitor != null)
                counterMonitor.stop();
        }
        return 0;
    }
    public int gaugeMonitorNotification() throws Exception {
        GaugeMonitor gaugeMonitor = null;
        try {
            MBeanServer server = MBeanServerFactory.newMBeanServer();
            String domain = server.getDefaultDomain();
            echo(">>> CREATE a new GaugeMonitor MBean");
            ObjectName gaugeMonitorName = new ObjectName(
                            domain + ":type=" + GaugeMonitor.class.getName());
            gaugeMonitor = new GaugeMonitor();
            server.registerMBean(gaugeMonitor, gaugeMonitorName);
            echo(">>> ADD a listener to the GaugeMonitor");
            gaugeMonitor.addNotificationListener(this, null, null);
            echo(">>> CREATE a new ObservedObject MBean");
            ObjectName obsObjName =
                ObjectName.getInstance(domain + ":type=ObservedObject");
            ObservedObject obsObj = new ObservedObject();
            server.registerMBean(obsObj, obsObjName);
            echo(">>> SET the attributes of the GaugeMonitor:");
            gaugeMonitor.addObservedObject(obsObjName);
            echo("\tATTRIBUTE \"ObservedObject\"    = " + obsObjName);
            gaugeMonitor.setObservedAttribute("IntegerAttribute");
            echo("\tATTRIBUTE \"ObservedAttribute\" = IntegerAttribute");
            gaugeMonitor.setNotifyLow(false);
            gaugeMonitor.setNotifyHigh(true);
            echo("\tATTRIBUTE \"Notify Low  Flag\"  = false");
            echo("\tATTRIBUTE \"Notify High Flag\"  = true");
            Double highThreshold = 3.0, lowThreshold = 2.5;
            gaugeMonitor.setThresholds(highThreshold, lowThreshold);
            echo("\tATTRIBUTE \"Low  Threshold\"    = " + lowThreshold);
            echo("\tATTRIBUTE \"High Threshold\"    = " + highThreshold);
            int granularityperiod = 500;
            gaugeMonitor.setGranularityPeriod(granularityperiod);
            echo("\tATTRIBUTE \"GranularityPeriod\" = " + granularityperiod);
            echo(">>> START the GaugeMonitor");
            gaugeMonitor.start();
            Thread.sleep(granularityperiod * 2);
            if (messageReceived) {
                echo("\tOK: GaugeMonitor notification received");
            } else {
                echo("\tKO: GaugeMonitor notification missed or not emitted");
                return 1;
            }
        } finally {
            if (gaugeMonitor != null)
                gaugeMonitor.stop();
        }
        return 0;
    }
    public int stringMonitorNotification() throws Exception {
        StringMonitor stringMonitor = null;
        try {
            MBeanServer server = MBeanServerFactory.newMBeanServer();
            String domain = server.getDefaultDomain();
            echo(">>> CREATE a new StringMonitor MBean");
            ObjectName stringMonitorName = new ObjectName(
                            domain + ":type=" + StringMonitor.class.getName());
            stringMonitor = new StringMonitor();
            server.registerMBean(stringMonitor, stringMonitorName);
            echo(">>> ADD a listener to the StringMonitor");
            stringMonitor.addNotificationListener(this, null, null);
            echo(">>> CREATE a new ObservedObject MBean");
            ObjectName obsObjName =
                ObjectName.getInstance(domain + ":type=ObservedObject");
            ObservedObject obsObj = new ObservedObject();
            server.registerMBean(obsObj, obsObjName);
            echo(">>> SET the attributes of the StringMonitor:");
            stringMonitor.addObservedObject(obsObjName);
            echo("\tATTRIBUTE \"ObservedObject\"    = " + obsObjName);
            stringMonitor.setObservedAttribute("StringAttribute");
            echo("\tATTRIBUTE \"ObservedAttribute\" = StringAttribute");
            stringMonitor.setNotifyMatch(true);
            echo("\tATTRIBUTE \"NotifyMatch\"       = true");
            stringMonitor.setNotifyDiffer(false);
            echo("\tATTRIBUTE \"NotifyDiffer\"      = false");
            stringMonitor.setStringToCompare("do_match_now");
            echo("\tATTRIBUTE \"StringToCompare\"   = \"do_match_now\"");
            int granularityperiod = 500;
            stringMonitor.setGranularityPeriod(granularityperiod);
            echo("\tATTRIBUTE \"GranularityPeriod\" = " + granularityperiod);
            echo(">>> START the StringMonitor");
            stringMonitor.start();
            Thread.sleep(granularityperiod * 2);
            if (messageReceived) {
                echo("\tOK: StringMonitor notification received");
            } else {
                echo("\tKO: StringMonitor notification missed or not emitted");
                return 1;
            }
        } finally {
            if (stringMonitor != null)
                stringMonitor.stop();
        }
        return 0;
    }
    public int monitorNotifications() throws Exception {
        echo(">>> ----------------------------------------");
        messageReceived = false;
        int error = counterMonitorNotification();
        echo(">>> ----------------------------------------");
        messageReceived = false;
        error += gaugeMonitorNotification();
        echo(">>> ----------------------------------------");
        messageReceived = false;
        error += stringMonitorNotification();
        echo(">>> ----------------------------------------");
        return error;
    }
    private static void echo(String message) {
        System.out.println(message);
    }
    public static void main (String args[]) throws Exception {
        NonComparableAttributeValueTest test = new NonComparableAttributeValueTest();
        int error = test.monitorNotifications();
        if (error > 0) {
            echo(">>> Unhappy Bye, Bye!");
            throw new IllegalStateException("Test FAILED: Didn't get all " +
                                            "the notifications that were " +
                                            "expected by the test!");
        } else {
            echo(">>> Happy Bye, Bye!");
        }
    }
}
