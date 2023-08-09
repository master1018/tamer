public class ThrottleManager
{
    public static final String THROTTLE_POLL_ACTION = "android.net.thrott.POLL_ACTION";
    public static final String EXTRA_CYCLE_READ = "cycleRead";
    public static final String EXTRA_CYCLE_WRITE = "cycleWrite";
    public static final String EXTRA_CYCLE_START = "cycleStart";
    public static final String EXTRA_CYCLE_END = "cycleEnd";
    public static final String THROTTLE_ACTION = "android.net.thrott.THROTTLE_ACTION";
    public static final String EXTRA_THROTTLE_LEVEL = "level";
    public static final String POLICY_CHANGED_ACTION = "android.net.thrott.POLICY_CHANGED_ACTION";
    public static final int DIRECTION_TX = 0;
    public static final int DIRECTION_RX = 1;
    public static final int PERIOD_CYCLE  = 0;
    public static final int PERIOD_YEAR   = 1;
    public static final int PERIOD_MONTH  = 2;
    public static final int PERIOD_WEEK   = 3;
    public static final int PERIOD_7DAY   = 4;
    public static final int PERIOD_DAY    = 5;
    public static final int PERIOD_24HOUR = 6;
    public static final int PERIOD_HOUR   = 7;
    public static final int PERIOD_60MIN  = 8;
    public static final int PERIOD_MINUTE = 9;
    public static final int PERIOD_60SEC  = 10;
    public static final int PERIOD_SECOND = 11;
    public long getResetTime(String iface) {
        try {
            return mService.getResetTime(iface);
        } catch (RemoteException e) {
            return -1;
        }
    }
    public long getPeriodStartTime(String iface) {
        try {
            return mService.getPeriodStartTime(iface);
        } catch (RemoteException e) {
            return -1;
        }
    }
    public long getByteCount(String iface, int direction, int period, int ago) {
        try {
            return mService.getByteCount(iface, direction, period, ago);
        } catch (RemoteException e) {
            return -1;
        }
    }
    public long getCliffThreshold(String iface, int cliff) {
        try {
            return mService.getCliffThreshold(iface, cliff);
        } catch (RemoteException e) {
            return -1;
        }
    }
    public int getCliffLevel(String iface, int cliff) {
        try {
            return mService.getCliffLevel(iface, cliff);
        } catch (RemoteException e) {
            return -1;
        }
    }
    public String getHelpUri() {
        try {
            return mService.getHelpUri();
        } catch (RemoteException e) {
            return null;
        }
    }
    private IThrottleManager mService;
    @SuppressWarnings({"UnusedDeclaration"})
    private ThrottleManager() {
    }
    public ThrottleManager(IThrottleManager service) {
        if (service == null) {
            throw new IllegalArgumentException(
                "ThrottleManager() cannot be constructed with null service");
        }
        mService = service;
    }
}
