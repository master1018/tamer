public class StringMonitor extends Monitor implements StringMonitorMBean {
    static class StringMonitorObservedObject extends ObservedObject {
        public StringMonitorObservedObject(ObjectName observedObject) {
            super(observedObject);
        }
        public final synchronized int getStatus() {
            return status;
        }
        public final synchronized void setStatus(int status) {
            this.status = status;
        }
        private int status;
    }
    private String stringToCompare = "";
    private boolean notifyMatch = false;
    private boolean notifyDiffer = false;
    private static final String[] types = {
        RUNTIME_ERROR,
        OBSERVED_OBJECT_ERROR,
        OBSERVED_ATTRIBUTE_ERROR,
        OBSERVED_ATTRIBUTE_TYPE_ERROR,
        STRING_TO_COMPARE_VALUE_MATCHED,
        STRING_TO_COMPARE_VALUE_DIFFERED
    };
    private static final MBeanNotificationInfo[] notifsInfo = {
        new MBeanNotificationInfo(
            types,
            "javax.management.monitor.MonitorNotification",
            "Notifications sent by the StringMonitor MBean")
    };
    private static final int MATCHING                   = 0;
    private static final int DIFFERING                  = 1;
    private static final int MATCHING_OR_DIFFERING      = 2;
    public StringMonitor() {
    }
    public synchronized void start() {
        if (isActive()) {
            MONITOR_LOGGER.logp(Level.FINER, StringMonitor.class.getName(),
                    "start", "the monitor is already active");
            return;
        }
        for (ObservedObject o : observedObjects) {
            final StringMonitorObservedObject smo =
                (StringMonitorObservedObject) o;
            smo.setStatus(MATCHING_OR_DIFFERING);
        }
        doStart();
    }
    public synchronized void stop() {
        doStop();
    }
    @Override
    public synchronized String getDerivedGauge(ObjectName object) {
        return (String) super.getDerivedGauge(object);
    }
    @Override
    public synchronized long getDerivedGaugeTimeStamp(ObjectName object) {
        return super.getDerivedGaugeTimeStamp(object);
    }
    @Deprecated
    public synchronized String getDerivedGauge() {
        if (observedObjects.isEmpty()) {
            return null;
        } else {
            return (String) observedObjects.get(0).getDerivedGauge();
        }
    }
    @Deprecated
    public synchronized long getDerivedGaugeTimeStamp() {
        if (observedObjects.isEmpty()) {
            return 0;
        } else {
            return observedObjects.get(0).getDerivedGaugeTimeStamp();
        }
    }
    public synchronized String getStringToCompare() {
        return stringToCompare;
    }
    public synchronized void setStringToCompare(String value)
        throws IllegalArgumentException {
        if (value == null) {
            throw new IllegalArgumentException("Null string to compare");
        }
        if (stringToCompare.equals(value))
            return;
        stringToCompare = value;
        for (ObservedObject o : observedObjects) {
            final StringMonitorObservedObject smo =
                (StringMonitorObservedObject) o;
            smo.setStatus(MATCHING_OR_DIFFERING);
        }
    }
    public synchronized boolean getNotifyMatch() {
        return notifyMatch;
    }
    public synchronized void setNotifyMatch(boolean value) {
        if (notifyMatch == value)
            return;
        notifyMatch = value;
    }
    public synchronized boolean getNotifyDiffer() {
        return notifyDiffer;
    }
    public synchronized void setNotifyDiffer(boolean value) {
        if (notifyDiffer == value)
            return;
        notifyDiffer = value;
    }
    @Override
    public MBeanNotificationInfo[] getNotificationInfo() {
        return notifsInfo.clone();
    }
    @Override
    ObservedObject createObservedObject(ObjectName object) {
        final StringMonitorObservedObject smo =
            new StringMonitorObservedObject(object);
        smo.setStatus(MATCHING_OR_DIFFERING);
        return smo;
    }
    @Override
    synchronized boolean isComparableTypeValid(ObjectName object,
                                               String attribute,
                                               Comparable<?> value) {
        if (value instanceof String) {
            return true;
        }
        return false;
    }
    @Override
    synchronized void onErrorNotification(MonitorNotification notification) {
        final StringMonitorObservedObject o = (StringMonitorObservedObject)
            getObservedObject(notification.getObservedObject());
        if (o == null)
            return;
        o.setStatus(MATCHING_OR_DIFFERING);
    }
    @Override
    synchronized MonitorNotification buildAlarmNotification(
                                               ObjectName object,
                                               String attribute,
                                               Comparable<?> value) {
        String type = null;
        String msg = null;
        Object trigger = null;
        final StringMonitorObservedObject o =
            (StringMonitorObservedObject) getObservedObject(object);
        if (o == null)
            return null;
        if (o.getStatus() == MATCHING_OR_DIFFERING) {
            if (o.getDerivedGauge().equals(stringToCompare)) {
                if (notifyMatch) {
                    type = STRING_TO_COMPARE_VALUE_MATCHED;
                    msg = "";
                    trigger = stringToCompare;
                }
                o.setStatus(DIFFERING);
            } else {
                if (notifyDiffer) {
                    type = STRING_TO_COMPARE_VALUE_DIFFERED;
                    msg = "";
                    trigger = stringToCompare;
                }
                o.setStatus(MATCHING);
            }
        } else {
            if (o.getStatus() == MATCHING) {
                if (o.getDerivedGauge().equals(stringToCompare)) {
                    if (notifyMatch) {
                        type = STRING_TO_COMPARE_VALUE_MATCHED;
                        msg = "";
                        trigger = stringToCompare;
                    }
                    o.setStatus(DIFFERING);
                }
            } else if (o.getStatus() == DIFFERING) {
                if (!o.getDerivedGauge().equals(stringToCompare)) {
                    if (notifyDiffer) {
                        type = STRING_TO_COMPARE_VALUE_DIFFERED;
                        msg = "";
                        trigger = stringToCompare;
                    }
                    o.setStatus(MATCHING);
                }
            }
        }
        return new MonitorNotification(type,
                                       this,
                                       0,
                                       0,
                                       msg,
                                       null,
                                       null,
                                       null,
                                       trigger);
    }
}
