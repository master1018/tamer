public class VoiceDataInstallerReceiver extends BroadcastReceiver {
    private final static String TAG = "RunVoiceDataInstaller";
    private final static String INSTALLER_PACKAGE = "com.svox.langpack.installer";
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction())
                && INSTALLER_PACKAGE.equals(getPackageName(intent))) {
            Log.v(TAG, INSTALLER_PACKAGE + " package was added, running installer...");
            Intent runIntent = new Intent("com.svox.langpack.installer.RUN_TTS_DATA_INSTALLER");
            runIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(runIntent);
        }
    }
    private static String getPackageName(Intent intent) {
        return intent.getData().getSchemeSpecificPart();
    }
}
