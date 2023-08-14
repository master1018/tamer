abstract class StkAppInstaller {
    private StkAppInstaller() {}
    static void install(Context context) {
        setAppState(context, true);
    }
    static void unInstall(Context context) {
        setAppState(context, false);
    }
    private static void setAppState(Context context, boolean install) {
        if (context == null) {
            return;
        }
        PackageManager pm = context.getPackageManager();
        if (pm == null) {
            return;
        }
        ComponentName cName = new ComponentName("com.android.stk",
                "com.android.stk.StkLauncherActivity");
        int state = install ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
        try {
            pm.setComponentEnabledSetting(cName, state,
                    PackageManager.DONT_KILL_APP);
        } catch (Exception e) {
            StkLog.d("StkAppInstaller", "Could not change STK app state");
        }
    }
}
