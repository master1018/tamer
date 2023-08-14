public class CounterMonitor extends Monitor implements CounterMonitorMBean {
    static class CounterMonitorObservedObject extends ObservedObject {
        public CounterMonitorObservedObject(ObjectName observedObject) {
            super(observedObject);
        }
        public final synchronized Number getThreshold() {
            return threshold;
        }
        public final synchronized void setThreshold(Number threshold) {
            this.threshold = threshold;
        }
        public final synchronized Number getPreviousScanCounter() {
            return previousScanCounter;
        }
        public final synchronized void setPreviousScanCounter(
                                                  Number previousScanCounter) {
            this.previousScanCounter = previousScanCounter;
        }
        public final synchronized boolean getModulusExceeded() {
            return modulusExceeded;
        }
        public final synchronized void setModulusExceeded(
                                                 boolean modulusExceeded) {
            this.modulusExceeded = modulusExceeded;
        }
        public final synchronized Number getDerivedGaugeExceeded() {
            return derivedGaugeExceeded;
        }
        public final synchronized void setDerivedGaugeExceeded(
                                                 Number derivedGaugeExceeded) {
            this.derivedGaugeExceeded = derivedGaugeExceeded;
        }
        public final synchronized boolean getDerivedGaugeValid() {
            return derivedGaugeValid;
        }
        public final synchronized void setDerivedGaugeValid(
                                                 boolean derivedGaugeValid) {
            this.derivedGaugeValid = derivedGaugeValid;
        }
        public final synchronized boolean getEventAlreadyNotified() {
            return eventAlreadyNotified;
        }
        public final synchronized void setEventAlreadyNotified(
                                               boolean eventAlreadyNotified) {
            this.eventAlreadyNotified = eventAlreadyNotified;
        }
        public final synchronized NumericalType getType() {
            return type;
        }
        public final synchronized void setType(NumericalType type) {
            this.type = type;
        }
        private Number threshold;
        private Number previousScanCounter;
        private boolean modulusExceeded;
        private Number derivedGaugeExceeded;
        private boolean derivedGaugeValid;
        private boolean eventAlreadyNotified;
        private NumericalType type;
    }
    private Number modulus = INTEGER_ZERO;
    private Number offset = INTEGER_ZERO;
    private boolean notify = false;
    private boolean differenceMode = false;
    private Number initThreshold = INTEGER_ZERO;
    private static final String[] types = {
        RUNTIME_ERROR,
        OBSERVED_OBJECT_ERROR,
        OBSERVED_ATTRIBUTE_ERROR,
        OBSERVED_ATTRIBUTE_TYPE_ERROR,
        THRESHOLD_ERROR,
        THRESHOLD_VALUE_EXCEEDED
    };
    private static final MBeanNotificationInfo[] notifsInfo = {
        new MBeanNotificationInfo(
            types,
            "javax.management.monitor.MonitorNotification",
            "Notifications sent by the CounterMonitor MBean")
    };
    public CounterMonitor() {
    }
    public synchronized void start() {
        if (isActive()) {
            MONITOR_LOGGER.logp(Level.FINER, CounterMonitor.class.getName(),
                    "start", "the monitor is already active");
            return;
        }
        for (ObservedObject o : observedObjects) {
            final CounterMonitorObservedObject cmo =
                (CounterMonitorObservedObject) o;
            cmo.setThreshold(initThreshold);
            cmo.setModulusExceeded(false);
            cmo.setEventAlreadyNotified(false);
            cmo.setPreviousScanCounter(null);
        }
        doStart();
    }
    public synchronized void stop() {
        doStop();
    }
    @Override
    public synchronized Number getDerivedGauge(ObjectName object) {
        return (Number) super.getDerivedGauge(object);
    }
    @Override
    public synchronized long getDerivedGaugeTimeStamp(ObjectName object) {
        return super.getDerivedGaugeTimeStamp(object);
    }
    public synchronized Number getThreshold(ObjectName object) {
        final CounterMonitorObservedObject o =
            (CounterMonitorObservedObject) getObservedObject(object);
        if (o == null)
            return null;
        if (offset.longValue() > 0L &&
            modulus.longValue() > 0L &&
            o.getThreshold().longValue() > modulus.longValue()) {
            return initThreshold;
        } else {
            return o.getThreshold();
        }
    }
    public synchronized Number getInitThreshold() {
        return initThreshold;
    }
    public synchronized void setInitThreshold(Number value)
        throws IllegalArgumentException {
        if (value == null) {
            throw new IllegalArgumentException("Null threshold");
        }
        if (value.longValue() < 0L) {
            throw new IllegalArgumentException("Negative threshold");
        }
        if (initThreshold.equals(value))
            return;
        initThreshold = value;
        int index = 0;
        for (ObservedObject o : observedObjects) {
            resetAlreadyNotified(o, index++, THRESHOLD_ERROR_NOTIFIED);
            final CounterMonitorObservedObject cmo =
                (CounterMonitorObservedObject) o;
            cmo.setThreshold(value);
            cmo.setModulusExceeded(false);
            cmo.setEventAlreadyNotified(false);
        }
    }
    @Deprecated
    public synchronized Number getDerivedGauge() {
        if (observedObjects.isEmpty()) {
            return null;
        } else {
            return (Number) observedObjects.get(0).getDerivedGauge();
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
    @Deprecated
    public synchronized Number getThreshold() {
        return getThreshold(getObservedObject());
    }
    @Deprecated
    public synchronized void setThreshold(Number value)
        throws IllegalArgumentException {
        setInitThreshold(value);
    }
    public synchronized Number getOffset() {
        return offset;
    }
    public synchronized void setOffset(Number value)
        throws IllegalArgumentException {
        if (value == null) {
            throw new IllegalArgumentException("Null offset");
        }
        if (value.longValue() < 0L) {
            throw new IllegalArgumentException("Negative offset");
        }
        if (offset.equals(value))
            return;
        offset = value;
        int index = 0;
        for (ObservedObject o : observedObjects) {
            resetAlreadyNotified(o, index++, THRESHOLD_ERROR_NOTIFIED);
        }
    }
    public synchronized Number getModulus() {
        return modulus;
    }
    public synchronized void setModulus(Number value)
        throws IllegalArgumentException {
        if (value == null) {
            throw new IllegalArgumentException("Null modulus");
        }
        if (value.longValue() < 0L) {
            throw new IllegalArgumentException("Negative modulus");
        }
        if (modulus.equals(value))
            return;
        modulus = value;
        int index = 0;
        for (ObservedObject o : observedObjects) {
            resetAlreadyNotified(o, index++, THRESHOLD_ERROR_NOTIFIED);
            final CounterMonitorObservedObject cmo =
                (CounterMonitorObservedObject) o;
            cmo.setModulusExceeded(false);
        }
    }
    public synchronized boolean getNotify() {
        return notify;
    }
    public synchronized void setNotify(boolean value) {
        if (notify == value)
            return;
        notify = value;
    }
    public synchronized boolean getDifferenceMode() {
        return differenceMode;
    }
    public synchronized void setDifferenceMode(boolean value) {
        if (differenceMode == value)
            return;
        differenceMode = value;
        for (ObservedObject o : observedObjects) {
            final CounterMonitorObservedObject cmo =
                (CounterMonitorObservedObject) o;
            cmo.setThreshold(initThreshold);
            cmo.setModulusExceeded(false);
            cmo.setEventAlreadyNotified(false);
            cmo.setPreviousScanCounter(null);
        }
    }
    @Override
    public MBeanNotificationInfo[] getNotificationInfo() {
        return notifsInfo.clone();
    }
    private synchronized boolean updateDerivedGauge(
        Object scanCounter, CounterMonitorObservedObject o) {
        boolean is_derived_gauge_valid;
        if (differenceMode) {
            if (o.getPreviousScanCounter() != null) {
                setDerivedGaugeWithDifference((Number)scanCounter, null, o);
                if (((Number)o.getDerivedGauge()).longValue() < 0L) {
                    if (modulus.longValue() > 0L) {
                        setDerivedGaugeWithDifference((Number)scanCounter,
                                                      modulus, o);
                    }
                    o.setThreshold(initThreshold);
                    o.setEventAlreadyNotified(false);
                }
                is_derived_gauge_valid = true;
            }
            else {
                is_derived_gauge_valid = false;
            }
            o.setPreviousScanCounter((Number)scanCounter);
        }
        else {
            o.setDerivedGauge((Number)scanCounter);
            is_derived_gauge_valid = true;
        }
        return is_derived_gauge_valid;
    }
    private synchronized MonitorNotification updateNotifications(
        CounterMonitorObservedObject o) {
        MonitorNotification n = null;
        if (!o.getEventAlreadyNotified()) {
            if (((Number)o.getDerivedGauge()).longValue() >=
                o.getThreshold().longValue()) {
                if (notify) {
                    n = new MonitorNotification(THRESHOLD_VALUE_EXCEEDED,
                                                this,
                                                0,
                                                0,
                                                "",
                                                null,
                                                null,
                                                null,
                                                o.getThreshold());
                }
                if (!differenceMode) {
                    o.setEventAlreadyNotified(true);
                }
            }
        } else {
            if (MONITOR_LOGGER.isLoggable(Level.FINER)) {
                final StringBuilder strb = new StringBuilder()
                .append("The notification:")
                .append("\n\tNotification observed object = ")
                .append(o.getObservedObject())
                .append("\n\tNotification observed attribute = ")
                .append(getObservedAttribute())
                .append("\n\tNotification threshold level = ")
                .append(o.getThreshold())
                .append("\n\tNotification derived gauge = ")
                .append(o.getDerivedGauge())
                .append("\nhas already been sent");
                MONITOR_LOGGER.logp(Level.FINER, CounterMonitor.class.getName(),
                        "updateNotifications", strb.toString());
            }
        }
        return n;
    }
    private synchronized void updateThreshold(CounterMonitorObservedObject o) {
        if (((Number)o.getDerivedGauge()).longValue() >=
            o.getThreshold().longValue()) {
            if (offset.longValue() > 0L) {
                long threshold_value = o.getThreshold().longValue();
                while (((Number)o.getDerivedGauge()).longValue() >=
                       threshold_value) {
                    threshold_value += offset.longValue();
                }
                switch (o.getType()) {
                    case INTEGER:
                        o.setThreshold(Integer.valueOf((int)threshold_value));
                        break;
                    case BYTE:
                        o.setThreshold(Byte.valueOf((byte)threshold_value));
                        break;
                    case SHORT:
                        o.setThreshold(Short.valueOf((short)threshold_value));
                        break;
                    case LONG:
                        o.setThreshold(Long.valueOf(threshold_value));
                        break;
                    default:
                        MONITOR_LOGGER.logp(Level.FINEST,
                                CounterMonitor.class.getName(),
                                "updateThreshold",
                                "the threshold type is invalid");
                        break;
                }
                if (!differenceMode) {
                    if (modulus.longValue() > 0L) {
                        if (o.getThreshold().longValue() >
                            modulus.longValue()) {
                            o.setModulusExceeded(true);
                            o.setDerivedGaugeExceeded(
                                (Number) o.getDerivedGauge());
                        }
                    }
                }
                o.setEventAlreadyNotified(false);
            } else {
                o.setModulusExceeded(true);
                o.setDerivedGaugeExceeded((Number) o.getDerivedGauge());
            }
        }
    }
    private synchronized void setDerivedGaugeWithDifference(
        Number scanCounter, Number mod, CounterMonitorObservedObject o) {
        long derived =
            scanCounter.longValue() - o.getPreviousScanCounter().longValue();
        if (mod != null)
            derived += modulus.longValue();
        switch (o.getType()) {
        case INTEGER: o.setDerivedGauge(Integer.valueOf((int) derived)); break;
        case BYTE: o.setDerivedGauge(Byte.valueOf((byte) derived)); break;
        case SHORT: o.setDerivedGauge(Short.valueOf((short) derived)); break;
        case LONG: o.setDerivedGauge(Long.valueOf(derived)); break;
        default:
            MONITOR_LOGGER.logp(Level.FINEST, CounterMonitor.class.getName(),
                    "setDerivedGaugeWithDifference",
                    "the threshold type is invalid");
            break;
        }
    }
    @Override
    ObservedObject createObservedObject(ObjectName object) {
        final CounterMonitorObservedObject cmo =
            new CounterMonitorObservedObject(object);
        cmo.setThreshold(initThreshold);
        cmo.setModulusExceeded(false);
        cmo.setEventAlreadyNotified(false);
        cmo.setPreviousScanCounter(null);
        return cmo;
    }
    @Override
    synchronized boolean isComparableTypeValid(ObjectName object,
                                               String attribute,
                                               Comparable<?> value) {
        final CounterMonitorObservedObject o =
            (CounterMonitorObservedObject) getObservedObject(object);
        if (o == null)
            return false;
        if (value instanceof Integer) {
            o.setType(INTEGER);
        } else if (value instanceof Byte) {
            o.setType(BYTE);
        } else if (value instanceof Short) {
            o.setType(SHORT);
        } else if (value instanceof Long) {
            o.setType(LONG);
        } else {
            return false;
        }
        return true;
    }
    @Override
    synchronized Comparable<?> getDerivedGaugeFromComparable(
                                                  ObjectName object,
                                                  String attribute,
                                                  Comparable<?> value) {
        final CounterMonitorObservedObject o =
            (CounterMonitorObservedObject) getObservedObject(object);
        if (o == null)
            return null;
        if (o.getModulusExceeded()) {
            if (((Number)o.getDerivedGauge()).longValue() <
                o.getDerivedGaugeExceeded().longValue()) {
                    o.setThreshold(initThreshold);
                    o.setModulusExceeded(false);
                    o.setEventAlreadyNotified(false);
            }
        }
        o.setDerivedGaugeValid(updateDerivedGauge(value, o));
        return (Comparable<?>) o.getDerivedGauge();
    }
    @Override
    synchronized void onErrorNotification(MonitorNotification notification) {
        final CounterMonitorObservedObject o = (CounterMonitorObservedObject)
            getObservedObject(notification.getObservedObject());
        if (o == null)
            return;
        o.setModulusExceeded(false);
        o.setEventAlreadyNotified(false);
        o.setPreviousScanCounter(null);
    }
    @Override
    synchronized MonitorNotification buildAlarmNotification(
                                               ObjectName object,
                                               String attribute,
                                               Comparable<?> value) {
        final CounterMonitorObservedObject o =
            (CounterMonitorObservedObject) getObservedObject(object);
        if (o == null)
            return null;
        final MonitorNotification alarm;
        if (o.getDerivedGaugeValid()) {
            alarm = updateNotifications(o);
            updateThreshold(o);
        } else {
            alarm = null;
        }
        return alarm;
    }
    @Override
    synchronized boolean isThresholdTypeValid(ObjectName object,
                                              String attribute,
                                              Comparable<?> value) {
        final CounterMonitorObservedObject o =
            (CounterMonitorObservedObject) getObservedObject(object);
        if (o == null)
            return false;
        Class<? extends Number> c = classForType(o.getType());
        return (c.isInstance(o.getThreshold()) &&
                isValidForType(offset, c) &&
                isValidForType(modulus, c));
    }
}
