public class OtaStartupReceiver extends BroadcastReceiver {
    private static final String TAG = "OtaStartupReceiver";
    private static final boolean DBG = (SystemProperties.getInt("ro.debuggable", 0) == 1);
    private static final int MIN_READY = 10;
    private Context mContext;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MIN_READY:
                Log.v(TAG, "Attempting OtaActivation from handler");
                OtaUtils.maybeDoOtaCall(mContext, mHandler, MIN_READY);
            }
        }
    };
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        if (!OtaUtils.isCdmaPhone()) {
            if (DBG) Log.d(TAG, "Not a CDMA phone, no need to process OTA");
            return;
        }
        if (shouldPostpone(context)) {
            if (DBG) Log.d(TAG, "Postponing CDMA provisioning until wizard runs");
            return;
        }
        OtaUtils.maybeDoOtaCall(mContext, mHandler, MIN_READY);
    }
    private boolean shouldPostpone(Context context) {
        Intent intent = new Intent("android.intent.action.DEVICE_INITIALIZATION_WIZARD");
        ResolveInfo resolveInfo = context.getPackageManager().resolveActivity(intent, 
                PackageManager.MATCH_DEFAULT_ONLY);
        boolean provisioned = Settings.Secure.getInt(context.getContentResolver(),
                Settings.Secure.DEVICE_PROVISIONED, 0) != 0;
        String mode = SystemProperties.get("ro.setupwizard.mode", "REQUIRED");
        boolean runningSetupWizard = "REQUIRED".equals(mode) || "OPTIONAL".equals(mode);
        if (DBG) {
            Log.v(TAG, "resolvInfo = " + resolveInfo + ", provisioned = " + provisioned 
                    + ", runningSetupWizard = " + runningSetupWizard);
        }
        return resolveInfo != null && !provisioned && runningSetupWizard;
    }
}
