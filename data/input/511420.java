public class DisableCarModeActivity extends Activity {
    private static final String TAG = "DisableCarModeActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            IUiModeManager uiModeManager = IUiModeManager.Stub.asInterface(
                    ServiceManager.getService("uimode"));
            uiModeManager.disableCarMode(UiModeManager.DISABLE_CAR_MODE_GO_HOME);
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to disable car mode", e);
        }
        finish();
    }
}
