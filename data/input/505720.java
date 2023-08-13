public class AlarmManager
{
    public static final int RTC_WAKEUP = 0;
    public static final int RTC = 1;
    public static final int ELAPSED_REALTIME_WAKEUP = 2;
    public static final int ELAPSED_REALTIME = 3;
    private final IAlarmManager mService;
    AlarmManager(IAlarmManager service) {
        mService = service;
    }
    public void set(int type, long triggerAtTime, PendingIntent operation) {
        try {
            mService.set(type, triggerAtTime, operation);
        } catch (RemoteException ex) {
        }
    }
    public void setRepeating(int type, long triggerAtTime, long interval,
            PendingIntent operation) {
        try {
            mService.setRepeating(type, triggerAtTime, interval, operation);
        } catch (RemoteException ex) {
        }
    }
    public static final long INTERVAL_FIFTEEN_MINUTES = 15 * 60 * 1000;
    public static final long INTERVAL_HALF_HOUR = 2*INTERVAL_FIFTEEN_MINUTES;
    public static final long INTERVAL_HOUR = 2*INTERVAL_HALF_HOUR;
    public static final long INTERVAL_HALF_DAY = 12*INTERVAL_HOUR;
    public static final long INTERVAL_DAY = 2*INTERVAL_HALF_DAY;
    public void setInexactRepeating(int type, long triggerAtTime, long interval,
            PendingIntent operation) {
        try {
            mService.setInexactRepeating(type, triggerAtTime, interval, operation);
        } catch (RemoteException ex) {
        }
    }
    public void cancel(PendingIntent operation) {
        try {
            mService.remove(operation);
        } catch (RemoteException ex) {
        }
    }
    public void setTime(long millis) {
        try {
            mService.setTime(millis);
        } catch (RemoteException ex) {
        }
    }
    public void setTimeZone(String timeZone) {
        try {
            mService.setTimeZone(timeZone);
        } catch (RemoteException ex) {
        }
    }
}
