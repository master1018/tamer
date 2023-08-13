public class MonitorNotification extends javax.management.Notification {
    public static final String OBSERVED_OBJECT_ERROR = "jmx.monitor.error.mbean";
    public static final String OBSERVED_ATTRIBUTE_ERROR = "jmx.monitor.error.attribute";
    public static final String OBSERVED_ATTRIBUTE_TYPE_ERROR = "jmx.monitor.error.type";
    public static final String THRESHOLD_ERROR = "jmx.monitor.error.threshold";
    public static final String RUNTIME_ERROR = "jmx.monitor.error.runtime";
    public static final String THRESHOLD_VALUE_EXCEEDED = "jmx.monitor.counter.threshold";
    public static final String THRESHOLD_HIGH_VALUE_EXCEEDED = "jmx.monitor.gauge.high";
    public static final String THRESHOLD_LOW_VALUE_EXCEEDED = "jmx.monitor.gauge.low";
    public static final String STRING_TO_COMPARE_VALUE_MATCHED = "jmx.monitor.string.matches";
    public static final String STRING_TO_COMPARE_VALUE_DIFFERED = "jmx.monitor.string.differs";
    private static final long serialVersionUID = -4608189663661929204L;
    private ObjectName observedObject = null;
    private String observedAttribute = null;
    private Object derivedGauge = null;
    private Object trigger = null;
    MonitorNotification(String type, Object source, long sequenceNumber, long timeStamp, String msg,
                               ObjectName obsObj, String obsAtt, Object derGauge, Object trigger) {
        super(type, source, sequenceNumber, timeStamp, msg);
        this.observedObject = obsObj;
        this.observedAttribute = obsAtt;
        this.derivedGauge = derGauge;
        this.trigger = trigger;
    }
    public ObjectName getObservedObject() {
        return observedObject;
    }
    public String getObservedAttribute() {
        return observedAttribute;
    }
    public Object getDerivedGauge() {
        return derivedGauge;
    }
    public Object getTrigger() {
        return trigger;
    }
}
