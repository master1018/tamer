public class RuntimeExceptionTest implements NotificationListener {
    public class ObservedObject implements ObservedObjectMBean {
        public Integer getIntegerAttribute() {
            return i;
        }
        public void setIntegerAttribute(Integer i) {
            this.i = i;
        }
        public String getStringAttribute() {
            return s;
        }
        public void setStringAttribute(String s) {
            this.s = s;
        }
        private Integer i = 1;
        private String s = "dummy";
    }
    public interface ObservedObjectMBean {
        public Integer getIntegerAttribute();
        public void setIntegerAttribute(Integer i);
        public String getStringAttribute();
        public void setStringAttribute(String s);
    }
    public void handleNotification(Notification notification, Object handback) {
        echo(">>> Received notification: " + notification);
        if (notification instanceof MonitorNotification) {
            String type = notification.getType();
            if (type.equals(MonitorNotification.RUNTIME_ERROR)) {
                MonitorNotification mn = (MonitorNotification) notification;
                echo("\tType: " + mn.getType());
                echo("\tTimeStamp: " + mn.getTimeStamp());
                echo("\tObservedObject: " + mn.getObservedObject());
                echo("\tObservedAttribute: " + mn.getObservedAttribute());
                echo("\tDerivedGauge: " + mn.getDerivedGauge());
                echo("\tTrigger: " + mn.getTrigger());
                messageReceived = true;
            }
        }
    }
    public int counterMonitorNotification() throws Exception {
        CounterMonitor counterMonitor = new CounterMonitor();
        try {
            echo(">>> CREATE a new CounterMonitor MBean");
            ObjectName counterMonitorName = new ObjectName(
                            domain + ":type=" + CounterMonitor.class.getName());
            server.registerMBean(counterMonitor, counterMonitorName);
            echo(">>> ADD a listener to the CounterMonitor");
            counterMonitor.addNotificationListener(this, null, null);
            echo(">>> SET the attributes of the CounterMonitor:");
            counterMonitor.addObservedObject(obsObjName);
            echo("\tATTRIBUTE \"ObservedObject\"    = " + obsObjName);
            counterMonitor.setObservedAttribute("IntegerAttribute");
            echo("\tATTRIBUTE \"ObservedAttribute\" = IntegerAttribute");
            counterMonitor.setNotify(false);
            echo("\tATTRIBUTE \"NotifyFlag\"        = false");
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
                echo("\tOK: CounterMonitor got RUNTIME_ERROR notification!");
            } else {
                echo("\tKO: CounterMonitor did not get " +
                     "RUNTIME_ERROR notification!");
                return 1;
            }
        } finally {
            messageReceived = false;
            if (counterMonitor != null)
                counterMonitor.stop();
        }
        return 0;
    }
    public int gaugeMonitorNotification() throws Exception {
        GaugeMonitor gaugeMonitor = new GaugeMonitor();
        try {
            echo(">>> CREATE a new GaugeMonitor MBean");
            ObjectName gaugeMonitorName = new ObjectName(
                            domain + ":type=" + GaugeMonitor.class.getName());
            server.registerMBean(gaugeMonitor, gaugeMonitorName);
            echo(">>> ADD a listener to the GaugeMonitor");
            gaugeMonitor.addNotificationListener(this, null, null);
            echo(">>> SET the attributes of the GaugeMonitor:");
            gaugeMonitor.addObservedObject(obsObjName);
            echo("\tATTRIBUTE \"ObservedObject\"    = " + obsObjName);
            gaugeMonitor.setObservedAttribute("IntegerAttribute");
            echo("\tATTRIBUTE \"ObservedAttribute\" = IntegerAttribute");
            gaugeMonitor.setNotifyLow(false);
            gaugeMonitor.setNotifyHigh(false);
            echo("\tATTRIBUTE \"Notify Low  Flag\"  = false");
            echo("\tATTRIBUTE \"Notify High Flag\"  = false");
            Integer highThreshold = 3, lowThreshold = 2;
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
                echo("\tOK: GaugeMonitor got RUNTIME_ERROR notification!");
            } else {
                echo("\tKO: GaugeMonitor did not get " +
                     "RUNTIME_ERROR notification!");
                return 1;
            }
        } finally {
            messageReceived = false;
            if (gaugeMonitor != null)
                gaugeMonitor.stop();
        }
        return 0;
    }
    public int stringMonitorNotification() throws Exception {
        StringMonitor stringMonitor = new StringMonitor();
        try {
            echo(">>> CREATE a new StringMonitor MBean");
            ObjectName stringMonitorName = new ObjectName(
                            domain + ":type=" + StringMonitor.class.getName());
            server.registerMBean(stringMonitor, stringMonitorName);
            echo(">>> ADD a listener to the StringMonitor");
            stringMonitor.addNotificationListener(this, null, null);
            echo(">>> SET the attributes of the StringMonitor:");
            stringMonitor.addObservedObject(obsObjName);
            echo("\tATTRIBUTE \"ObservedObject\"    = " + obsObjName);
            stringMonitor.setObservedAttribute("StringAttribute");
            echo("\tATTRIBUTE \"ObservedAttribute\" = StringAttribute");
            stringMonitor.setNotifyMatch(false);
            echo("\tATTRIBUTE \"NotifyMatch\"       = false");
            stringMonitor.setNotifyDiffer(false);
            echo("\tATTRIBUTE \"NotifyDiffer\"      = false");
            stringMonitor.setStringToCompare("dummy");
            echo("\tATTRIBUTE \"StringToCompare\"   = \"dummy\"");
            int granularityperiod = 500;
            stringMonitor.setGranularityPeriod(granularityperiod);
            echo("\tATTRIBUTE \"GranularityPeriod\" = " + granularityperiod);
            echo(">>> START the StringMonitor");
            stringMonitor.start();
            Thread.sleep(granularityperiod * 2);
            if (messageReceived) {
                echo("\tOK: StringMonitor got RUNTIME_ERROR notification!");
            } else {
                echo("\tKO: StringMonitor did not get " +
                     "RUNTIME_ERROR notification!");
                return 1;
            }
        } finally {
            messageReceived = false;
            if (stringMonitor != null)
                stringMonitor.stop();
        }
        return 0;
    }
    public int monitorNotifications() throws Exception {
        server = MBeanServerFactory.newMBeanServer();
        MBeanServerForwarderInvocationHandler mbsfih =
            (MBeanServerForwarderInvocationHandler)
            Proxy.getInvocationHandler(server);
        mbsfih.setGetAttributeException(
            new RuntimeException("Test RuntimeException"));
        domain = server.getDefaultDomain();
        obsObjName = ObjectName.getInstance(domain + ":type=ObservedObject");
        server.registerMBean(new ObservedObject(), obsObjName);
        echo(">>> ----------------------------------------");
        int error = counterMonitorNotification();
        echo(">>> ----------------------------------------");
        error += gaugeMonitorNotification();
        echo(">>> ----------------------------------------");
        error += stringMonitorNotification();
        echo(">>> ----------------------------------------");
        return error;
    }
    private static void echo(String message) {
        System.out.println(message);
    }
    public static void main (String args[]) throws Exception {
        System.setProperty("javax.management.builder.initial",
                           MBeanServerBuilderImpl.class.getName());
        RuntimeExceptionTest test = new RuntimeExceptionTest();
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
    private boolean messageReceived = false;
    private MBeanServer server;
    private ObjectName obsObjName;
    private String domain;
}
