public class VoiceDialerReceiver extends BroadcastReceiver {
    private static final String TAG = "VoiceDialerReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Config.LOGD) Log.d(TAG, "onReceive " + intent);
        String action = intent.getAction();
        String host = intent.getData() != null ? intent.getData().getHost() : null;
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            CommandRecognizerEngine.deleteCachedGrammarFiles(context);
        }
        else if (Intent.ACTION_PACKAGE_ADDED.equals(action) ||
                Intent.ACTION_PACKAGE_CHANGED.equals(action) ||
                Intent.ACTION_PACKAGE_REMOVED.equals(action) ||
                Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE.equals(action) ||
                Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE.equals(action)) {
            CommandRecognizerEngine.deleteCachedGrammarFiles(context);
        }
        else if (Intents.SECRET_CODE_ACTION.equals(action) && "8351".equals(host)) {
            RecognizerLogger.enable(context);
            Toast.makeText(context, R.string.logging_enabled, Toast.LENGTH_LONG).show();
        }
        else if (Intents.SECRET_CODE_ACTION.equals(action) && "8350".equals(host)) {
            RecognizerLogger.disable(context);
            Toast.makeText(context, R.string.logging_disabled, Toast.LENGTH_LONG).show();
        }
    }
}
