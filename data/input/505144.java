public class LangPackUninstaller extends BroadcastReceiver {
    private final static String TAG = "LangPackUninstaller";
    private final static String INSTALLER_PACKAGE = "com.svox.langpack.installer";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(TAG, "about to delete " + INSTALLER_PACKAGE);
        context.getPackageManager().deletePackage(INSTALLER_PACKAGE, null, 0);
    }
    private static String getPackageName(Intent intent) {
        return intent.getData().getSchemeSpecificPart();
    }
}
