class TimerAlarmClockNotification
    extends javax.management.Notification {
    private static final long serialVersionUID = -4841061275673620641L;
    public TimerAlarmClockNotification(TimerAlarmClock source) {
        super("", source, 0);
    }
}
