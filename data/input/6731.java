public class GaugeMonitor extends Monitor implements GaugeMonitorMBean {
    static class GaugeMonitorObservedObject extends ObservedObject {
        public GaugeMonitorObservedObject(ObjectName observedObject) {
            super(observedObject);
        }
        public final synchronized boolean getDerivedGaugeValid() {
            return derivedGaugeValid;
        }
        public final synchronized void setDerivedGaugeValid(
                                                 boolean derivedGaugeValid) {
            this.derivedGaugeValid = derivedGaugeValid;
        }
        public final synchronized NumericalType getType() {
            return type;
        }
        public final synchronized void setType(NumericalType type) {
            this.type = type;
        }
        public final synchronized Number getPreviousScanGauge() {
            return previousScanGauge;
        }
        public final synchronized void setPreviousScanGauge(
                                                  Number previousScanGauge) {
            this.previousScanGauge = previousScanGauge;
        }
        public final synchronized int getStatus() {
            return status;
        }
        public final synchronized void setStatus(int status) {
            this.status = status;
        }
        private boolean derivedGaugeValid;
        private NumericalType type;
        private Number previousScanGauge;
        private int status;
    }
    private Number highThreshold = INTEGER_ZERO;
    private Number lowThreshold = INTEGER_ZERO;
    private boolean notifyHigh = false;
    private boolean notifyLow = false;
    private boolean differenceMode = false;
    private static final String[] types = {
        RUNTIME_ERROR,
        OBSERVED_OBJECT_ERROR,
        OBSERVED_ATTRIBUTE_ERROR,
        OBSERVED_ATTRIBUTE_TYPE_ERROR,
        THRESHOLD_ERROR,
        THRESHOLD_HIGH_VALUE_EXCEEDED,
        THRESHOLD_LOW_VALUE_EXCEEDED
    };
    private static final MBeanNotificationInfo[] notifsInfo = {
        new MBeanNotificationInfo(
            types,
            "javax.management.monitor.MonitorNotification",
            "Notifications sent by the GaugeMonitor MBean")
    };
    private static final int RISING             = 0;
    private static final int FALLING            = 1;
    private static final int RISING_OR_FALLING  = 2;
    public GaugeMonitor() {
    }
    public synchronized void start() {
        if (isActive()) {
            MONITOR_LOGGER.logp(Level.FINER, GaugeMonitor.class.getName(),
                    "start", "the monitor is already active");
            return;
        }
        for (ObservedObject o : observedObjects) {
            final GaugeMonitorObservedObject gmo =
                (GaugeMonitorObservedObject) o;
            gmo.setStatus(RISING_OR_FALLING);
            gmo.setPreviousScanGauge(null);
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
    public synchronized Number getHighThreshold() {
        return highThreshold;
    }
    public synchronized Number getLowThreshold() {
        return lowThreshold;
    }
    public synchronized void setThresholds(Number highValue, Number lowValue)
        throws IllegalArgumentException {
        if ((highValue == null) || (lowValue == null)) {
            throw new IllegalArgumentException("Null threshold value");
        }
        if (highValue.getClass() != lowValue.getClass()) {
            throw new IllegalArgumentException("Different type " +
                                               "threshold values");
        }
        if (isFirstStrictlyGreaterThanLast(lowValue, highValue,
                                           highValue.getClass().getName())) {
            throw new IllegalArgumentException("High threshold less than " +
                                               "low threshold");
        }
        if (highThreshold.equals(highValue) && lowThreshold.equals(lowValue))
            return;
        highThreshold = highValue;
        lowThreshold = lowValue;
        int index = 0;
        for (ObservedObject o : observedObjects) {
            resetAlreadyNotified(o, index++, THRESHOLD_ERROR_NOTIFIED);
            final GaugeMonitorObservedObject gmo =
                (GaugeMonitorObservedObject) o;
            gmo.setStatus(RISING_OR_FALLING);
        }
    }
    public synchronized boolean getNotifyHigh() {
        return notifyHigh;
    }
    public synchronized void setNotifyHigh(boolean value) {
        if (notifyHigh == value)
            return;
        notifyHigh = value;
    }
    public synchronized boolean getNotifyLow() {
        return notifyLow;
    }
    public synchronized void setNotifyLow(boolean value) {
        if (notifyLow == value)
            return;
        notifyLow = value;
    }
    public synchronized boolean getDifferenceMode() {
        return differenceMode;
    }
    public synchronized void setDifferenceMode(boolean value) {
        if (differenceMode == value)
            return;
        differenceMode = value;
        for (ObservedObject o : observedObjects) {
            final GaugeMonitorObservedObject gmo =
                (GaugeMonitorObservedObject) o;
            gmo.setStatus(RISING_OR_FALLING);
            gmo.setPreviousScanGauge(null);
        }
    }
    @Override
    public MBeanNotificationInfo[] getNotificationInfo() {
        return notifsInfo.clone();
    }
    private synchronized boolean updateDerivedGauge(
        Object scanGauge, GaugeMonitorObservedObject o) {
        boolean is_derived_gauge_valid;
        if (differenceMode) {
            if (o.getPreviousScanGauge() != null) {
                setDerivedGaugeWithDifference((Number)scanGauge, o);
                is_derived_gauge_valid = true;
            }
            else {
                is_derived_gauge_valid = false;
            }
            o.setPreviousScanGauge((Number)scanGauge);
        }
        else {
            o.setDerivedGauge((Number)scanGauge);
            is_derived_gauge_valid = true;
        }
        return is_derived_gauge_valid;
    }
    private synchronized MonitorNotification updateNotifications(
        GaugeMonitorObservedObject o) {
        MonitorNotification n = null;
        if (o.getStatus() == RISING_OR_FALLING) {
            if (isFirstGreaterThanLast((Number)o.getDerivedGauge(),
                                       highThreshold,
                                       o.getType())) {
                if (notifyHigh) {
                    n = new MonitorNotification(
                            THRESHOLD_HIGH_VALUE_EXCEEDED,
                            this,
                            0,
                            0,
                            "",
                            null,
                            null,
                            null,
                            highThreshold);
                }
                o.setStatus(FALLING);
            } else if (isFirstGreaterThanLast(lowThreshold,
                                              (Number)o.getDerivedGauge(),
                                              o.getType())) {
                if (notifyLow) {
                    n = new MonitorNotification(
                            THRESHOLD_LOW_VALUE_EXCEEDED,
                            this,
                            0,
                            0,
                            "",
                            null,
                            null,
                            null,
                            lowThreshold);
                }
                o.setStatus(RISING);
            }
        } else {
            if (o.getStatus() == RISING) {
                if (isFirstGreaterThanLast((Number)o.getDerivedGauge(),
                                           highThreshold,
                                           o.getType())) {
                    if (notifyHigh) {
                        n = new MonitorNotification(
                                THRESHOLD_HIGH_VALUE_EXCEEDED,
                                this,
                                0,
                                0,
                                "",
                                null,
                                null,
                                null,
                                highThreshold);
                    }
                    o.setStatus(FALLING);
                }
            } else if (o.getStatus() == FALLING) {
                if (isFirstGreaterThanLast(lowThreshold,
                                           (Number)o.getDerivedGauge(),
                                           o.getType())) {
                    if (notifyLow) {
                        n = new MonitorNotification(
                                THRESHOLD_LOW_VALUE_EXCEEDED,
                                this,
                                0,
                                0,
                                "",
                                null,
                                null,
                                null,
                                lowThreshold);
                    }
                    o.setStatus(RISING);
                }
            }
        }
        return n;
    }
    private synchronized void setDerivedGaugeWithDifference(
        Number scanGauge, GaugeMonitorObservedObject o) {
        Number prev = o.getPreviousScanGauge();
        Number der;
        switch (o.getType()) {
        case INTEGER:
            der = Integer.valueOf(((Integer)scanGauge).intValue() -
                                  ((Integer)prev).intValue());
            break;
        case BYTE:
            der = Byte.valueOf((byte)(((Byte)scanGauge).byteValue() -
                                      ((Byte)prev).byteValue()));
            break;
        case SHORT:
            der = Short.valueOf((short)(((Short)scanGauge).shortValue() -
                                        ((Short)prev).shortValue()));
            break;
        case LONG:
            der = Long.valueOf(((Long)scanGauge).longValue() -
                               ((Long)prev).longValue());
            break;
        case FLOAT:
            der = Float.valueOf(((Float)scanGauge).floatValue() -
                                ((Float)prev).floatValue());
            break;
        case DOUBLE:
            der = Double.valueOf(((Double)scanGauge).doubleValue() -
                                 ((Double)prev).doubleValue());
            break;
        default:
            MONITOR_LOGGER.logp(Level.FINEST, GaugeMonitor.class.getName(),
                    "setDerivedGaugeWithDifference",
                    "the threshold type is invalid");
            return;
        }
        o.setDerivedGauge(der);
    }
    private boolean isFirstGreaterThanLast(Number greater,
                                           Number less,
                                           NumericalType type) {
        switch (type) {
        case INTEGER:
        case BYTE:
        case SHORT:
        case LONG:
            return (greater.longValue() >= less.longValue());
        case FLOAT:
        case DOUBLE:
            return (greater.doubleValue() >= less.doubleValue());
        default:
            MONITOR_LOGGER.logp(Level.FINEST, GaugeMonitor.class.getName(),
                    "isFirstGreaterThanLast",
                    "the threshold type is invalid");
            return false;
        }
    }
    private boolean isFirstStrictlyGreaterThanLast(Number greater,
                                                   Number less,
                                                   String className) {
        if (className.equals("java.lang.Integer") ||
            className.equals("java.lang.Byte") ||
            className.equals("java.lang.Short") ||
            className.equals("java.lang.Long")) {
            return (greater.longValue() > less.longValue());
        }
        else if (className.equals("java.lang.Float") ||
                 className.equals("java.lang.Double")) {
            return (greater.doubleValue() > less.doubleValue());
        }
        else {
            MONITOR_LOGGER.logp(Level.FINEST, GaugeMonitor.class.getName(),
                    "isFirstStrictlyGreaterThanLast",
                    "the threshold type is invalid");
            return false;
        }
    }
    @Override
    ObservedObject createObservedObject(ObjectName object) {
        final GaugeMonitorObservedObject gmo =
            new GaugeMonitorObservedObject(object);
        gmo.setStatus(RISING_OR_FALLING);
        gmo.setPreviousScanGauge(null);
        return gmo;
    }
    @Override
    synchronized boolean isComparableTypeValid(ObjectName object,
                                               String attribute,
                                               Comparable<?> value) {
        final GaugeMonitorObservedObject o =
            (GaugeMonitorObservedObject) getObservedObject(object);
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
        } else if (value instanceof Float) {
            o.setType(FLOAT);
        } else if (value instanceof Double) {
            o.setType(DOUBLE);
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
        final GaugeMonitorObservedObject o =
            (GaugeMonitorObservedObject) getObservedObject(object);
        if (o == null)
            return null;
        o.setDerivedGaugeValid(updateDerivedGauge(value, o));
        return (Comparable<?>) o.getDerivedGauge();
    }
    @Override
    synchronized void onErrorNotification(MonitorNotification notification) {
        final GaugeMonitorObservedObject o = (GaugeMonitorObservedObject)
            getObservedObject(notification.getObservedObject());
        if (o == null)
            return;
        o.setStatus(RISING_OR_FALLING);
        o.setPreviousScanGauge(null);
    }
    @Override
    synchronized MonitorNotification buildAlarmNotification(
                                               ObjectName object,
                                               String attribute,
                                               Comparable<?> value) {
        final GaugeMonitorObservedObject o =
            (GaugeMonitorObservedObject) getObservedObject(object);
        if (o == null)
            return null;
        final MonitorNotification alarm;
        if (o.getDerivedGaugeValid())
            alarm = updateNotifications(o);
        else
            alarm = null;
        return alarm;
    }
    @Override
    synchronized boolean isThresholdTypeValid(ObjectName object,
                                              String attribute,
                                              Comparable<?> value) {
        final GaugeMonitorObservedObject o =
            (GaugeMonitorObservedObject) getObservedObject(object);
        if (o == null)
            return false;
        Class<? extends Number> c = classForType(o.getType());
        return (isValidForType(highThreshold, c) &&
                isValidForType(lowThreshold, c));
    }
}
