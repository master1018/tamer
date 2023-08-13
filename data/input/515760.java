public class StatusBarManager {
    public static final int DISABLE_EXPAND = 0x00000001;
    public static final int DISABLE_NOTIFICATION_ICONS = 0x00000002;
    public static final int DISABLE_NOTIFICATION_ALERTS = 0x00000004;
    public static final int DISABLE_NOTIFICATION_TICKER = 0x00000008;
    public static final int DISABLE_NONE = 0x00000000;
    private Context mContext;
    private IStatusBar mService;
    private IBinder mToken = new Binder();
    StatusBarManager(Context context) {
        mContext = context;
        mService = IStatusBar.Stub.asInterface(
                ServiceManager.getService(Context.STATUS_BAR_SERVICE));
    }
    public void disable(int what) {
        try {
            mService.disable(what, mToken, mContext.getPackageName());
        } catch (RemoteException ex) {
            throw new RuntimeException(ex);
        }
    }
    public void expand() {
        try {
            mService.activate();
        } catch (RemoteException ex) {
            throw new RuntimeException(ex);
        }
    }
    public void collapse() {
        try {
            mService.deactivate();
        } catch (RemoteException ex) {
            throw new RuntimeException(ex);
        }
    }
    public void toggle() {
        try {
            mService.toggle();
        } catch (RemoteException ex) {
            throw new RuntimeException(ex);
        }
    }
    public IBinder addIcon(String slot, int iconId, int iconLevel) {
        try {
            return mService.addIcon(slot, mContext.getPackageName(), iconId, iconLevel);
        } catch (RemoteException ex) {
            throw new RuntimeException(ex);
        }
    }
    public void updateIcon(IBinder key, String slot, int iconId, int iconLevel) {
        try {
            mService.updateIcon(key, slot, mContext.getPackageName(), iconId, iconLevel);
        } catch (RemoteException ex) {
            throw new RuntimeException(ex);
        }
    }
    public void removeIcon(IBinder key) {
        try {
            mService.removeIcon(key);
        } catch (RemoteException ex) {
            throw new RuntimeException(ex);
        }
    }
}
