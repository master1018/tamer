public class UiModeManager {
    private static final String TAG = "UiModeManager";
    public static String ACTION_ENTER_CAR_MODE = "android.app.action.ENTER_CAR_MODE";
    public static String ACTION_EXIT_CAR_MODE = "android.app.action.EXIT_CAR_MODE";
    public static String ACTION_ENTER_DESK_MODE = "android.app.action.ENTER_DESK_MODE";
    public static String ACTION_EXIT_DESK_MODE = "android.app.action.EXIT_DESK_MODE";
    public static final int MODE_NIGHT_AUTO = Configuration.UI_MODE_NIGHT_UNDEFINED >> 4;
    public static final int MODE_NIGHT_NO = Configuration.UI_MODE_NIGHT_NO >> 4;
    public static final int MODE_NIGHT_YES = Configuration.UI_MODE_NIGHT_YES >> 4;
    private IUiModeManager mService;
     UiModeManager() {
        mService = IUiModeManager.Stub.asInterface(
                ServiceManager.getService(Context.UI_MODE_SERVICE));
    }
    public static final int ENABLE_CAR_MODE_GO_CAR_HOME = 0x0001;
    public void enableCarMode(int flags) {
        if (mService != null) {
            try {
                mService.enableCarMode(flags);
            } catch (RemoteException e) {
                Log.e(TAG, "disableCarMode: RemoteException", e);
            }
        }
    }
    public static final int DISABLE_CAR_MODE_GO_HOME = 0x0001;
    public void disableCarMode(int flags) {
        if (mService != null) {
            try {
                mService.disableCarMode(flags);
            } catch (RemoteException e) {
                Log.e(TAG, "disableCarMode: RemoteException", e);
            }
        }
    }
    public int getCurrentModeType() {
        if (mService != null) {
            try {
                return mService.getCurrentModeType();
            } catch (RemoteException e) {
                Log.e(TAG, "getCurrentModeType: RemoteException", e);
            }
        }
        return Configuration.UI_MODE_TYPE_NORMAL;
    }
    public void setNightMode(int mode) {
        if (mService != null) {
            try {
                mService.setNightMode(mode);
            } catch (RemoteException e) {
                Log.e(TAG, "setNightMode: RemoteException", e);
            }
        }
    }
    public int getNightMode() {
        if (mService != null) {
            try {
                return mService.getNightMode();
            } catch (RemoteException e) {
                Log.e(TAG, "getNightMode: RemoteException", e);
            }
        }
        return -1;
    }
}
